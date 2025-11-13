package com.taobao.arthas.core.command.config;

import com.taobao.arthas.common.config.ConfigConstants;
import com.taobao.arthas.core.command.model.ConfigCheckerModel;
import java.io.*;
import java.util.*;

/**
 * Simple Implementation of Config Checker
 */
public class SimpleConfigChecker {

    public ConfigCheckerModel check(File configDir, String profile, boolean checkSensitive) {
        ConfigCheckerModel result = new ConfigCheckerModel();
        result.setProfile(profile);
        result.setCheckTime(System.currentTimeMillis());

        if (!configDir.exists() || !configDir.isDirectory()) {
            return createError("Invalid config directory");
        }

        File[] files = configDir.listFiles((dir, name) ->
                name.endsWith(".properties") || name.endsWith(".yml") || name.endsWith(".yaml"));

        if (files == null || files.length == 0) {
            return createError("No config files found");
        }

        List<ConfigCheckerModel.FileReport> reports = new ArrayList<>();
        for (File file : files) {
            reports.add(checkFile(file, checkSensitive));
        }

        result.setFileReports(reports);
        result.setSummary(createSummary(reports));
        return result;
    }

    private ConfigCheckerModel.FileReport checkFile(File file, boolean checkSensitive) {
        String filename = file.getName();
        ConfigConstants.FileType fileType = filename.endsWith(".properties") ?
                ConfigConstants.FileType.PROPERTIES : ConfigConstants.FileType.YAML;

        ConfigCheckerModel.FileReport report = new ConfigCheckerModel.FileReport(filename, fileType);

        try {
            if (fileType == ConfigConstants.FileType.PROPERTIES) {
                checkProperties(file, report, checkSensitive);
            } else {
                checkYaml(file, report, checkSensitive);
            }
        } catch (Exception e) {
            report.addIssue(new ConfigCheckerModel.ConfigIssue(
                    ConfigConstants.IssueType.PARSE_ERROR, 0,
                    "Parse error: " + e.getMessage(), ConfigConstants.Severity.HIGH, null
            ));
        }

        return report;
    }

    private void checkProperties(File file, ConfigCheckerModel.FileReport report, boolean checkSensitive)
            throws IOException {
        Properties props = new Properties();
        try (FileReader reader = new FileReader(file)) {
            props.load(reader);
        }

        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);

            if (checkSensitive && isSensitive(key) && isExposed(value)) {
                report.addIssue(new ConfigCheckerModel.ConfigIssue(
                        ConfigConstants.IssueType.SENSITIVE_DATA, -1,
                        "Sensitive: " + key + " = " + mask(value),
                        ConfigConstants.Severity.HIGH, key
                ));
            }

            if (isRequired(key) && !props.containsKey(key)) {
                report.addIssue(new ConfigCheckerModel.ConfigIssue(
                        ConfigConstants.IssueType.MISSING_REQUIRED, -1,
                        "Missing: " + key, ConfigConstants.Severity.MEDIUM, key
                ));
            }
        }
    }

    private void checkYaml(File file, ConfigCheckerModel.FileReport report, boolean checkSensitive)
            throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                if (checkSensitive && line.contains(":") && isSensitive(line.toLowerCase())) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String value = parts[1].trim();
                        if (isExposed(value)) {
                            report.addIssue(new ConfigCheckerModel.ConfigIssue(
                                    ConfigConstants.IssueType.SENSITIVE_DATA, lineNum,
                                    "Sensitive: " + parts[0].trim() + " = " + mask(value),
                                    ConfigConstants.Severity.HIGH, parts[0].trim()
                            ));
                        }
                    }
                }
            }
        }
    }

    private boolean isSensitive(String text) {
        for (String key : ConfigConstants.SENSITIVE_KEYS) {
            if (text.contains(key)) return true;
        }
        return false;
    }

    private boolean isExposed(String value) {
        return value != null && !value.trim().isEmpty() &&
                !value.trim().startsWith("${") && value.length() > 3;
    }

    private boolean isRequired(String key) {
        for (String req : ConfigConstants.REQUIRED_PROPERTIES) {
            if (req.equals(key)) return true;
        }
        return false;
    }

    private String mask(String value) {
        return value.length() <= 4 ? "***" :
                value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }

    private ConfigCheckerModel createError(String message) {
        ConfigCheckerModel result = new ConfigCheckerModel();
        result.setCheckTime(System.currentTimeMillis());

        ConfigCheckerModel.FileReport error = new ConfigCheckerModel.FileReport("ERROR", ConfigConstants.FileType.UNKNOWN);
        error.addIssue(new ConfigCheckerModel.ConfigIssue(
                ConfigConstants.IssueType.SYSTEM_ERROR, 0, message, ConfigConstants.Severity.HIGH, null
        ));

        result.setFileReports(java.util.Arrays.asList(error));

        ConfigCheckerModel.CheckSummary summary = new ConfigCheckerModel.CheckSummary();
        summary.filesChecked = 1;
        summary.errors = 1;
        result.setSummary(summary);

        return result;
    }

    private ConfigCheckerModel.CheckSummary createSummary(List<ConfigCheckerModel.FileReport> reports) {
        ConfigCheckerModel.CheckSummary summary = new ConfigCheckerModel.CheckSummary();
        summary.filesChecked = reports.size();

        for (ConfigCheckerModel.FileReport report : reports) {
            switch (report.status) {
                case PASSED: summary.passed++; break;
                case WARNING: summary.warnings++; break;
                case ERROR: summary.errors++; break;
                case SECURITY_ISSUE: summary.securityIssues++; break;
            }
        }

        return summary;
    }
}
