package com.pcm.alert.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单去重器 —— 基于内存的冷却窗口去重。
 * <p>
 * 按 type|serviceName|requestPath|summary 组合 key，
 * 同一 key 在 cooldownSeconds 内只允许发送一次。
 * </p>
 */
public class SimpleAlertDeduplicator implements AlertDeduplicator {
    private final boolean enabled;
    private final long cooldownSeconds;

    /** key → 上次发送时间 */
    private final Map<String, Instant> lastSentAt = new ConcurrentHashMap<>();

    public SimpleAlertDeduplicator(boolean enabled, long cooldownSeconds) {
        this.enabled = enabled;
        this.cooldownSeconds = cooldownSeconds;
    }

    @Override
    public boolean allow(AlertEvent event) {
        if (!enabled) {
            return true;
        }
        Instant last = lastSentAt.get(buildKey(event));
        return last == null || Duration.between(last, Instant.now()).getSeconds() >= cooldownSeconds;
    }

    @Override
    public void record(AlertEvent event) {
        if (enabled) {
            lastSentAt.put(buildKey(event), Instant.now());
        }
    }

    /**
     * 构建去重 key：type|serviceName|requestPath|summary。
     */
    private String buildKey(AlertEvent event) {
        StringBuilder key = new StringBuilder();
        key.append(event.getType()).append('|');
        key.append(nullToEmpty(event.getServiceName())).append('|');
        key.append(nullToEmpty(event.getRequestPath())).append('|');
        key.append(nullToEmpty(event.getSummary()));
        return key.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
