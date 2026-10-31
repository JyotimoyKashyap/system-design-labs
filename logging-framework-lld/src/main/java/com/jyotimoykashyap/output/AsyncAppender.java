package com.jyotimoykashyap.output;

import com.jyotimoykashyap.LogMessage;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class AsyncAppender implements LogAppender {
    private static final int DEFAULT_CAPACITY = 1024;

    private final LogAppender delegate;
    private final BlockingQueue<LogMessage> queue;
    private final Thread workerThread;
    private volatile boolean isRunning;

    public AsyncAppender(LogAppender delegate) {
        this(delegate, DEFAULT_CAPACITY);
    }

    public AsyncAppender(LogAppender delegate, int capacity) {
        this.delegate = Objects.requireNonNull(delegate);

        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity should be > 0");
        }

        this.queue = new ArrayBlockingQueue<>(capacity);
        this.isRunning = true;

        // create the background consumer thread
        this.workerThread = new Thread(this::consumeLogs, "AsyncAppender-Worker");
        this.workerThread.setDaemon(true); // Dont' prevent JVM from shutting down
        this.workerThread.start();
    }

    @Override
    public void appendLog(LogMessage log) {
        if (!isRunning) {
            return;
        }

        boolean enqueued = queue.offer(log);

        if (!enqueued) {
            // BACKPRESSURE POLICY : Queue is full!
            // For now we drop the log, but then there can be different types of policies
            System.err.println("[AsyncAppender] Queue full! Dropping log: " + log.message());
        }

    }

    private void consumeLogs() {
        while (isRunning || !queue.isEmpty()) {
            try {
                LogMessage message = queue.poll(100, TimeUnit.MILLISECONDS);
                if (message != null) {
                    delegate.appendLog(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public void close() {
        this.isRunning = false;
        try {
            workerThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        delegate.close();
    }
}
