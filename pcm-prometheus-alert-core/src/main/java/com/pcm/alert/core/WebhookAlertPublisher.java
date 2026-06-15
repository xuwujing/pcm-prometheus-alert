package com.pcm.alert.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class WebhookAlertPublisher implements AlertPublisher {
    private static final Logger log = LoggerFactory.getLogger(WebhookAlertPublisher.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int timeoutMs;

    public WebhookAlertPublisher(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    @Override
    public void publish(AlertMessage message) {
        if (message.getWebhook() == null || message.getWebhook().trim().isEmpty()) {
            log.warn("Alert webhook is empty. title={}", message.getTitle());
            return;
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL(message.getWebhook());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeoutMs);
            connection.setReadTimeout(timeoutMs);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            byte[] body = buildPayload(message).getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(body.length);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(body);
            }
            int status = connection.getResponseCode();
            if (status >= 400) {
                log.warn("Alert webhook returned non-success status. status={}, title={}", status, message.getTitle());
            }
        } catch (Exception e) {
            log.warn("Publish alert webhook failed. title={}, error={}", message.getTitle(), e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    protected String buildPayload(AlertMessage message) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", message.getTitle());
        payload.put("content", message.getContent());
        payload.put("level", message.getLevel());
        payload.put("type", message.getType());
        payload.put("occurredAt", String.valueOf(message.getOccurredAt()));
        payload.put("attributes", message.getAttributes());
        return objectMapper.writeValueAsString(payload);
    }
}
