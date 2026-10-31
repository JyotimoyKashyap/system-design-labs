package com.jyotimoykashyap;

import com.jyotimoykashyap.output.AsyncAppender;
import com.jyotimoykashyap.output.ConsoleAppender;
import com.jyotimoykashyap.output.LogAppender;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Logger {
    private LogLevel minLogLevel;
    private List<LogAppender> outputs;
    // needs to be volatile to prevent CPU instruction reordering
    // from publishing a partially constructed object to other threads
    private static volatile Logger instance;

    private Logger (LogLevel minLogLevel, List<LogAppender> outputs) {
        this.minLogLevel = Objects.requireNonNull(minLogLevel, "MinLogLevel cannot be null");
        this.outputs = Objects.requireNonNull(outputs, "Output stream cannot be null");
    }

    public void debug(String msg) {
        log(msg, LogLevel.DEBUG);
    }

    public void info(String msg) {
        log(msg, LogLevel.INFO);
    }

    public void warn(String msg) {
        log(msg, LogLevel.WARN);
    }

    public void error(String msg) {
        log(msg, LogLevel.ERROR);
    }

    public void error(String msg, Throwable t) {
        log(msg, t, LogLevel.ERROR);
    }

    public void fatal(String msg) {
        log(msg, LogLevel.FATAL);
    }

    public void fatal(String msg, Throwable t) {
        log(msg, t, LogLevel.FATAL);
    }

    public void close() {
        outputs.forEach(LogAppender::close);
    }

    private void log(String msg, LogLevel level) {
        log(msg, null, level);
    }

    private void log(String msg, Throwable t, LogLevel level) {
        if (level.getSeverity() >= minLogLevel.getSeverity()) {
            LogMessage message = (t == null)
                    ? new LogMessage(msg, level)
                    : new LogMessage(msg, t, level);

            outputs.forEach(o -> o.appendLog(message));
        }
    }

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger.Builder().build();
                }

                return instance;
            }
        }
        return instance;
    }

    /**
     * Builder for Logger
     */
    public static class Builder {
        private LogLevel minLogLevel = LogLevel.INFO;
        private final List<LogAppender> outputs = new ArrayList<>(List.of(new AsyncAppender(new ConsoleAppender())));

        public Builder setLogLevel(LogLevel level) {
            this.minLogLevel = Objects.requireNonNull(level);
            return this;
        }

        public Builder addOutput(LogAppender appender) {
            this.outputs.add(Objects.requireNonNull(appender));
            return this;
        }

        public Builder setOutputs(List<LogAppender> outputs) {
            this.outputs.clear();
            this.outputs.addAll(Objects.requireNonNull(outputs, "Outputs cannot be null"));
            return this;
        }

        public Logger build() {
            return new Logger(minLogLevel, List.copyOf(outputs));
        }
    }
}
