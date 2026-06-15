package com.pcm.alert.core;

public interface AlertPublisher {
    void publish(AlertMessage message);
}
