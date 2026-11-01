package com.jyotimoykashyap;

import com.jyotimoykashyap.output.AsyncAppender;
import com.jyotimoykashyap.output.ConsoleAppender;
import com.jyotimoykashyap.output.LogAppender;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LoggerFactory {
    private static final ConcurrentHashMap<String, Logger> registry = new ConcurrentHashMap<>();

    // global root configuration shared by default
    private static volatile LogLevel rootLevel = LogLevel.INFO;
    private static final List<LogAppender> rootAppenders = new CopyOnWriteArrayList<>(List.of(new AsyncAppender(new ConsoleAppender())));

    private LoggerFactory() {}

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(Objects.requireNonNull(clazz, "Class cannot be null").getSimpleName());
    }

    public static Logger getLogger(String name) {
        Objects.requireNonNull(name, "Logger name cannot be null");
        return registry.computeIfAbsent(name, n -> new Logger.Builder()
                .setName(name)
                .setLogLevel(rootLevel)
                .setOutputs(rootAppenders)
                .build());
    }

    public static void setRootLevel(LogLevel level) {
        rootLevel = Objects.requireNonNull(level);
    }

    public static void setLoggerLevel(String name, LogLevel level) {
        Logger logger = registry.get(name);
        if (logger != null) {
            logger.setLogLevel(level);
        }
    }
    public static void closeAll() {
        registry.values().forEach(Logger::close);
        rootAppenders.forEach(LogAppender::close);
    }
}
