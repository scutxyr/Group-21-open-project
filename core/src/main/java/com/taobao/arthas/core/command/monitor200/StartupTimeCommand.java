package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.command.model.StartupTimeModel;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Option;
import com.taobao.middleware.cli.annotations.Summary;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * 应用启动时间统计命令
 *
 * @author Huang Junhao
 */

@Name("startup-time")
@Summary("Display JVM startup time and application uptime statistics")
@Description("Show detailed information about JVM startup time, application uptime, and related statistics.\n" +
        "\nExamples:\n" +
        "  startup-time                    # Show basic startup information\n" +
        "  startup-time -d                 # Show detailed information\n" +
        "  startup-time --format human     # Human readable format")
public class StartupTimeCommand extends AnnotatedCommand {

    @Option(shortName = "d", longName = "detailed")
    @Description("Show detailed startup information")
    private boolean detailed = false;

    @Option(longName = "format")
    @Description("Output format: human, json (default: human)")
    private String format = "human";

    @Override
    public void process(CommandProcess process) {
        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

            long startTime = runtimeBean.getStartTime();
            long uptime = runtimeBean.getUptime();
            long currentTime = System.currentTimeMillis();

            String jvmName = runtimeBean.getVmName();
            String jvmVersion = runtimeBean.getVmVersion();
            String jvmVendor = runtimeBean.getVmVendor();

            StartupTimeModel model = new StartupTimeModel();
            model.setStartTime(startTime);
            model.setUptime(uptime);
            model.setCurrentTime(currentTime);
            model.setJvmName(jvmName);
            model.setJvmVersion(jvmVersion);
            model.setJvmVendor(jvmVendor);
            model.setDetailed(detailed);
            model.setFormat(format);

            if (detailed) {
                model.setProcessId(getPid());
                model.setInputArguments(runtimeBean.getInputArguments());
                model.setClassPath(runtimeBean.getClassPath());
                model.setLibraryPath(runtimeBean.getLibraryPath());
            }

            process.appendResult(model);

        } catch (Exception e) {
            process.write("Error getting startup time information: " + e.getMessage() + "\n");
        }
    }

    private long getPid() {
        try {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            return Long.parseLong(name.split("@")[0]);
        } catch (Exception e) {
            return -1;
        }
    }
}