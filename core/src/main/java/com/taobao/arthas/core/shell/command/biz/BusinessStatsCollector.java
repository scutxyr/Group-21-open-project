package com.taobao.arthas.core.shell.command.biz;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
public class BusinessStatsCollector {

    private static final Map<String, BusinessMethodStats> STATS_MAP = new ConcurrentHashMap<>();

    public static void recordInvoke(String className, String methodName, long costTime) {
        String key = className + "." + methodName;
        STATS_MAP.computeIfAbsent(key, k -> new BusinessMethodStats())
                .recordInvoke(costTime);
    }

    public static Map<String, BusinessMethodStats> getStats() {
        return new ConcurrentHashMap<>(STATS_MAP);
    }

    public static void reset() {
        STATS_MAP.clear();
    }

    public static class BusinessMethodStats {
        private final AtomicLong invokeCount = new AtomicLong(0);
        private final AtomicLong totalTime = new AtomicLong(0);
        private volatile long lastInvokeTime = System.currentTimeMillis();

        public void recordInvoke(long costTime) {
            invokeCount.incrementAndGet();
            totalTime.addAndGet(costTime);
            lastInvokeTime = System.currentTimeMillis();
        }

        public long getInvokeCount() {
            return invokeCount.get();
        }

        public double getAverageTime() {
            long count = invokeCount.get();
            long total = totalTime.get();
            return count > 0 ? (double) total / count : 0.0;
        }

        public String getLastInvokeTime() {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new java.util.Date(lastInvokeTime));
        }
    }
}
