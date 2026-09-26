package search.auto.complete.queryingestionengine;

import search.auto.complete.autocompleteengine.AutoCompleteIndex;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

public class QueryIngestionBuffer {
    private final AtomicReference<ConcurrentHashMap<String, LongAdder>> activeBuffer;
    private final AutoCompleteIndex index;
    private final FlushPolicy flushPolicy;

    public QueryIngestionBuffer(AutoCompleteIndex index, FlushPolicy flushPolicy) {
        this.index = Objects.requireNonNull(index, "Index cannot be null");
        this.flushPolicy = Objects.requireNonNull(flushPolicy, "FlushPolicy cannot be null");
        this.activeBuffer = new AtomicReference<>(new ConcurrentHashMap<>());
    }

    public void start() {
        this.flushPolicy.start(this::flush);
    }

    public synchronized void flush() {
        ConcurrentHashMap<String, LongAdder> snapshot = activeBuffer
                .getAndSet(new ConcurrentHashMap<>());

        if (snapshot.isEmpty()) {
            return;
        }

        index.insertBatch(snapshot);
    }

    public void queue(String query) {
        if (query == null || query.isBlank()) {
            return;
        }
        // query is already validated
        ConcurrentHashMap<String, LongAdder> currentMap = activeBuffer.get();
        currentMap.computeIfAbsent(query, k -> new LongAdder()).increment();
    }

    public void stop() {
        flushPolicy.stop();
        flush();
    }
}
