package com.jyotimoykashyap;

import com.jyotimoykashyap.output.AsyncAppender;
import com.jyotimoykashyap.output.LogAppender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class LoggerConcurrencyTest {

    /**
     * Test 1: 20 concurrent threads blasting 100 logs each (total 2,000 logs).
     * Asserts that zero logs are lost and all 2,000 messages are delivered to the sink.
     */
    @Test
    @DisplayName("AsyncAppender should deliver all 2,000 logs from 20 concurrent threads without loss")
    void testConcurrentBlasterZeroDrop() throws InterruptedException {
        int threadCount = 20;
        int logsPerThread = 100;
        int totalExpectedLogs = threadCount * logsPerThread;

        AtomicInteger deliveredLogsCount = new AtomicInteger(0);

        // In-memory counting appender
        LogAppender countingAppender = new LogAppender() {
            @Override
            public void appendLog(LogMessage log) {
                deliveredLogsCount.incrementAndGet();
            }

            @Override
            public void close() {}
        };

        // Large enough buffer to hold all 2,000 logs
        AsyncAppender asyncAppender = new AsyncAppender(countingAppender, 4096);

        Logger logger = new Logger.Builder()
                .setLogLevel(LogLevel.DEBUG)
                .setOutputs(List.of(asyncAppender))
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for simultaneous gun-shot start
                    for (int j = 0; j < logsPerThread; j++) {
                        logger.info("Thread-" + threadId + " Message-" + j);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Fire all threads simultaneously
        startLatch.countDown();
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS), "All producer threads should finish within 5s");

        // Gracefully close logger (drains the queue)
        logger.close();
        executor.shutdown();

        assertEquals(totalExpectedLogs, deliveredLogsCount.get(),
                "All 2,000 concurrent logs must be delivered through the async queue");
    }

    /**
     * Test 2: Log Level Hierarchy Filtering.
     * Asserts that logs below the configured threshold are discarded on the fast-path.
     */
    @Test
    @DisplayName("Logger should discard logs below configured minimum level")
    void testLevelFiltering() {
        AtomicInteger loggedCount = new AtomicInteger(0);

        LogAppender appender = new LogAppender() {
            @Override
            public void appendLog(LogMessage log) {
                loggedCount.incrementAndGet();
            }

            @Override
            public void close() {}
        };

        // Configured threshold is WARN
        Logger logger = new Logger.Builder()
                .setLogLevel(LogLevel.WARN)
                .setOutputs(List.of(appender))
                .build();

        logger.debug("Debug 1"); // Discarded
        logger.info("Info 1");   // Discarded
        logger.warn("Warn 1");   // Accepted
        logger.error("Error 1"); // Accepted
        logger.fatal("Fatal 1"); // Accepted

        assertEquals(3, loggedCount.get(), "Only WARN, ERROR, and FATAL should be logged");
    }

    /**
     * Test 3: Backpressure / Non-blocking Queue Capacity.
     * Asserts that when the buffer fills up, application threads are NEVER blocked.
     */
    @Test
    @DisplayName("AsyncAppender should never block producers even when queue is saturated")
    void testNonBlockingUnderSaturation() throws InterruptedException {
        // Appender that artificially sleeps to simulate slow I/O
        LogAppender slowAppender = new LogAppender() {
            @Override
            public void appendLog(LogMessage log) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public void close() {}
        };

        // Very small buffer (capacity 5)
        AsyncAppender asyncAppender = new AsyncAppender(slowAppender, 5);

        Logger logger = new Logger.Builder()
                .setLogLevel(LogLevel.INFO)
                .setOutputs(List.of(asyncAppender))
                .build();

        long startTime = System.currentTimeMillis();

        // Push 50 logs. If synchronous, 50 * 50ms = 2500ms.
        // Because asyncAppender.offer is non-blocking, it should finish in < 50ms!
        for (int i = 0; i < 50; i++) {
            logger.info("Flood message " + i);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        assertTrue(elapsed < 200, "Producers should return immediately (< 200ms) without blocking on slow I/O");

        logger.close();
    }
}
