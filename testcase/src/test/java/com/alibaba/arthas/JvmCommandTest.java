package com.alibaba.arthas;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class JvmCommandTest {

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
    public void testJvmCommand() throws Exception {
        System.out.println("\n=== 测试 JVM 命令 ===");
        
        String output = executeArthasCommand("jvm");
        
        System.out.println("\n【JVM 命令完整输出】");
        System.out.println("========================================");
        System.out.println(output);
        System.out.println("========================================");
        
        assertTrue("输出应包含 RUNTIME 信息", output.contains("RUNTIME"));
        assertTrue("输出应包含 CLASS-LOADING 信息", output.contains("CLASS-LOADING"));
        assertTrue("输出应包含 COMPILATION 信息", output.contains("COMPILATION"));
        assertTrue("输出应包含 GARBAGE-COLLECTORS 信息", output.contains("GARBAGE-COLLECTORS"));
        assertTrue("输出应包含 MEMORY 信息", output.contains("MEMORY"));
        assertTrue("输出应包含 OPERATING-SYSTEM 信息", output.contains("OPERATING-SYSTEM"));
        assertTrue("输出应包含 THREAD 信息", output.contains("THREAD"));
        
        assertTrue("应包含 JVM 版本信息", output.contains("VM-VERSION"));
        assertTrue("应包含类加载数量", output.contains("LOADED-CLASS-COUNT"));
        
        System.out.println("\n✓ JVM 命令测试通过");
        System.out.println("输出摘要:");
        printSummary(output, "RUNTIME", "MEMORY", "THREAD");
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


    private void printSummary(String output, String... keywords) {
        for (String keyword : keywords) {
            int index = output.indexOf(keyword);
            if (index != -1) {
                int endIndex = Math.min(index + 200, output.length());
                String excerpt = output.substring(index, endIndex)
                    .replaceAll("\\s+", " ")
                    .trim();
                System.out.println("  " + keyword + ": " + excerpt + "...");
            }
        }
    }
}
