package com.pcm.alert.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Webhook 推送器 —— 通过 HTTP POST 发送 JSON 告警消息。
 * <p>
 * 支持多 IM 平台格式：默认 JSON、钉钉、飞书、企业微信。
 * 使用 JDK 原生 HttpURLConnection，无额外依赖。
 * 推送失败仅记录 WARN 日志，不向上抛出异常。
 * </p>
 */
public class WebhookAlertPublisher implements AlertPublisher {
    private static final Logger log = LoggerFactory.getLogger(WebhookAlertPublisher.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int timeoutMs;
    private final EnumMap<WebhookFormat, WebhookPayloadBuilder> payloadBuilders = new EnumMap<>(WebhookFormat.class);

    public WebhookAlertPublisher(int timeoutMs) {
        this.timeoutMs = timeoutMs;
        registerDefaults();
    }

    /**
     * 注册自定义 payload 构建器。
     */
    public void registerPayloadBuilder(WebhookFormat format, WebhookPayloadBuilder builder) {
        payloadBuilders.put(format, builder);
    }

    private void registerDefaults() {
        payloadBuilders.put(WebhookFormat.DEFAULT, new DefaultWebhookPayloadBuilder());
        payloadBuilders.put(WebhookFormat.DINGTALK, new DingTalkWebhookPayloadBuilder());
        payloadBuilders.put(WebhookFormat.FEISHU, new FeishuWebhookPayloadBuilder());
        payloadBuilders.put(WebhookFormat.WECOM, new WeComWebhookPayloadBuilder());
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

    /**
     * 根据消息的 webhookFormat 选择对应的 payload 构建器。
     */
    protected String buildPayload(AlertMessage message) throws Exception {
        WebhookFormat format = message.getWebhookFormat() != null ? message.getWebhookFormat() : WebhookFormat.DEFAULT;
        WebhookPayloadBuilder builder = payloadBuilders.getOrDefault(format, payloadBuilders.get(WebhookFormat.DEFAULT));
        return builder.build(message);
    }
}
