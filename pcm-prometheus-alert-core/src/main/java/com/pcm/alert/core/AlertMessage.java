package com.pcm.alert.core;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 告警消息模型 —— 渲染后的可推送消息。
 * <p>
 * 由 {@link AlertMessageRenderer} 从 {@link AlertEvent} 生成，
 * 包含标题、内容、webhook 地址和扩展属性。
 * </p>
 */
public class AlertMessage {

    /** 消息标题 */
    private String title;

    /** 消息正文 */
    private String content;

    /** 告警级别 */
    private AlertLevel level;

    /** 告警类型 */
    private AlertType type;

    /** 目标 webhook 地址 */
    private String webhook;

    /** Webhook 消息格式，默认 DEFAULT */
    private WebhookFormat webhookFormat = WebhookFormat.DEFAULT;

    /** 模板名称（预留） */
    private String templateName = "default";

    /** 接收人（预留） */
    private String receiver;

    /** 发生时间 */
    private Instant occurredAt = Instant.now();

    /** 扩展属性（eventId、serviceName、traceId 等） */
    private Map<String, Object> attributes = new LinkedHashMap<>();

    // ---- getters / setters ----

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public AlertLevel getLevel() {
        return level;
    }

    public void setLevel(AlertLevel level) {
        this.level = level;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public WebhookFormat getWebhookFormat() {
        return webhookFormat;
    }

    public void setWebhookFormat(WebhookFormat webhookFormat) {
        this.webhookFormat = webhookFormat;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
