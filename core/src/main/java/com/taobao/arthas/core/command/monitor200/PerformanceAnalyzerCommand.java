
package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.command.model.PerformanceModel;
import com.taobao.arthas.core.command.constant.PerformanceAnalyzerConstants;
import java.lang.management.*;
import java.util.List;

/**
 * 性能分析工具类 用于收集、分析和评估JVM性能指标
 *
 * @author Huang Junhao
 */
public class PerformanceAnalyzerCommand {

    private static final double PERCENTAGE = 100;

    private static final int BINARY_BASE = 1024;

    private static final double BINARY_BASE_DOUBLE = 1024.0;

    public static PerformanceModel collectPerformanceData() {
        PerformanceModel perfData = new PerformanceModel();

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;
        double usagePercent = (double) usedMemory / maxMemory * PERCENTAGE;

        perfData.setTotalMemory(totalMemory);
        perfData.setFreeMemory(freeMemory);
        perfData.setMaxMemory(maxMemory);
        perfData.setUsedMemory(usedMemory);
        perfData.setMemoryUsagePercent(usagePercent);

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        perfData.setThreadCount(threadBean.getThreadCount());
        perfData.setPeakThreadCount(threadBean.getPeakThreadCount());
        perfData.setDaemonThreadCount(threadBean.getDaemonThreadCount());

        long gcCount = 0;
        long gcTime = 0;
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            long count = gcBean.getCollectionCount();
            long time = gcBean.getCollectionTime();
            if (count > 0)
                gcCount += count;
            if (time > 0)
                gcTime += time;
        }
        perfData.setGcCount(gcCount);
        perfData.setGcTime(gcTime);

        ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
        perfData.setLoadedClassCount(classBean.getLoadedClassCount());
        perfData.setTotalLoadedClassCount(classBean.getTotalLoadedClassCount());
        perfData.setUnloadedClassCount(classBean.getUnloadedClassCount());

        perfData.setAvailableProcessors(runtime.availableProcessors());

