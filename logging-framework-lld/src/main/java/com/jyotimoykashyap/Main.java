package com.jyotimoykashyap;

import com.jyotimoykashyap.output.AsyncAppender;
import com.jyotimoykashyap.output.FileAppender;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("=== 1. Using LoggerFactory for Per-Class Loggers (Flyweight Pattern) ===");

        // Two different classes request their loggers
        Logger orderLogger = LoggerFactory.getLogger(OrderService.class);
        Logger paymentLogger = LoggerFactory.getLogger(PaymentService.class);

        // Verify Flyweight: requesting OrderService again returns the EXACT same instance
        Logger orderLoggerAgain = LoggerFactory.getLogger(OrderService.class);
        System.out.println("Are orderLogger and orderLoggerAgain the exact same instance? " + (orderLogger == orderLoggerAgain));

        // Default Root Level is INFO
        orderLogger.info("Order #1001 placed successfully");
        orderLogger.debug("Order #1001 database query executed (SHOULD NOT PRINT)");

        paymentLogger.info("Payment of $150 initiated for user_42");
        paymentLogger.debug("Payment gateway raw payload (SHOULD NOT PRINT)");

        System.out.println("\n=== 2. Surgical Live Reconfiguration (Incident Troubleshooting) ===");
        // Simulate a payment incident: enable DEBUG *only* for PaymentService
        System.out.println(">> Changing PaymentService log level to DEBUG dynamically...");
        LoggerFactory.setLoggerLevel("PaymentService", LogLevel.DEBUG);

        paymentLogger.debug("Payment gateway HTTP status 200, trace_id: 9a8b-7c6d"); // Now prints!
        orderLogger.debug("OrderService still suppressed (SHOULD NOT PRINT)");        // Still suppressed!

        System.out.println("\n=== 3. Adding Asynchronous FileAppender to Shared Pipeline ===");
        String logFilePath = "logging-framework-lld/build/logs/app.log";
        FileAppender fileAppender = new FileAppender(logFilePath);
        AsyncAppender asyncFile = new AsyncAppender(fileAppender);

        Logger auditLogger = new Logger.Builder()
                .setName("AuditService")
                .setLogLevel(LogLevel.DEBUG)
                .setOutputs(java.util.List.of(asyncFile))
                .build();

        auditLogger.info("Audit: User admin logged in");
        auditLogger.error("Audit: Unauthorized access attempt blocked", new SecurityException("Invalid token"));

        // Allow async workers to drain and flush
        auditLogger.close();
        LoggerFactory.closeAll();

        System.out.println("All logs flushed successfully! Inspect disk logs at: " + logFilePath);
    }

    // Simulated application classes
    static class OrderService {}
    static class PaymentService {}
}
