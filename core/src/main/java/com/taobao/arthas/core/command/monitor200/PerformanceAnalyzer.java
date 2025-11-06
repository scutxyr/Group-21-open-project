package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.command.model.PerformanceData;
import java.lang.management.*;
import java.util.List;

/**
 * 性能分析工具类
 * 用于收集、分析和评估JVM性能指标
 *
 * @author Huang Junhao
 */
public class PerformanceAnalyzer {

    /**
     * 收集性能数据
     */
    public static PerformanceData collectPerformanceData() {
        PerformanceData perfData = new PerformanceData();

        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            long usedMemory = totalMemory - freeMemory;
            double usagePercent = (double) usedMemory / maxMemory * 100;

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
                if (count > 0) gcCount += count;
                if (time > 0) gcTime += time;
            }
            perfData.setGcCount(gcCount);
            perfData.setGcTime(gcTime);

            ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
            perfData.setLoadedClassCount(classBean.getLoadedClassCount());
            perfData.setTotalLoadedClassCount(classBean.getTotalLoadedClassCount());
            perfData.setUnloadedClassCount(classBean.getUnloadedClassCount());

            perfData.setAvailableProcessors(runtime.availableProcessors());

        } catch (Exception e) {
            // 忽略错误，返回部分数据
        }

        return perfData;
    }

    /**
     * 计算性能评分 (0-100)
     */
    public static void calculatePerformanceScore(PerformanceData perfData) {
        int score = 100;

        double memUsage = perfData.getMemoryUsagePercent();
        if (memUsage > 90) {
            score -= 40;
            perfData.getIssues().add("Memory usage is critically high (" + String.format("%.1f", memUsage) + "%)");
            perfData.getRecommendations().add("Consider increasing heap size with -Xmx parameter");
        } else if (memUsage > 80) {
            score -= 30;
            perfData.getIssues().add("Memory usage is high (" + String.format("%.1f", memUsage) + "%)");
            perfData.getRecommendations().add("Monitor memory usage and consider optimization");
        } else if (memUsage > 70) {
            score -= 15;
        }

        long gcCount = perfData.getGcCount();
        long gcTime = perfData.getGcTime();
        if (gcCount > 1000) {
            score -= 30;
            perfData.getIssues().add("High GC frequency (" + gcCount + " collections)");
            perfData.getRecommendations().add("Review object creation patterns and memory allocation");
        } else if (gcCount > 500) {
            score -= 20;
        } else if (gcCount > 100) {
            score -= 10;
        }

        if (gcTime > 10000) {
            score -= 10;
            perfData.getIssues().add("High GC time (" + gcTime + "ms)");
        }

        int threadCount = perfData.getThreadCount();
        if (threadCount > 500) {
            score -= 20;
            perfData.getIssues().add("Very high thread count (" + threadCount + ")");
            perfData.getRecommendations().add("Review thread pool configurations");
        } else if (threadCount > 200) {
            score -= 10;
            perfData.getIssues().add("High thread count (" + threadCount + ")");
        }

        long loadedClasses = perfData.getLoadedClassCount();
        if (loadedClasses > 10000) {
            score -= 10;
            perfData.getIssues().add("Large number of loaded classes (" + loadedClasses + ")");
            perfData.getRecommendations().add("Consider reducing dependencies or using lazy loading");
        } else if (loadedClasses > 5000) {
            score -= 5;
        }

        score = Math.max(0, Math.min(100, score));
        perfData.setPerformanceScore(score);

        if (score >= 90) {
            perfData.setPerformanceLevel("Excellent");
        } else if (score >= 75) {
            perfData.setPerformanceLevel("Good");
        } else if (score >= 60) {
            perfData.setPerformanceLevel("Fair");
        } else if (score >= 40) {
            perfData.setPerformanceLevel("Poor");
        } else {
            perfData.setPerformanceLevel("Critical");
        }

        if (score < 75) {
            perfData.getRecommendations().add("Run performance profiling to identify bottlenecks");
        }
        if (perfData.getIssues().isEmpty()) {
            perfData.getRecommendations().add("System is performing well, continue monitoring");
        }
    }

    /**
     * 检测性能告警
     */
    public static void detectAlerts(PerformanceData perfData) {
        double memUsage = perfData.getMemoryUsagePercent();
        if (memUsage > 95) {
            perfData.getAlerts().add("CRITICAL: Memory usage above 95% - OOM risk!");
        } else if (memUsage > 90) {
            perfData.getAlerts().add("WARNING: Memory usage above 90%");
        } else if (memUsage > 85) {
            perfData.getAlerts().add("INFO: Memory usage above 85%");
        }

        long gcTime = perfData.getGcTime();
        long gcCount = perfData.getGcCount();
        if (gcTime > 30000) {
            perfData.getAlerts().add("CRITICAL: Total GC time exceeds 30 seconds");
        } else if (gcTime > 10000) {
            perfData.getAlerts().add("WARNING: Total GC time exceeds 10 seconds");
        }

        if (gcCount > 2000) {
            perfData.getAlerts().add("WARNING: GC count exceeds 2000");
        }

        int threadCount = perfData.getThreadCount();
        if (threadCount > 1000) {
            perfData.getAlerts().add("CRITICAL: Thread count exceeds 1000");
        } else if (threadCount > 500) {
            perfData.getAlerts().add("WARNING: Thread count exceeds 500");
        }

        long loadedClasses = perfData.getLoadedClassCount();
        if (loadedClasses > 20000) {
            perfData.getAlerts().add("WARNING: Loaded class count exceeds 20000");
        }

        if (perfData.getAlerts().isEmpty()) {
            perfData.getAlerts().add("INFO: All metrics within normal range");
        }
    }

    /**
     * 格式化字节数
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
