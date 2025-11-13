package com.taobao.arthas.core.shell.command.impl;

import com.taobao.arthas.core.command.config.SimpleConfigChecker;
import com.taobao.arthas.core.command.model.ConfigCheckerModel;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Summary;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Option;

import java.io.File;

import static com.taobao.arthas.common.config.ConfigConstants.*;

/**
 * Config Checker Command
 */
@Name("config-checker")
@Summary("Check configuration files for syntax, security and completeness")
@Description("Configuration file checker command")
public class ConfigCheckerCommand extends AnnotatedCommand {

    private String configDir = DEFAULT_CONFIG_DIR;
    private boolean checkSensitive = false;
    private String format = DEFAULT_FORMAT;

    @Override
    public void process(CommandProcess process) {
        try {
            File dir = new File(configDir);
            ConfigCheckerModel result = new SimpleConfigChecker().check(dir, "default", checkSensitive);
            result.setFormat(format);
            process.appendResult(result);
            process.end();
        } catch (Exception e) {
            process.end(1, "Check failed: " + e.getMessage());
        }
    }

    @Option(shortName = "c", longName = "config-dir")
    @Description("Configuration directory path")
    public void setConfigDir(String configDir) {
        this.configDir = configDir;
    }

    @Option(shortName = "s", longName = "check-sensitive", flag = true)
    @Description("Check for sensitive information")
    public void setCheckSensitive(boolean checkSensitive) {
        this.checkSensitive = checkSensitive;
    }

    @Option(shortName = "o", longName = "output")
    @Description("Output format: text or json")
    public void setFormat(String format) {
        this.format = format;
    }
}
