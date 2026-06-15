package com.pcm.alert.core;

public interface AlertEventCollector {
    boolean supports();

    void start();

    void stop();
}
