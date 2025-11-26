package com.taobao.arthas.core.shell.command.biz;

import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Option;
import com.taobao.middleware.cli.annotations.Summary;
import java.util.Map;

@Name("bitzstats")
@Summary("Statistics for business method calls")
@Description("Used to count business method calls, including call count, average time and other information")
public class BitzStatsCommand extends AnnotatedCommand {

    private boolean help;
    private boolean reset;
    private Integer top = 10;

    @Option(shortName = "h", longName = "help", flag = true)
    @Description("Show help information")
    public void setHelp(boolean help) {
        this.help = help;
    }

    @Option(shortName = "r", longName = "reset", flag = true)
    @Description("Reset statistics")
    public void setReset(boolean reset) {
        this.reset = reset;
    }

    @Option(shortName = "t", longName = "top")
    @Description("Show top N most frequently called methods, default: 10")
    public void setTop(Integer top) {
        this.top = top;
    }

    @Override
    public void process(CommandProcess process) {
        try {
            if (help) {
                showHelp(process);
                return;
            }

            if (reset) {
                resetStatistics(process);
                return;
            }

            showBusinessStats(process, top);

        } catch (Exception e) {
            process.write("bitzstats command error: " + e.getMessage() + "\n");
        } finally {
            process.end();
        }
    }

    private void showHelp(CommandProcess process) {
        StringBuilder help = new StringBuilder();
        help.append("bitzstats - Business method call statistics tool\n");
        help.append("USAGE:\n");
        help.append("  bitzstats                    # Show business statistics\n");
        help.append("  bitzstats -r                 # Reset statistics\n");
        help.append("  bitzstats -t 5               # Show top 5 most called methods\n");
        help.append("  bitzstats -h                 # Show help\n");
        help.append("EXAMPLES:\n");
        help.append("  bitzstats\n");
        help.append("  bitzstats -r\n");
        help.append("  bitzstats -t 20\n");
        process.write(help.toString());
    }

    private void resetStatistics(CommandProcess process) {
        BusinessStatsCollector.reset();
        process.write("Business statistics reset\n");
    }

    private void showBusinessStats(CommandProcess process, int topN) {
        Map<String, BusinessStatsCollector.BusinessMethodStats> stats = BusinessStatsCollector.getStats();

        if (stats.isEmpty()) {
            process.write("No business method call statistics available\n");
            process.write("Hint: Need to use monitor command to monitor business methods first\n");
            return;
        }

        StringBuilder result = new StringBuilder();
        result.append("Business Method Call Statistics (Top ").append(topN).append(")\n");
        result.append("==========================================\n");

        // Sort by call count and show top N
        stats.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().getInvokeCount(), e1.getValue().getInvokeCount()))
                .limit(topN)
                .forEach(entry -> {
                    BusinessStatsCollector.BusinessMethodStats methodStats = entry.getValue();
                    result.append(String.format("Method: %s\n", entry.getKey()));
                    result.append(String.format("  Call count: %d\n", methodStats.getInvokeCount()));
                    result.append(String.format("  Average time: %.2fms\n", methodStats.getAverageTime()));
                    result.append(String.format("  Last call: %s\n", methodStats.getLastInvokeTime()));
                    result.append("------------------------------------------\n");
                });

        process.write(result.toString());
    }
}
