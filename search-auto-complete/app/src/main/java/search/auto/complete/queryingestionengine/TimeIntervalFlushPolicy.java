package search.auto.complete.queryingestionengine;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimeIntervalFlushPolicy implements FlushPolicy {

    private final long interval;
    private final TimeUnit timeUnit;
    private final ScheduledExecutorService scheduler;
    private boolean isRunning = false;

    public TimeIntervalFlushPolicy(long intervalSecond) {
        this(intervalSecond, TimeUnit.SECONDS);
    }

    public TimeIntervalFlushPolicy(long interval, TimeUnit timeUnit) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Flush interval must be positive: " + interval);
        }

        this.interval = interval;
        this.timeUnit = Objects.requireNonNull(timeUnit, "TimeUnit cannot be null");
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "query-ingestion-flush-thread");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public synchronized void start(Runnable flushAction) {
        Objects.requireNonNull(flushAction, "Flush action cannot be null");

        if (isRunning) {
            throw new IllegalStateException("Flush policy is already running");
        }

        if (scheduler.isShutdown()) {
            throw new IllegalStateException("Cannot restart a stopped FlushPolicy");
        }

        scheduler.scheduleWithFixedDelay(() -> {
                    try {
                        flushAction.run();
                    } catch (Throwable e) {
                        System.err.println("[QueryIngestionBuffer] " +
                                "Error during background flush "
                                + e.getMessage());
                    }
                }, interval, interval, timeUnit);
        this.isRunning = true;
    }

    @Override
    public synchronized void stop() {
        if (!isRunning) return;

        this.isRunning = false;
        this.scheduler.shutdown();
    }
}
