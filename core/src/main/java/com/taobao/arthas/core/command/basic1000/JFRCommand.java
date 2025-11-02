package com.taobao.arthas.core.command.basic1000;

import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Argument;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Option;
import com.taobao.middleware.cli.annotations.Summary;

/**
 * JFR command disabled for Java 8 environment
 */
@Name("jfr")
@Summary("Java Flight Recorder Command (disabled in Java 8)")
@Description("JFR functionality requires Java 9 or higher. " +
        "This command is disabled in Java 8 environment.\n" +
        "Please upgrade to Java 9 or higher to use JFR features.")
public class JFRCommand extends AnnotatedCommand {

    private String cmd;

    @Argument(index = 0, argName = "cmd", required = false)
    @Description("command name (start status stop dump) - disabled in Java 8")
    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    @Option(shortName = "n", longName = "name")
    @Description("Name that can be used to identify recording - disabled in Java 8")
    public void setName(String name) {
        // Ignored in Java 8
    }

    @Option(shortName = "s", longName = "settings")
    @Description("Settings file(s) - disabled in Java 8")
    public void setSettings(String settings) {
        // Ignored in Java 8
    }

    @Option(longName = "dumponexit")
    @Description("Dump running recording when JVM shuts down - disabled in Java 8")
    public void setDumpOnExit(Boolean dumpOnExit) {
        // Ignored in Java 8
    }

    @Option(shortName = "d", longName = "delay")
    @Description("Delay recording start - disabled in Java 8")
    public void setDelay(String delay) {
        // Ignored in Java 8
    }

    @Option(longName = "duration")
    @Description("Duration of recording - disabled in Java 8")
    public void setDuration(String duration) {
        // Ignored in Java 8
    }

    @Option(shortName = "f", longName = "filename")
    @Description("Resulting recording filename - disabled in Java 8")
    public void setFilename(String filename) {
        // Ignored in Java 8
    }

    @Option(longName = "maxage")
    @Description("Maximum time to keep recorded data - disabled in Java 8")
    public void setMaxAge(String maxAge) {
        // Ignored in Java 8
    }

    @Option(longName = "maxsize")
    @Description("Maximum amount of bytes to keep - disabled in Java 8")
    public void setMaxSize(String maxSize) {
        // Ignored in Java 8
    }

    @Option(shortName = "r", longName = "recording")
    @Description("Recording number - disabled in Java 8")
    public void setRecording(Long recording) {
        // Ignored in Java 8
    }

    @Option(longName = "state")
    @Description("Query recordings by state - disabled in Java 8")
    public void setState(String state) {
        // Ignored in Java 8
    }

    @Override
    public void process(CommandProcess process) {
        process.write("JFR command is not available in Java 8 environment.\n");
        process.write("Java Flight Recorder (JFR) requires Java 9 or higher.\n");
        process.write("Please upgrade your Java version to use JFR features.\n");
        process.end();
    }
}
