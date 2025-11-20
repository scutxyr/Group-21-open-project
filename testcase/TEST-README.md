# Arthas 命令功能测试

## 测试说明

本目录包含三个 JUnit 测试类，用于测试 Arthas 的核心命令功能：

1. **JvmCommandTest** - 测试 `jvm` 命令
2. **PerfCounterCommandTest** - 测试 `perfcounter` 命令  
3. **MemoryCommandTest** - 测试 `memory` 命令

## 文件结构

```
testcase/
├── pom.xml                          # 已添加 JUnit 4.12 依赖
├── src/
│   ├── main/java/com/alibaba/arthas/
│   │   ├── Test.java                # 原有的测试目标程序
│   │   ├── Pojo.java                # 数据对象
│   │   └── Type.java                # 枚举类型
│   └── test/java/com/alibaba/arthas/
│       ├── JvmCommandTest.java      # ✨ JVM 命令测试
│       ├── PerfCounterCommandTest.java  # ✨ PerfCounter 命令测试
│       └── MemoryCommandTest.java   # ✨ Memory 命令测试
```

## 前置条件

1. **已编译 Arthas 项目**：
   ```powershell
   mvn clean package -DskipTests
   ```

2. **已编译 MathGame**：
   ```powershell
   cd math-game
   mvn clean compile
   cd ..
   ```

3. **已编译 testcase 模块**：
   ```powershell
   cd testcase
   mvn clean compile
   cd ..
   ```

4. **本地安装 Arthas 4.0.5**：
   - 确保 `~/.arthas/lib/4.0.5/` 目录存在
   - 包含 `arthas-core.jar` 等文件

## 运行测试

### 方式一：运行单个测试类

```powershell
# 测试 jvm 命令
cd testcase
mvn test -Dtest=JvmCommandTest

# 测试 perfcounter 命令
mvn test -Dtest=PerfCounterCommandTest

# 测试 memory 命令
mvn test -Dtest=MemoryCommandTest
```

### 方式二：运行所有测试

```powershell
cd testcase
mvn test
```

### 方式三：运行特定测试方法

```powershell
# 只运行 JvmCommandTest 中的 testJvmCommand 方法
mvn test -Dtest=JvmCommandTest#testJvmCommand

# 运行 PerfCounterCommandTest 中的特定方法
mvn test -Dtest=PerfCounterCommandTest#testPerfCounterWithPattern
```

## 测试流程

每个测试类都遵循以下流程：

1. **@Before setUp()**
   - 启动 MathGame 作为目标进程
   - 通过 `jps` 获取 MathGame 的 PID
   - 启动 Arthas 并 attach 到 MathGame 进程
   - 等待 Arthas 初始化完成

2. **@Test testXXX()**
   - 使用 arthas-client 执行命令
   - 验证输出包含期望的关键信息
   - 使用 JUnit 断言检查结果

3. **@After tearDown()**
   - 停止 Arthas（执行 stop 命令）
   - 终止 MathGame 进程
   - 清理测试环境

## 测试内容

### JvmCommandTest

验证 `jvm` 命令能够获取：
- ✅ RUNTIME 信息（JVM 版本、启动时间等）
- ✅ CLASS-LOADING 信息（已加载类数量）
- ✅ COMPILATION 信息（JIT 编译器信息）
- ✅ GARBAGE-COLLECTORS 信息（GC 收集器）
- ✅ MEMORY 信息（堆/非堆内存）
- ✅ OPERATING-SYSTEM 信息（操作系统）
- ✅ THREAD 信息（线程统计）

### PerfCounterCommandTest

验证 `perfcounter` 命令能够：
- ✅ 获取所有性能计数器
- ✅ 使用匹配模式过滤（如 `perfcounter -d java.*`）
- ✅ 输出包含 `java.*`、`sun.*`、`os.*` 等计数器

### MemoryCommandTest

验证 `memory` 命令能够：
- ✅ 获取堆内存信息（heap）
- ✅ 显示各内存区域（eden、survivor、old、metaspace）
- ✅ 显示内存使用量（used、max、百分比）
- ✅ 包含数值数据（KB/MB/GB）

## 测试输出示例

```
=== 启动测试目标进程 ===
目标进程 PID: 12345
Arthas 已启动并连接到进程 12345

=== 测试 JVM 命令 ===
✓ JVM 命令测试通过
输出摘要:
  RUNTIME: VM-VERSION: 1.8.0_462 ...
  MEMORY: heap: 245M/3641M ...
  THREAD: COUNT: 15 ...

=== 清理测试环境 ===
测试环境已清理

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

## 常见问题

### 1. 端口冲突

如果提示端口 3658 被占用：
```powershell
# 查找占用端口的进程
netstat -ano | findstr :3658

# 结束进程（替换 <PID>）
taskkill /F /PID <PID>
```

### 2. MathGame 未启动

确保 MathGame 已编译：
```powershell
cd math-game
mvn clean compile
```

### 3. Arthas JAR 文件不存在

确保已编译整个项目：
```powershell
mvn clean package -DskipTests
```

### 4. JUnit 依赖问题

重新下载依赖：
```powershell
cd testcase
mvn dependency:resolve
```

## 与原有文件的区别

### Test.java（src/main/java）
- **用途**：手动运行的演示程序
- **运行方式**：`java -cp target/classes com.alibaba.arthas.Test`
- **特点**：无限循环，需要手动 attach Arthas

### JvmCommandTest.java（src/test/java）
- **用途**：自动化测试
- **运行方式**：`mvn test -Dtest=JvmCommandTest`
- **特点**：
  - 自动启动目标进程
  - 自动 attach Arthas
  - 自动执行命令并验证结果
  - 自动清理环境
  - 生成测试报告

## 优势

✨ **标准 JUnit 格式**
- 集成到 Maven 生命周期
- 可在 CI/CD 中自动运行
- 生成标准测试报告

🔄 **全自动化**
- 无需手动启动进程
- 无需手动执行命令
- 自动验证结果

📊 **断言验证**
- 使用 JUnit 断言确保功能正确
- 测试失败时自动报告

🧹 **自动清理**
- @After 保证环境清理
- 避免僵尸进程

## 下一步

可以继续添加更多测试：
- `DashboardCommandTest` - 测试 dashboard 命令
- `ThreadCommandTest` - 测试 thread 命令
- `WatchCommandTest` - 测试 watch 命令
- `TraceCommandTest` - 测试 trace 命令

## 参考文档

- [JUnit 4 官方文档](https://junit.org/junit4/)
- [Arthas 命令列表](https://arthas.aliyun.com/doc/commands.html)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
