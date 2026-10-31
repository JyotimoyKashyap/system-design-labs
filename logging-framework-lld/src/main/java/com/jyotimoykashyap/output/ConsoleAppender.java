package com.jyotimoykashyap.output;

import com.jyotimoykashyap.LogLevel;
import com.jyotimoykashyap.LogMessage;
import com.jyotimoykashyap.formatter.LogFormatter;
import com.jyotimoykashyap.formatter.SimpleTextFormatter;

import java.util.Objects;

public class ConsoleAppender implements LogAppender {

    LogFormatter formatter;

    public ConsoleAppender() {
        this(new SimpleTextFormatter());
    }

    public ConsoleAppender(LogFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter, "Formatter cannot be null");
    }

    @Override
    public void appendLog(LogMessage log) {
        String formatted = formatter.format(log);
        if (log.level() == LogLevel.ERROR || log.level() == LogLevel.FATAL) {
            System.err.println(formatted);
            return;
        }
        System.out.println(formatted);
    }

    @Override
    public void close() {
        System.out.flush();
    }
}
