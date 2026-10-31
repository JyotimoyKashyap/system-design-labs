package com.jyotimoykashyap;

import java.time.Instant;

public record LogMessage(
        Instant timestamp,
        String message,
        Throwable throwable,
        LogLevel level,
        String threadName
) {
    public LogMessage(String message, LogLevel level) {
        this(Instant.now(), message, null, level, Thread.currentThread().getName());
    }

    public LogMessage(String message, Throwable throwable, LogLevel level) {
        this(Instant.now(), message, throwable, level, Thread.currentThread().getName());
    }
}
