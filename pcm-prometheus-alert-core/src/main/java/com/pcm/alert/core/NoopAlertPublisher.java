package com.pcm.alert.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 空推送器 —— webhook 未配置时的兜底实现。
 * <p>
 * 仅记录 WARN 日志，不执行任何实际推送。
 * </p>
 */
public class NoopAlertPublisher implements AlertPublisher {
    private static final Logger log = LoggerFactory.getLogger(NoopAlertPublisher.class);

    @Override
    public void publish(AlertMessage message) {
        log.warn("Alert webhook is empty. title={}, content={}", message.getTitle(), message.getContent());
    }
}
