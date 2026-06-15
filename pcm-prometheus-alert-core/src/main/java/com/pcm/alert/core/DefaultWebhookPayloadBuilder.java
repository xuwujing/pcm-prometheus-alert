package com.pcm.alert.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 默认 JSON 格式 payload 构建器。
 */
public class DefaultWebhookPayloadBuilder implements WebhookPayloadBuilder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String build(AlertMessage message) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", message.getTitle());
        payload.put("content", message.getContent());
        payload.put("level", message.getLevel() != null ? message.getLevel().name() : null);
        payload.put("type", message.getType() != null ? message.getType().name() : null);
        payload.put("occurredAt", String.valueOf(message.getOccurredAt()));
        payload.put("attributes", message.getAttributes());
        return objectMapper.writeValueAsString(payload);
    }
}
