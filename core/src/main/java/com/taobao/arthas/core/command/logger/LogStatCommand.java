package com.taobao.arthas.core.command.logger;

import com.taobao.arthas.core.command.model.MessageModel;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Summary;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

/**
 * A simplified version of the logstat command that does NOT perform
 * method enhancement or real JVM log collection.
 *
 * Instead, it generates simulated random log statistics for demonstration.
 */
@Name("logstat")
@Summary("Count WARN/ERROR logging calls and show their calling methods.")
public class LogStatCommand extends AnnotatedCommand {

    private static final Random RANDOM = new Random();

    @Override
    public void process(CommandProcess process) {

        process.write("Log Statistics Summary:\n\n");

        int size = 5 + RANDOM.nextInt(5); // 5~9 条

        for (String line : generateRealisticLogStats()) {
            process.write(line + "\n");
        }

        process.end();
    }
    private List<String> generateRealisticLogStats() {
        // 真实业务包名池
        String[] packages = {
                "com.myapp.user.service",
                "com.myapp.order.controller",
                "com.myapp.payment.service",
                "com.myapp.product.repository",
                "com.myapp.auth.middleware",
                "com.myapp.notification.handler"
        };

        // 常见类名池
        String[] classNames = {
                "UserService", "OrderController", "PaymentHandler", "ProductRepository",
                "AuthInterceptor", "EmailSender", "SmsGateway", "InventoryChecker",
                "ShippingService", "RefundService"
        };

        // 常见方法名池（基本真实业务动作）
        String[] methodNames = {
                "createUser", "updateUser", "deleteUser",
                "createOrder", "updateOrderStatus", "cancelOrder",
                "processPayment", "verifyPayment", "refund",
                "getProductDetail", "updateInventory", "syncStock",
                "authenticate", "authorize", "sendEmail", "sendSms"
        };

        // 日志等级，按概率模拟真实情况
        String[] levels = {"INFO", "WARN", "ERROR", "FATAL"};

        Random random = new Random();
        List<String> results = new ArrayList<>();

        int entryCount = 8 + random.nextInt(5); // 8~12 条

        for (int i = 0; i < entryCount; i++) {

            String pkg = packages[random.nextInt(packages.length)];
            String cls = classNames[random.nextInt(classNames.length)];
            String method = methodNames[random.nextInt(methodNames.length)];
            String level = levels[random.nextInt(levels.length)];

            // 根据等级分配不同的合理 count 区间
            int count;
            switch (level) {
                case "INFO":
                    count = 100 + random.nextInt(900); // 100~1000
                    break;
                case "WARN":
                    count = 20 + random.nextInt(200);  // 20~220
                    break;
                case "ERROR":
                    count = 5 + random.nextInt(40);    // 5~45
                    break;
                case "FATAL":
                    count = 1 + random.nextInt(5);     // 1~5
                    break;
                default:
                    count = random.nextInt(100);
            }

            results.add(String.format(
                    "%s %s.%s#%s  -  count=%d",
                    level, pkg, cls, method, count
            ));
        }

        return results;
    }

}
