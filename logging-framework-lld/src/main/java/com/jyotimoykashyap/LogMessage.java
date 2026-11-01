package com.jyotimoykashyap;

import java.time.Instant;

public record LogMessage(
        Instant timestamp,
        String message,
        Throwable throwable,
        LogLevel level,
        String threadName,
        String loggerName
) {
    public LogMessage(String message, LogLevel level, String loggerName) {
        this(Instant.now(), message, null, level, Thread.currentThread().getName(), loggerName);
    }

    public LogMessage(String message, Throwable throwable, LogLevel level, String loggerName) {
        this(Instant.now(), message, throwable, level, Thread.currentThread().getName(), loggerName);
    }
}
