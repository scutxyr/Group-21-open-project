import com.taobao.arthas.core.shell.command.biz.BusinessStatsCollector;
import java.util.Map;

public class TestBitzStats {
    public static void main(String[] args) {
        System.out.println("=== Testing BitzStats Function ===\n");

        // Record method invocations
        System.out.println("1. Recording method invocations...");
        BusinessStatsCollector.recordInvoke("com.example.UserService", "getUser", 150);
        BusinessStatsCollector.recordInvoke("com.example.UserService", "getUser", 200);
        BusinessStatsCollector.recordInvoke("com.example.OrderService", "createOrder", 300);
        BusinessStatsCollector.recordInvoke("com.example.UserService", "updateUser", 100);
        BusinessStatsCollector.recordInvoke("com.example.OrderService", "createOrder", 400);

        System.out.println("2. Getting statistics...");
        Map<String, BusinessStatsCollector.BusinessMethodStats> stats = BusinessStatsCollector.getStats();

        System.out.println("Total entries: " + stats.size());
        System.out.println("\nDetailed statistics:");

        stats.forEach((key, value) -> {
            System.out.println("Method: " + key);
            System.out.println("  Invoke count: " + value.getInvokeCount());
            System.out.println("  Average time: " + String.format("%.2f", value.getAverageTime()) + "ms");
            System.out.println("  Last invoke: " + value.getLastInvokeTime());
            System.out.println();
        });

        // Test reset function
        System.out.println("3. Testing reset function...");
        BusinessStatsCollector.reset();
        stats = BusinessStatsCollector.getStats();
        System.out.println("Entries after reset: " + stats.size());

        System.out.println("\n=== Test Completed ===");
    }
}
