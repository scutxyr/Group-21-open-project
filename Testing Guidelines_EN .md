# 📊 BitzStats Functional Testing Guide

## Test Overview
The `bitzstats` command is used to monitor and collect statistics on business method invocations, including call count, average response time, success rate, and other key metrics.

## Test Environment Requirements
- JDK 1.8+
- Arthas 4.1.1+
- Test application (e.g., math-game.jar)

## Test Procedures

### 1. Environment Setup
```bash
# Start test application
java -jar math-game.jar

# Start Arthas
java -jar arthas-boot.jar
```

### 2. Basic Functionality Testing
```bash
# Check if command exists
help | grep bitzstats


```

### 3. Feature Verification (if command is available)
```bash
# Display business statistics
bitzstats


```

## Expected Output Example
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

## Unit Testing Plan

### 1. BusinessStatsCollector Unit Tests
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

### 2. Integration Testing Plan
```java
// Use Arthas testing framework for integration tests
public class BitzStatsCommandIT {
    
    @Test
    public void testCommandRegistration() {
        // Verify command is correctly registered in Arthas
        // Use Arthas testing utility classes
    }
}
```

## Manual Verification Checklist

- [ ] Command appears in help list
- [ ] Command executes normally
- [ ] Help information displays correctly
- [ ] Parameter parsing works properly
- [ ] Business statistics display correctly
- [ ] Reset functionality works normally

## Troubleshooting Guide

### If Command is Unavailable:
1. Check if class files compiled successfully
2. Verify jar package contains relevant class files
3. Check command registration logic
4. Review Arthas startup logs

### Debug Commands:
```bash
# Check class loading
sc -d com.taobao.arthas.core.shell.command.biz.BitzStatsCommand

# View all commands
help
```

## Performance Testing Scenarios

### 1. High Concurrency Test
- Simulate 1000+ concurrent method invocations
- Verify statistical accuracy under load
- Monitor memory usage and CPU consumption

### 2. Long-running Test
- Continuous 24-hour operation test
- Verify memory leak prevention
- Check statistical data consistency

### 3. Stress Test
- Maximum concurrent connection test
- Large data volume statistical test
- System resource usage monitoring

## Compatibility Testing Matrix

| Environment | Status | Notes |
|-------------|--------|-------|
| JDK 1.8 | ✅ Required | Primary support |
| JDK 11 | ✅ Required | Extended support |
| Windows 10 | ✅ Required | Full functionality |
| Linux Ubuntu | ✅ Required | Full functionality |
| macOS | ✅ Required | Full functionality |
| Arthas 3.x | ⚠️ Optional | Limited testing |
| Arthas 4.x | ✅ Required | Primary version |



## Success Criteria

### Functional Success Criteria:
- Command successfully registers in Arthas
- All parameters work correctly
- Statistical data is accurate and complete
- No memory leaks detected
- Error handling works properly

### Performance Success Criteria:
- Response time < 100ms for all operations
- Memory usage < 100MB under load
- Supports 1000+ concurrent recordings
- No statistical data loss

## Rollback Plan

### If Issues Detected:
1. Disable command registration temporarily
2. Restore original arthas-core.jar backup
3. Notify development team for fixes
4. Schedule re-testing after fixes

