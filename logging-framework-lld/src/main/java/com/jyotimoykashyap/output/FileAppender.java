package com.jyotimoykashyap.output;

import com.jyotimoykashyap.LogMessage;
import com.jyotimoykashyap.formatter.LogFormatter;
import com.jyotimoykashyap.formatter.SimpleTextFormatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class FileAppender implements LogAppender {
    private final String filePath;
    private final LogFormatter formatter;
    private final BufferedWriter writer;

    public FileAppender(String filePath) throws IOException {
        this(filePath, new SimpleTextFormatter());
    }

    public FileAppender(String filePath, LogFormatter formatter) throws IOException {
        this.filePath = Objects.requireNonNull(filePath, "File path cannot be null");
        this.formatter = Objects.requireNonNull(formatter, "Formatter cannot be null");

        File file = new File(filePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        // true = append mode so existing logs are not overwritten
        this.writer = new BufferedWriter(new FileWriter(file, true));
    }

    @Override
    public synchronized void appendLog(LogMessage log) {
        try {
            String formatted = formatter.format(log);
            writer.write(formatted);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("[FileAppender] Failed to write log: " + e.getMessage());
        }
    }

    @Override
    public synchronized void close() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println("[FileAppender] Failed to close file writer: " + e.getMessage());
        }
    }

    public String getFilePath() {
        return filePath;
    }
}
