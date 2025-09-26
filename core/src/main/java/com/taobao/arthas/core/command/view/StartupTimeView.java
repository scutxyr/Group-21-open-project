package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.StartupTimeModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.ui.LabelElement;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 启动时间统计视图
 *
 * @author Huang Junhao
 */
public class StartupTimeView extends ResultView<StartupTimeModel> {

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

        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
        table.row(new LabelElement("🚀 Application Startup Time Statistics").style(Decoration.bold.fg(Color.cyan)));
        table.row(new LabelElement("=" + "=".repeat(45)));
        process.write(RenderUtil.render(table, process.width()) + "\n");

        TableElement infoTable = new TableElement(2, 1).leftCellPadding(1).rightCellPadding(1);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTimeStr = sdf.format(new Date(result.getStartTime()));
        String currentTimeStr = sdf.format(new Date(result.getCurrentTime()));
        String uptimeStr = formatDuration(result.getUptime());

        infoTable.row("Start Time:", new LabelElement(startTimeStr).style(Decoration.fg(Color.green)));
        infoTable.row("Current Time:", currentTimeStr);
        infoTable.row("Uptime:", new LabelElement(uptimeStr).style(Decoration.bold.fg(Color.yellow)));
        infoTable.row("", "");
        infoTable.row("JVM Name:", result.getJvmName());
        infoTable.row("JVM Version:", result.getJvmVersion());
        infoTable.row("JVM Vendor:", result.getJvmVendor());

        if (result.getProcessId() > 0) {
            infoTable.row("Process ID:", String.valueOf(result.getProcessId()));
        }

        process.write(RenderUtil.render(infoTable, process.width()) + "\n");

        if (result.isDetailed()) {
            drawDetailedInfo(process, result);
        }

        drawUptimeAnalysis(process, result);
    }

    private void drawDetailedInfo(CommandProcess process, StartupTimeModel result) {
        process.write("\n");
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
        table.row(new LabelElement("📋 Detailed Information").style(Decoration.bold.fg(Color.magenta)));

        List<String> args = result.getInputArguments();
        if (args != null && !args.isEmpty()) {
            table.row("");
            table.row(new LabelElement("JVM Arguments:").style(Decoration.bold));
            for (int i = 0; i < Math.min(args.size(), 10); i++) {
                table.row("  " + (i + 1) + ". " + args.get(i));
            }
            if (args.size() > 10) {
                table.row("  ... and " + (args.size() - 10) + " more arguments");
            }
        }

        process.write(RenderUtil.render(table, process.width()) + "\n");
    }

    private void drawUptimeAnalysis(CommandProcess process, StartupTimeModel result) {
        process.write("\n");
        TableElement table = new TableElement(2, 1).leftCellPadding(1).rightCellPadding(1);
        table.row(new LabelElement("⏱️ Uptime Analysis").style(Decoration.bold.fg(Color.blue)), "");

        long uptimeMs = result.getUptime();
        long days = uptimeMs / (24 * 60 * 60 * 1000);
        long hours = (uptimeMs % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (uptimeMs % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (uptimeMs % (60 * 1000)) / 1000;

        table.row("Total Uptime:", formatDuration(uptimeMs));
        table.row("Days:", String.valueOf(days));
        table.row("Hours:", String.valueOf(hours));
        table.row("Minutes:", String.valueOf(minutes));
        table.row("Seconds:", String.valueOf(seconds));

        String status;
        Color statusColor;
        if (uptimeMs < 60 * 1000) { // < 1分钟
            status = "Just Started";
            statusColor = Color.yellow;
        } else if (uptimeMs < 60 * 60 * 1000) { // < 1小时
            status = "Recently Started";
            statusColor = Color.green;
        } else if (uptimeMs < 24 * 60 * 60 * 1000) { // < 1天
            status = "Running Stable";
            statusColor = Color.green;
        } else {
            status = "Long Running";
            statusColor = Color.cyan;
        }

        table.row("Status:", new LabelElement(status).style(Decoration.bold.fg(statusColor)));

        process.write(RenderUtil.render(table, process.width()) + "\n");
    }

    private void drawJsonFormat(CommandProcess process, StartupTimeModel result) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"startTime\": ").append(result.getStartTime()).append(",\n");
        json.append("  \"currentTime\": ").append(result.getCurrentTime()).append(",\n");
        json.append("  \"uptime\": ").append(result.getUptime()).append(",\n");
        json.append("  \"uptimeFormatted\": \"").append(formatDuration(result.getUptime())).append("\",\n");
        json.append("  \"jvmName\": \"").append(result.getJvmName()).append("\",\n");
        json.append("  \"jvmVersion\": \"").append(result.getJvmVersion()).append("\",\n");
        json.append("  \"jvmVendor\": \"").append(result.getJvmVendor()).append("\"");

        if (result.getProcessId() > 0) {
            json.append(",\n  \"processId\": ").append(result.getProcessId());
        }

        json.append("\n}\n");
        process.write(json.toString());
    }

    private String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        }

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return seconds + "s";
        }
    }
}