package search.auto.complete.queryingestionengine;

public interface FlushPolicy {
    void start(Runnable flushAction);
    void stop();
}
