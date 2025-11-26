package com.taobao.arthas.core.command.monitor200;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.advisor.AdviceListenerAdapter;
import com.taobao.arthas.core.advisor.ArthasMethod;
import com.taobao.arthas.core.shell.command.CommandProcess;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * LogStatAdviceListener — works with master-style AdviceListenerAdapter signatures.
 *
 * It instruments logger internal entry points (logback/log4j2) and when those methods are invoked,
 * it inspects the current thread stack to find the business caller (the frame outside logging packages),
 * then increments a concurrent counter keyed by "class#method", and writes a short line to the process.
 *
 * Notes:
 *  - This class matches the same callback signatures as AbstractTraceAdviceListener (before/afterReturning/afterThrowing/destroy).
 *  - If you want to change behavior (e.g., batch summary output), we can adapt easily.
 */
public class LogstatAdviceListener extends AdviceListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(LogstatAdviceListener.class);

    private final LogstatCommand command;
    private final CommandProcess process;
    private final boolean verbose;

    // counter: "com.example.Foo#bar" -> count
    private final Map<String, AtomicLong> counter = new ConcurrentHashMap<String, AtomicLong>();

    public LogstatAdviceListener(LogstatCommand command, CommandProcess process, boolean verbose) {
        this.command = command;
        this.process = process;
        this.verbose = verbose;
    }

    @Override
    public void destroy() {
        // nothing specific
        counter.clear();
    }

    /**
     * This before(...) signature matches your AbstractTraceAdviceListener.before(...)
     */
    @Override
    public void before(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args) throws Throwable {
        try {
            // We're invoked inside a logger internal method. Inspect stack to find the first frame outside logging packages.
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();

            String sourceClass = null;
            String sourceMethod = null;

            for (int i = 0; i < stack.length; i++) {
                StackTraceElement ste = stack[i];
                String cn = ste.getClassName();
                String mn = ste.getMethodName();

                // skip JVM and thread frames
                if (cn == null) continue;
                if (cn.startsWith("java.") || cn.startsWith("sun.") || cn.startsWith("jdk.internal.") || cn.equals(Thread.class.getName())) {
                    continue;
                }
                // skip logging internal packages (common ones)
                if (cn.startsWith("ch.qos.logback") || cn.startsWith("org.apache.logging.log4j")
                        || cn.startsWith("org.slf4j") || cn.startsWith("org.apache.commons")
                        || cn.startsWith("com.alibaba.arthas")) {
                    continue;
                }

                // If this frame's method looks like warn/error, take it.
                if ("warn".equalsIgnoreCase(mn) || "error".equalsIgnoreCase(mn)) {
                    sourceClass = cn;
                    sourceMethod = mn;
                    break;
                }

                // fallback heuristic: if next frame is a logging internal frame, this is likely the caller
                if (i + 1 < stack.length) {
                    String nextCn = stack[i + 1].getClassName();
                    if (nextCn != null && (nextCn.startsWith("ch.qos.logback") || nextCn.startsWith("org.apache.logging.log4j") || nextCn.startsWith("org.slf4j"))) {
                        sourceClass = cn;
                        sourceMethod = mn;
                        break;
                    }
                }
            }

            if (sourceClass == null) {
                // fallback to the instrumented class/method itself
                sourceClass = clazz != null ? clazz.getName() : "unknown";
                sourceMethod = method != null ? method.getName() : "unknown";
            }

            String key = sourceClass + "#" + sourceMethod;
            AtomicLong val = counter.get(key);
            if (val == null) {
                AtomicLong newVal = new AtomicLong(1);
                val = counter.putIfAbsent(key, newVal);
                if (val == null) {
                    val = newVal;
                } else {
                    val.incrementAndGet();
                }
            } else {
                val.incrementAndGet();
            }

            // determine level heuristically by scanning stack for 'error' or 'warn'
            boolean isError = false;
            boolean isWarn = false;
            for (StackTraceElement ste : stack) {
                String mn = ste.getMethodName();
                if ("error".equalsIgnoreCase(mn)) {
                    isError = true;
                    break;
                } else if ("warn".equalsIgnoreCase(mn)) {
                    isWarn = true;
                }
            }

            String wanted = command.getLevel();
            if ("ERROR".equals(wanted) && !isError) {
                return; // skip non-error
            } else if ("WARN".equals(wanted) && !isError && !isWarn) {
                return; // skip non-warn/non-error
            }

            long cnt = counter.get(key).get();

            String out = String.format("%s %s - count=%d\n", (isError ? "ERROR" : (isWarn ? "WARN" : "LOG")), key, cnt);

            try {
                process.write(out);
            } catch (Throwable t) {
                if (verbose) {
                    logger.warn("logstat: failed to write to process output: {}", t.getMessage());
                }
            }
        } catch (Throwable t) {
            if (verbose) {
                logger.warn("logstat before() failed.", t);
            }
        }
    }

    @Override
    public void afterReturning(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args, Object returnObject) throws Throwable {
        // no-op for now
    }

    @Override
    public void afterThrowing(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args, Throwable throwable) throws Throwable {
        // optionally, could mark throwable occurrences — not used now
    }

    /**
     * Snapshot / helper for summaries
     */
    public Map<String, AtomicLong> snapshot() {
        return new ConcurrentHashMap<String, AtomicLong>(counter);
    }
}
