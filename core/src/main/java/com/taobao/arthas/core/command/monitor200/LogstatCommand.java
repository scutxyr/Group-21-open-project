package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.GlobalOptions;
import com.taobao.arthas.core.advisor.AdviceListener;
import com.taobao.arthas.core.command.Constants;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.SearchUtils;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.util.matcher.Matcher;
import com.taobao.middleware.cli.annotations.Argument;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Option;
import com.taobao.middleware.cli.annotations.Summary;

import java.util.Arrays;
import java.util.List;

/**
 * logstat command — statistics of WARN/ERROR logger calls and their source methods.
 *
 * Usage:
 *   logstat [class-pattern] --level ERROR|WARN -n 100
 *
 * If class-pattern omitted, default to common logger internal classes (logback/log4j2/slf4j adapters).
 */
@Name("logStat")
@Summary("Count WARN/ERROR logging calls and show their calling methods.")
public class LogstatCommand extends EnhancerCommand {

    private String classPattern;
    private String level = "ERROR"; // default
    private int limit = 100;

    @Argument(argName = "class-pattern", index = 0, required = false)
    @Description("Class name pattern to hook (use '.' or '/' as separator). If omitted, common logger classes will be used.")
    public void setClassPattern(String classPattern) {
        this.classPattern = StringUtils.normalizeClassName(classPattern);
    }

    @Option(longName = "level")
    @Description("Log level to capture: ERROR or WARN (WARN will include WARN+ERROR). Default ERROR.")
    public void setLevel(String level) {
        if (level != null) {
            this.level = level.toUpperCase();
        }
    }

    @Option(shortName = "n", longName = "limit")
    @Description("Max number of top sources to print for summary commands (not used for every single write).")
    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getClassPattern() {
        return classPattern;
    }

    public String getLevel() {
        return level;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    protected Matcher getClassNameMatcher() {
        if (classNameMatcher == null) {
            if (classPattern == null || classPattern.trim().isEmpty()) {
                // default logger internals
                List<String> defaults = Arrays.asList(
                        "ch.qos.logback.classic.Logger",
                        "org.apache.logging.log4j.core.Logger",
                        "org.slf4j.Logger"
                );
                classNameMatcher = SearchUtils.classNameMatcher(String.join("|", defaults), true);
            } else {
                // NOTE: EnhancerCommand does not expose isRegEx() in your version, so use wildcard by default
                classNameMatcher = SearchUtils.classNameMatcher(getClassPattern(), false);
            }
        }
        return classNameMatcher;
    }

    @Override
    protected Matcher getClassNameExcludeMatcher() {
        // reuse field from EnhancerCommand (getExcludeClassPattern)
        if (classNameExcludeMatcher == null && getExcludeClassPattern() != null) {
            classNameExcludeMatcher = SearchUtils.classNameMatcher(getExcludeClassPattern(), false);
        }
        return classNameExcludeMatcher;
    }

    @Override
    protected Matcher getMethodNameMatcher() {
        // we want to instrument internal logging entry methods; match common method names
        if (methodNameMatcher == null) {
            // We'll instrument broad methods - enhancer will try to apply; actual filtering by stacktrace happens in listener
            methodNameMatcher = SearchUtils.classNameMatcher("*", false);
        }
        return methodNameMatcher;
    }

    @Override
    protected AdviceListener getAdviceListener(CommandProcess process) {
        boolean verbose = GlobalOptions.verbose || this.verbose;
        return new LogstatAdviceListener(this, process, verbose);
    }


    public String summary() {
        return "logstat — count WARN/ERROR logger calls and print their caller methods.";
    }


    public String getHelp() {
        return Constants.EXPRESS_DESCRIPTION + "\nExamples:\n" +
                "  logstat --level ERROR\n" +
                "  logstat ch.qos.logback.classic.Logger --level WARN -n 50\n";
    }
}
