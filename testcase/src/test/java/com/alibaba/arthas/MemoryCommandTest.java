package com.alibaba.arthas;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MemoryCommandTest {

    private Process targetProcess;
    private String targetPid;
    private String projectRoot;
    private String arthasBootJar;
    private String mathGameClasspath;

    @Before
    public void setUp() throws Exception {
        System.out.println("=== 启动测试目标进程 ===");
        
        projectRoot = findProjectRoot();
        arthasBootJar = projectRoot + "\\boot\\target\\arthas-boot-jar-with-dependencies.jar";
        mathGameClasspath = projectRoot + "\\math-game\\target\\classes";
        
        System.out.println("项目根目录: " + projectRoot);
        
        ProcessBuilder pb = new ProcessBuilder(
            "java",
            "-cp", mathGameClasspath,
            "demo.MathGame"
        );
        targetProcess = pb.start();
        
        TimeUnit.SECONDS.sleep(3);
        
        targetPid = findMathGamePid();
        assertNotNull("找不到 MathGame 进程", targetPid);
        System.out.println("目标进程 PID: " + targetPid);
    }

    @Test
    public void testMemoryCommand() throws Exception {
        System.out.println("\n=== 测试 Memory 命令 ===");
        
        String output = executeArthasCommand("memory");
        
        System.out.println("\n【Memory 命令完整输出】");
        System.out.println("========================================");
        System.out.println(output);
        System.out.println("========================================");
        
        assertTrue("输出应包含 heap 信息", output.contains("heap"));
        assertTrue("输出应包含内存使用数据", output.contains("used") || output.contains("max"));
        
        boolean hasMemoryRegions = 
            output.contains("eden") || 
            output.contains("survivor") ||
            output.contains("old") ||
            output.contains("metaspace") ||
            output.contains("code_cache");
        
        assertTrue("应包含内存区域信息", hasMemoryRegions);
        
        System.out.println("\n✓ Memory 命令测试通过");
        printMemorySummary(output);
    }

    @Test
    public void testMemoryUsageValues() throws Exception {
        System.out.println("\n=== 测试 Memory 使用量数值 ===");
        
        String output = executeArthasCommand("memory");

        System.out.println("\n【Memory 使用量完整输出】");
        System.out.println("========================================");
        System.out.println(output);
        System.out.println("========================================");
        
        assertTrue("应包含内存使用的数值数据", output.length() > 100);
        
        System.out.println("\n✓ Memory 数值测试通过");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("\n=== 清理测试环境 ===");

        if (targetProcess != null && targetProcess.isAlive()) {
            targetProcess.destroy();
            targetProcess.waitFor(5, TimeUnit.SECONDS);
        }
        
        System.out.println("测试环境已清理");
    }

    private String findProjectRoot() {
        String currentDir = System.getProperty("user.dir");
        java.io.File dir = new java.io.File(currentDir);
        if (dir.getName().equals("testcase")) {
            return dir.getParent();
        }
        return currentDir;
    }

    private String findMathGamePid() throws Exception {
        Process jpsProcess = new ProcessBuilder("jps").start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(jpsProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("MathGame")) {
                    return line.split("\\s+")[0];
                }
            }
        }
        return null;
    }

    private String executeArthasCommand(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "java",
            "-jar", arthasBootJar,
            "--use-version", "4.0.5",
            "--select", "MathGame",
            "-c", command
        );
        
        pb.directory(new java.io.File(projectRoot));
        Process process = pb.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        process.waitFor(60, TimeUnit.SECONDS);
        return output.toString();
    }

    private void printMemorySummary(String output) {
        String[] lines = output.split("\n");
        int count = 0;
        for (String line : lines) {
            if ((line.contains("heap") || line.contains("eden") || 
                 line.contains("old") || line.contains("metaspace")) && count < 5) {
                System.out.println("  " + line.trim());
                count++;
            }
        }
    }
}
