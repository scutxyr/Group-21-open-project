package com.taobao.arthas.common.config;

/**
 * Config Checker Constants Define
 */
public final class ConfigConstants {

    private ConfigConstants() {}

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum IssueType {
        UNSUPPORTED_FORMAT,
        PARSE_ERROR,
        SENSITIVE_DATA,
        MISSING_REQUIRED,
        SYNTAX_ERROR,
        SYSTEM_ERROR
    }

    public enum FileType {
        PROPERTIES, YAML, JSON, XML, UNKNOWN
    }

    public enum CheckStatus {
        PASSED, WARNING, ERROR, SECURITY_ISSUE
    }

    public static final String[] SENSITIVE_KEYS = {
            "password", "pwd", "secret", "key", "token",
            "credential", "auth", "private", "certificate", "ssl"
    };

    public static final String[] REQUIRED_PROPERTIES = {
            "server.port", "spring.application.name"
    };

    public static final String DEFAULT_CONFIG_DIR = "./config";
    public static final String DEFAULT_PROFILE = "default";
    public static final String DEFAULT_FORMAT = "text";
}
