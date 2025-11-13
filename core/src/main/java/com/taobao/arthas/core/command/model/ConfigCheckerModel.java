package com.taobao.arthas.core.command.model;

import com.taobao.arthas.common.config.ConfigConstants;
import java.util.ArrayList;
import java.util.List;

/**
 * Config Checker Result Model
 */
public class ConfigCheckerModel extends ResultModel {
    private String profile;
    private long checkTime;
    private List<FileReport> fileReports = new ArrayList<>();
    private CheckSummary summary;
    private String format;

    @Override
    public String getType() {
        return "config-checker";
    }

    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }
    public long getCheckTime() { return checkTime; }
    public void setCheckTime(long checkTime) { this.checkTime = checkTime; }
    public List<FileReport> getFileReports() { return fileReports; }
    public void setFileReports(List<FileReport> fileReports) { this.fileReports = fileReports; }
    public CheckSummary getSummary() { return summary; }
    public void setSummary(CheckSummary summary) { this.summary = summary; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public static class FileReport {
        public String filename;
        public ConfigConstants.FileType fileType;
        public ConfigConstants.CheckStatus status;
        public List<ConfigIssue> issues = new ArrayList<>();

        public FileReport(String filename, ConfigConstants.FileType fileType) {
            this.filename = filename;
            this.fileType = fileType;
            this.status = ConfigConstants.CheckStatus.PASSED;
        }

        public void addIssue(ConfigIssue issue) {
            this.issues.add(issue);
            if (issue.severity == ConfigConstants.Severity.HIGH ||
                    issue.severity == ConfigConstants.Severity.CRITICAL) {
                this.status = ConfigConstants.CheckStatus.SECURITY_ISSUE;
            } else if (issue.severity == ConfigConstants.Severity.MEDIUM &&
                    this.status != ConfigConstants.CheckStatus.SECURITY_ISSUE) {
                this.status = ConfigConstants.CheckStatus.ERROR;
            } else if (this.status == ConfigConstants.CheckStatus.PASSED) {
                this.status = ConfigConstants.CheckStatus.WARNING;
            }
        }
    }

    public static class ConfigIssue {
        public ConfigConstants.IssueType type;
        public int line;
        public String message;
        public ConfigConstants.Severity severity;
        public String property;

        public ConfigIssue(ConfigConstants.IssueType type, int line, String message,
                           ConfigConstants.Severity severity, String property) {
            this.type = type;
            this.line = line;
            this.message = message;
            this.severity = severity;
            this.property = property;
        }
    }

    public static class CheckSummary {
        public int filesChecked;
        public int passed;
        public int warnings;
        public int errors;
        public int securityIssues;
    }
}
