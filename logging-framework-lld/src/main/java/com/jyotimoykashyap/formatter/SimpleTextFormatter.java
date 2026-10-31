package com.jyotimoykashyap.formatter;

import com.jyotimoykashyap.LogMessage;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SimpleTextFormatter implements LogFormatter {
    @Override
    public String format(LogMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(msg.timestamp()).append("] ")
                .append("[").append(msg.level()).append("] ")
                .append("[").append(msg.threadName()).append("] - ")
                .append(msg.message());
        // Bonus: If an exception was logged, append its stack trace!
        if (msg.throwable() != null) {
            StringWriter sw = new StringWriter();
            msg.throwable().printStackTrace(new PrintWriter(sw));
            sb.append(System.lineSeparator()).append(sw);
        }
        return sb.toString();
    }
}
