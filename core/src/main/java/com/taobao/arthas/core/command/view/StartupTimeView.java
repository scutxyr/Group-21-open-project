
package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.StartupTimeModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.text.Decoration;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.text.SimpleDateFormat;
import com.taobao.arthas.core.command.constant.StartupTimeConstants;
import java.util.Date;
import java.util.List;

import static com.taobao.text.ui.Element.label;

/**
 * 启动时间统计视图
 *
 * @author Huang Junhao
 */
public class StartupTimeView extends ResultView<StartupTimeModel> {
    private static final int ARGUMENT_SIZE = 10;

    @Override
    public void draw(CommandProcess process, StartupTimeModel result) {
        if (result == null) {
            process.write("No startup time information available.\n");
            return;
        }

        if ("json".equals(result.getFormat())) {
            drawJsonFormat(process, result);
        } else {
            drawHumanFormat(process, result);
        }
    }

    private void drawHumanFormat(CommandProcess process, StartupTimeModel result) {
        TableElement table = new TableElement(2, 1).leftCellPadding(1).rightCellPadding(1);

        table.row(true, label("Application Startup Time Statistics").style(Decoration.bold.bold()));
        table.row("", "");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTimeStr = sdf.format(new Date(result.getStartTime()));
        String currentTimeStr = sdf.format(new Date(result.getCurrentTime()));
        String uptimeStr = formatDuration(result.getUptime());

        table.row("Start Time:", startTimeStr);
        table.row("Current Time:", currentTimeStr);
        table.row("Uptime:", uptimeStr);
        table.row("", "");
        table.row("JVM Name:", result.getJvmName());
        table.row("JVM Version:", result.getJvmVersion());
        table.row("JVM Vendor:", result.getJvmVendor());

        if (result.getProcessId() > 0) {
            table.row("Process ID:", String.valueOf(result.getProcessId()));
        }

        process.write(RenderUtil.render(table, process.width()) + "\n");

        if (result.isDetailed()) {
            drawDetailedInfo(process, result);
        }

        drawUptimeAnalysis(process, result);
    }

    private void drawDetailedInfo(CommandProcess process, StartupTimeModel result) {
        process.write("\n");
        TableElement table = new TableElement(1, 1).leftCellPadding(1).rightCellPadding(1);
        table.row(true, label("Detailed Information").style(Decoration.bold.bold()));

        List<String> args = result.getInputArguments();
        if (args != null && !args.isEmpty()) {
            table.row("");
            table.row("JVM Arguments:");
            for (int i = 0; i < Math.min(args.size(), ARGUMENT_SIZE); i++) {
                table.row("  " + (i + 1) + ". " + args.get(i));
            }
            if (args.size() > ARGUMENT_SIZE) {
                table.row("  ... and " + (args.size() - ARGUMENT_SIZE) + " more arguments");
            }
        }

        process.write(RenderUtil.render(table, process.width()) + "\n");
    }

    private void drawUptimeAnalysis(CommandProcess process, StartupTimeModel result) {
        process.write("\n");
        TableElement table = new TableElement(2, 1).leftCellPadding(1).rightCellPadding(1);
        table.row(true, label("Uptime Analysis").style(Decoration.bold.bold()));

        long uptimeMs = result.getUptime();
        long days = uptimeMs / (StartupTimeConstants.SECONDS_PER_DAY * StartupTimeConstants.DECIMAL_BASE);
        long hours = (uptimeMs % (StartupTimeConstants.SECONDS_PER_DAY * StartupTimeConstants.DECIMAL_BASE))
                / (StartupTimeConstants.SECONDS_PER_HOUR * StartupTimeConstants.DECIMAL_BASE);
        long minutes = (uptimeMs % (StartupTimeConstants.SECONDS_PER_HOUR * StartupTimeConstants.DECIMAL_BASE))
                / (StartupTimeConstants.MILLISECONDS_PER_MINUTE);
        long seconds = (uptimeMs % (StartupTimeConstants.MILLISECONDS_PER_MINUTE)) / StartupTimeConstants.DECIMAL_BASE;

        table.row("Total Uptime:", formatDuration(uptimeMs));
        table.row("Days:", String.valueOf(days));
        table.row("Hours:", String.valueOf(hours));
        table.row("Minutes:", String.valueOf(minutes));
        table.row("Seconds:", String.valueOf(seconds));
        table.row("", "");

        String status;
        if (uptimeMs < StartupTimeConstants.MILLISECONDS_PER_MINUTE) {
            status = "Just Started";
        } else if (uptimeMs < StartupTimeConstants.SECONDS_PER_HOUR * StartupTimeConstants.DECIMAL_BASE) {
            status = "Recently Started";
        } else if (uptimeMs < StartupTimeConstants.SECONDS_PER_DAY * StartupTimeConstants.DECIMAL_BASE) {
            status = "Running Stable";
        } else {
            status = "Long Running";
        }
        table.row("Status:", status);

        process.write(RenderUtil.render(table, process.width()) + "\n");
    }

    private void drawJsonFormat(CommandProcess process, StartupTimeModel result) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"startTime\": ").append(result.getStartTime()).append(",\n");
        json.append("  \"currentTime\": ").append(result.getCurrentTime()).append(",\n");
        json.append("  \"uptime\": ").append(result.getUptime()).append(",\n");
        json.append("  \"jvmName\": \"").append(result.getJvmName()).append("\",\n");
        json.append("  \"jvmVersion\": \"").append(result.getJvmVersion()).append("\",\n");
        json.append("  \"jvmVendor\": \"").append(result.getJvmVendor()).append("\",\n");
        json.append("  \"processId\": ").append(result.getProcessId()).append("\n");
        json.append("}\n");
        process.write(json.toString());
    }

    private String formatDuration(long millis) {
        if (millis < StartupTimeConstants.DECIMAL_BASE) {
            return millis + "ms";
        } else if (millis < StartupTimeConstants.MILLISECONDS_PER_MINUTE) {
            long seconds = millis / StartupTimeConstants.DECIMAL_BASE;
            return seconds + "s";
        } else if (millis < StartupTimeConstants.SECONDS_PER_HOUR * StartupTimeConstants.DECIMAL_BASE) {
            long minutes = millis / (StartupTimeConstants.MILLISECONDS_PER_MINUTE);
            long seconds = (millis % (StartupTimeConstants.MILLISECONDS_PER_MINUTE))
                    / StartupTimeConstants.DECIMAL_BASE;
            return minutes + "m " + seconds + "s";
        } else if (millis < StartupTimeConstants.SECONDS_PER_DAY * StartupTimeConstants.DECIMAL_BASE) {
            long hours = millis / (StartupTimeConstants.SECONDS_PER_HOUR * StartupTimeConstants.DECIMAL_BASE);
            long minutes = (millis % (StartupTimeConstants.SECONDS_PER_HOUR * StartupTimeConstants.DECIMAL_BASE))
                    / (StartupTimeConstants.MILLISECONDS_PER_MINUTE);
            return hours + "h " + minutes + "m";
        } else {
            long days = millis / (StartupTimeConstants.SECONDS_PER_DAY * StartupTimeConstants.DECIMAL_BASE);
            long hours = (millis % (StartupTimeConstants.SECONDS_PER_DAY * StartupTimeConstants.DECIMAL_BASE))
                    / (StartupTimeConstants.SECONDS_PER_HOUR * StartupTimeConstants.DECIMAL_BASE);
            return days + "d " + hours + "h";
        }
    }
}
