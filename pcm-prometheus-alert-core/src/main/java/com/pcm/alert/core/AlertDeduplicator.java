package com.pcm.alert.core;

public interface AlertDeduplicator {
    boolean allow(AlertEvent event);

    void record(AlertEvent event);
}
