package com.jyotimoykashyap;

import com.jyotimoykashyap.output.AsyncAppender;
import com.jyotimoykashyap.output.ConsoleAppender;
import com.jyotimoykashyap.output.FileAppender;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("=== 1. Testing Default Synchronous Console Logger ===");
        Logger defaultLogger = Logger.getInstance();
        defaultLogger.info("Application starting up...");
        defaultLogger.debug("This DEBUG log will NOT print because default min level is INFO!");
        defaultLogger.warn("Simulated warning: Memory usage above 80%");
        defaultLogger.error("Simulated error occurred!", new RuntimeException("Database connection timeout"));

        System.out.println("\n=== 2. Testing Custom Asynchronous Logger with File & Console ===");
        String logFilePath = "logging-framework-lld/build/logs/app.log";
        FileAppender fileAppender = new FileAppender(logFilePath);

        // Decorate both ConsoleAppender and FileAppender with AsyncAppender
        AsyncAppender asyncConsole = new AsyncAppender(new ConsoleAppender());
        AsyncAppender asyncFile = new AsyncAppender(fileAppender);

        Logger asyncLogger = new Logger.Builder()
                .setLogLevel(LogLevel.DEBUG)
                .setOutputs(java.util.List.of(asyncConsole, asyncFile))
                .build();

        asyncLogger.debug("Async debug log message");
        asyncLogger.info("Async info log: User 42 checked out");
        asyncLogger.warn("Async warn log: Slow query detected (120ms)");
        asyncLogger.error("Async error log: Payment gateway rejected charge", new IllegalStateException("Card declined"));

        // Allow async workers to process and gracefully flush before exit
        asyncLogger.close();
        System.out.println("\nAll logs flushed cleanly to console and '" + logFilePath + "'!");
    }
}