        return perfData;
    }

    public static void calculatePerformanceScore(PerformanceModel perfData) {
        int score = PerformanceAnalyzerConstants.INITIAL_PERCENTAGE;

        double memUsage = perfData.getMemoryUsagePercent();
        if (memUsage > PerformanceAnalyzerConstants.MEMORY_USAGE_CRITICAL_THRESHOLD) {
            score -= PerformanceAnalyzerConstants.MEMORY_USAGE_CRITICAL_PUNISH;
            perfData.getIssues().add("Memory usage is critically high (" + String.format("%.1f", memUsage) + "%)");
            perfData.getRecommendations().add("Consider increasing heap size with -Xmx parameter");
        } else if (memUsage > PerformanceAnalyzerConstants.MEMORY_USAGE_HIGH_THRESHOLD) {
            score -= PerformanceAnalyzerConstants.MEMORY_USAGE_HIGH_PUNISH;
            perfData.getIssues().add("Memory usage is high (" + String.format("%.1f", memUsage) + "%)");
            perfData.getRecommendations().add("Monitor memory usage and consider optimization");
        } else if (memUsage > PerformanceAnalyzerConstants.MEMORY_USAGE_WARNING_THRESHOLD) {
            score -= PerformanceAnalyzerConstants.MEMORY_USAGE_WARNING_PUNISH;
        }

        long gcCount = perfData.getGcCount();
        long gcTime = perfData.getGcTime();
        if (gcCount > PerformanceAnalyzerConstants.GARBAGE_COLLECTION_CRITICAL_THRESHOLD) {
            score -= PerformanceAnalyzerConstants.GARBAGE_COLLECTION_CRITICAL_PUNISH;
            perfData.getIssues().add("High GC frequency (" + gcCount + " collections)");
            perfData.getRecommendations().add("Review object creation patterns and memory allocation");
        } else if (gcCount > PerformanceAnalyzerConstants.GARBAGE_COLLECTION_HIGH_THRESHOLD) {
            score -= PerformanceAnalyzerConstants.GARBAGE_COLLECTION_HIGH_PUNISH;
        } else if (gcCount > PerformanceAnalyzerConstants.GARBAGE_COLLECTION_WARNING_THRESHOLD) {
            score -= PerformanceAnalyzerConstants.GARBAGE_COLLECTION_WARNING_PUNISH;
        }

        if (gcTime > PerformanceAnalyzerConstants.GARBAGE_COLLECTION_TIME_THRESHOLD) {
            score -= PerformanceAnalyzerConstants.GARBAGE_COLLECTION_TIME_PUNISH;
            perfData.getIssues().add("High GC time (" + gcTime + "ms)");
        }

        int threadCount = perfData.getThreadCount();
        if (threadCount > PerformanceAnalyzerConstants.THREAD_HIGH_THRESHOLD) {
            score -= PerformanceAnalyzerConstants.THREAD_HIGH_PUNISH;
            perfData.getIssues().add("Very high thread count (" + threadCount + ")");
            perfData.getRecommendations().add("Review thread pool configurations");
        } else if (threadCount > PerformanceAnalyzerConstants.THREAD_WARNING_THRESHOLD) {
            score -= PerformanceAnalyzerConstants.THREAD_WARNING_PUNISH;
            perfData.getIssues().add("High thread count (" + threadCount + ")");
        }

        long loadedClasses = perfData.getLoadedClassCount();
        if (loadedClasses > PerformanceAnalyzerConstants.LOADED_CLASSES_HIGH_THRESHOLD) {
            score -= PerformanceAnalyzerConstants.LOADED_CLASSES_HIGH_PUNISH;
            perfData.getIssues().add("Large number of loaded classes (" + loadedClasses + ")");
            perfData.getRecommendations().add("Consider reducing dependencies or using lazy loading");
        } else if (loadedClasses > PerformanceAnalyzerConstants.LOADED_CLASSES_WARNING_THRESHOLD) {
            score -= PerformanceAnalyzerConstants.LOADED_CLASSES_WARNING_PUNISH;
        }

        score = Math.max(0, score);
        perfData.setPerformanceScore(score);

        if (score >= PerformanceAnalyzerConstants.EXCELLENT) {
            perfData.setPerformanceLevel("Excellent");
        } else if (score >= PerformanceAnalyzerConstants.GOOD) {
            perfData.setPerformanceLevel("Good");
        } else if (score >= PerformanceAnalyzerConstants.FAIR) {
            perfData.setPerformanceLevel("Fair");
        } else if (score >= PerformanceAnalyzerConstants.POOR) {
            perfData.setPerformanceLevel("Poor");
        } else {
            perfData.setPerformanceLevel("Critical");
        }

        if (score < PerformanceAnalyzerConstants.GOOD) {
            perfData.getRecommendations().add("Run performance profiling to identify bottlenecks");
        }
        if (perfData.getIssues().isEmpty()) {
            perfData.getRecommendations().add("System is performing well, continue monitoring");
        }
    }

    public static void detectAlerts(PerformanceModel perfData) {
        double memUsage = perfData.getMemoryUsagePercent();
        if (memUsage > PerformanceAnalyzerConstants.MEMORY_USAGE_CRITICAL_THRESHOLD) { // Threshold should keep
                                                                                       // consistency with previous
                                                                                       // ones.
            perfData.getAlerts().add("CRITICAL: Memory usage above 90% - OOM risk!");
        } else if (memUsage > PerformanceAnalyzerConstants.MEMORY_USAGE_HIGH_THRESHOLD) {
            perfData.getAlerts().add("WARNING: Memory usage above 80%");
        } else if (memUsage > PerformanceAnalyzerConstants.MEMORY_USAGE_WARNING_THRESHOLD) {
            perfData.getAlerts().add("INFO: Memory usage above 70%");
        }

        long gcTime = perfData.getGcTime();
        long gcCount = perfData.getGcCount();
        if (gcTime > PerformanceAnalyzerConstants.GARBAGE_COLLECTION_TIME_THRESHOLD) {
            perfData.getAlerts().add("WARNING: Total GC time exceeds 10 seconds");
        }

        if (gcCount > PerformanceAnalyzerConstants.GARBAGE_COLLECTION_CRITICAL_THRESHOLD) {
            perfData.getAlerts().add("CRITICAL: GC count exceeds 1000");
        }

        int threadCount = perfData.getThreadCount();
        if (threadCount > PerformanceAnalyzerConstants.THREAD_HIGH_THRESHOLD) {
            perfData.getAlerts().add("HIGH: Thread count exceeds 500");
        } else if (threadCount > PerformanceAnalyzerConstants.THREAD_WARNING_THRESHOLD) {
            perfData.getAlerts().add("WARNING: Thread count exceeds 200");
        }

        long loadedClasses = perfData.getLoadedClassCount();
        if (loadedClasses > PerformanceAnalyzerConstants.LOADED_CLASSES_HIGH_THRESHOLD) {
            perfData.getAlerts().add("HIGH: Loaded class count exceeds 10000");
        }

        if (perfData.getAlerts().isEmpty()) {
            perfData.getAlerts().add("INFO: All metrics within normal range");
        }
    }

    public static String formatBytes(long bytes) {
        if (bytes < BINARY_BASE) {
            return bytes + " B";
        } else if (bytes < BINARY_BASE * BINARY_BASE) {
            return String.format("%.2f KB", bytes / BINARY_BASE_DOUBLE);
        } else if (bytes < BINARY_BASE * BINARY_BASE * BINARY_BASE) {
            return String.format("%.2f MB", bytes / (BINARY_BASE_DOUBLE * BINARY_BASE));
        } else {
            return String.format("%.2f GB", bytes / (BINARY_BASE_DOUBLE * BINARY_BASE * BINARY_BASE));
        }
    }
}
