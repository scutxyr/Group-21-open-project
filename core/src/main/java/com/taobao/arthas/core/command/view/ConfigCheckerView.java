package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.ConfigCheckerModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.common.config.ConfigConstants;

/**
 * Config Checker View
 */
public class ConfigCheckerView extends ResultView<ConfigCheckerModel> {

    @Override
    public void draw(CommandProcess process, ConfigCheckerModel result) {
        if ("json".equals(result.getFormat())) {
            drawJsonFormat(process, result);
        } else {
            drawTextFormat(process, result);
        }
    }

    private void drawTextFormat(CommandProcess process, ConfigCheckerModel result) {
        StringBuilder output = new StringBuilder();

        output.append("Configuration Check Results\n");
        output.append("===========================\n\n");

        for (ConfigCheckerModel.FileReport fileReport : result.getFileReports()) {
            output.append("File: ").append(fileReport.filename).append("\n");
            output.append("Type: ").append(fileReport.fileType).append("\n");
            output.append("Status: ").append(fileReport.status).append("\n");
            output.append("-----------------------------------\n");

            if (fileReport.issues.isEmpty()) {
                output.append("✓ No issues found\n");
            } else {
                for (ConfigCheckerModel.ConfigIssue issue : fileReport.issues) {
                    output.append(getSeverityPrefix(issue.severity))
                            .append(" ").append(issue.severity)
                            .append(": ").append(issue.message).append("\n");
                }
            }
            output.append("\n");
        }

        ConfigCheckerModel.CheckSummary summary = result.getSummary();
        output.append("Summary:\n");
        output.append("--------\n");
        output.append("Total files checked: ").append(summary.filesChecked).append("\n");
        output.append("Passed: ").append(summary.passed).append("\n");
        output.append("Warnings: ").append(summary.warnings).append("\n");
        output.append("Errors: ").append(summary.errors).append("\n");
        output.append("Security issues: ").append(summary.securityIssues).append("\n");

        process.write(output.toString());
    }

    private void drawJsonFormat(CommandProcess process, ConfigCheckerModel result) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"summary\": {\n");
        json.append("    \"filesChecked\": ").append(result.getSummary().filesChecked).append(",\n");
        json.append("    \"passed\": ").append(result.getSummary().passed).append(",\n");
        json.append("    \"warnings\": ").append(result.getSummary().warnings).append(",\n");
        json.append("    \"errors\": ").append(result.getSummary().errors).append(",\n");
        json.append("    \"securityIssues\": ").append(result.getSummary().securityIssues).append("\n");
        json.append("  }\n");
        json.append("}\n");
        process.write(json.toString());
    }

    private String getSeverityPrefix(ConfigConstants.Severity severity) {
        switch (severity) {
            case CRITICAL: return "[CRITICAL]";
            case HIGH: return "[HIGH]";
            case MEDIUM: return "[MEDIUM]";
            case LOW: return "[LOW]";
            default: return "[INFO]";
        }
    }
}
