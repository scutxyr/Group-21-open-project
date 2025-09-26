package com.taobao.arthas.core.command.model;

import java.util.List;

/**
 * 启动时间统计结果模型
 *
 * @author Huang Junhao
 */

public class StartupTimeModel extends ResultModel {

    private long startTime;
    private long uptime;
    private long currentTime;
    private String jvmName;
    private String jvmVersion;
    private String jvmVendor;
    private boolean detailed;
    private String format;

    private long processId;
    private List<String> inputArguments;
    private String classPath;
    private String libraryPath;

    @Override
    public String getType() {
        return "startup-time";
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public String getJvmName() {
        return jvmName;
    }

    public void setJvmName(String jvmName) {
        this.jvmName = jvmName;
    }

    public String getJvmVersion() {
        return jvmVersion;
    }

    public void setJvmVersion(String jvmVersion) {
        this.jvmVersion = jvmVersion;
    }

    public String getJvmVendor() {
        return jvmVendor;
    }

    public void setJvmVendor(String jvmVendor) {
        this.jvmVendor = jvmVendor;
    }

    public boolean isDetailed() {
        return detailed;
    }

    public void setDetailed(boolean detailed) {
        this.detailed = detailed;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public List<String> getInputArguments() {
        return inputArguments;
    }

    public void setInputArguments(List<String> inputArguments) {
        this.inputArguments = inputArguments;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getLibraryPath() {
        return libraryPath;
    }

    public void setLibraryPath(String libraryPath) {
        this.libraryPath = libraryPath;
    }
}