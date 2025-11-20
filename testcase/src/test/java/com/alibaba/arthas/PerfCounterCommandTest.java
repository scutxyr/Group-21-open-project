package com.alibaba.arthas;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class PerfCounterCommandTest {

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
    public void testPerfCounterCommand() throws Exception {
        System.out.println("\n=== 测试 PerfCounter 命令 ===");
        
        String output = executeArthasCommand("perfcounter");
        
        System.out.println("\n【PerfCounter 命令完整输出】");
        System.out.println("========================================");
        System.out.println(output);
        System.out.println("========================================");
        
        assertTrue("输出应包含性能计数器信息", output.length() > 100);
        
        boolean hasValidCounters = 
            output.contains("java.") || 
            output.contains("sun.") ||
            output.contains("os.") ||
            output.contains("compiler.");
        
        assertTrue("应包含有效的性能计数器", hasValidCounters);
        
        System.out.println("\n✓ PerfCounter 命令测试通过");
    }

    @Test
    public void testPerfCounterWithPattern() throws Exception {
        System.out.println("\n=== 测试 PerfCounter 命令（带匹配模式）===");
        
        String output = executeArthasCommand("perfcounter -d java.*");
        
        System.out.println("\n【PerfCounter 命令（带模式）完整输出】");
        System.out.println("========================================");
        System.out.println(output);
        System.out.println("========================================");
        
        assertNotNull("输出不应为空", output);
        assertTrue("输出应包含 java 相关计数器", 
            output.contains("java.") || output.contains("没有找到") || output.contains("not found"));
        
        System.out.println("\n✓ PerfCounter 匹配模式测试通过");
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
}
