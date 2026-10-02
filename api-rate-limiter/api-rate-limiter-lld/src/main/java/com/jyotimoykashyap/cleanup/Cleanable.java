package com.jyotimoykashyap.cleanup;

public interface Cleanable {
    void cleanUpStaleEntries(long ttlMillis);
}
