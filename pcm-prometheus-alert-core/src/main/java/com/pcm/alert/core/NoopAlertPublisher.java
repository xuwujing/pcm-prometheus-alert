package com.pcm.alert.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoopAlertPublisher implements AlertPublisher {
    private static final Logger log = LoggerFactory.getLogger(NoopAlertPublisher.class);

    @Override
    public void publish(AlertMessage message) {
        log.warn("Alert webhook is empty. title={}, content={}", message.getTitle(), message.getContent());
    }
}
