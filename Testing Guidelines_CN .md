## 📋 测试指南文档

### 功能测试指南

**功能概述：**
`bitzstats` 命令用于统计业务方法的调用情况，包括调用次数、平均耗时、成功率等指标。

**测试环境要求：**
- JDK 1.8+
- Arthas 4.1.1+
- 测试应用（如 math-game.jar）

**测试步骤：**

1. **环境准备**
   ```bash
   # 启动测试应用
   java -jar math-game.jar
   
   # 启动 Arthas
   java -jar arthas-boot.jar
   ```

2. **基础功能测试**
   ```bash
   # 测试命令是否存在
   help | grep bitzstats
   
   # 测试命令执行
   bitzstats
   bitzstats -h
   ```

3. **功能验证（如果命令可用）**
   ```bash
   # 显示业务统计
   bitzstats
   
   # 显示前5个最频繁调用的方法
   bitzstats -t 5
   
   # 重置统计
   bitzstats -r
   ```

**预期输出示例：**
```
[arthas@12345]$ bitzstats
Business Method Call Statistics (Top 10)
==========================================
Method: com.example.OrderService.createOrder
  Call count: 156
  Average time: 45.2ms
  Last call: 2025-11-02 21:15:30
------------------------------------------
```

### 单元测试方案

**1. BusinessStatsCollector 单元测试**
```java
public class BusinessStatsCollectorTest {
    
    @Test
    public void testRecordInvoke() {
        BusinessStatsCollector.recordInvoke("TestService", "testMethod", 100);
        Map<String, BusinessStatsCollector.BusinessMethodStats> stats = BusinessStatsCollector.getStats();
        
        assertNotNull(stats.get("TestService.testMethod"));
        assertEquals(1, stats.get("TestService.testMethod").getInvokeCount());
    }
    
    @Test
    public void testReset() {
        BusinessStatsCollector.recordInvoke("TestService", "testMethod", 100);
        BusinessStatsCollector.reset();
        
        Map<String, BusinessStatsCollector.BusinessMethodStats> stats = BusinessStatsCollector.getStats();
        assertTrue(stats.isEmpty());
    }
}
```

**2. 集成测试方案**
```java
// 使用 Arthas 测试框架进行集成测试
public class BitzStatsCommandIT {
    
    @Test
    public void testCommandRegistration() {
        // 验证命令是否在 Arthas 中正确注册
        // 使用 Arthas 的测试工具类
    }
}
```

### 手动验证清单

- [ ] 命令在 help 列表中显示
- [ ] 命令可以正常执行
- [ ] 帮助信息显示正确
- [ ] 参数解析正常工作
- [ ] 业务统计数据正确显示
- [ ] 重置功能正常工作

### 问题排查指南

**如果命令不可用：**
1. 检查类文件是否编译成功
2. 验证 jar 包是否包含相关类文件
3. 检查命令注册逻辑
4. 查看 Arthas 启动日志

**调试命令：**
```bash
# 检查类加载
sc -d com.taobao.arthas.core.shell.command.biz.BitzStatsCommand

# 查看所有命令
help
```


