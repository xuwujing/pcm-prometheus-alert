package com.pcm.alert.core;

/**
 * 默认消息渲染器 —— 将事件格式化为 key: value 文本。
 * <p>
 * 输出格式：标题为 [LEVEL] TYPE - serviceName，
 * 内容为 service/env/host/traceId/path/method/statusCode/costMs/summary/detail/stackTrace 的逐行拼接。
 * 堆栈截断至 4000 字符。
 * </p>
 */
public class DefaultAlertMessageRenderer implements AlertMessageRenderer {
    private final String webhook;
    private final WebhookFormat webhookFormat;

    public DefaultAlertMessageRenderer(String webhook, WebhookFormat webhookFormat) {
        this.webhook = webhook;
        this.webhookFormat = webhookFormat;
    }

    @Override
    public AlertMessage render(AlertEvent event) {
        AlertMessage message = new AlertMessage();
        message.setType(event.getType());
        message.setLevel(event.getLevel());
        message.setWebhook(webhook);
        message.setWebhookFormat(webhookFormat);
        message.setOccurredAt(event.getOccurredAt());
        message.setTitle("[" + event.getLevel() + "] " + event.getType() + " - " + safe(event.getServiceName()));
        message.setContent(buildContent(event));
        message.getAttributes().put("eventId", event.getEventId());
        message.getAttributes().put("serviceName", event.getServiceName());
        message.getAttributes().put("environment", event.getEnvironment());
        message.getAttributes().put("host", event.getHost());
        message.getAttributes().put("traceId", event.getTraceId());
        return message;
    }

    private String buildContent(AlertEvent event) {
        StringBuilder content = new StringBuilder();
        append(content, "service", event.getServiceName());
        append(content, "env", event.getEnvironment());
        append(content, "host", event.getHost());
        append(content, "traceId", event.getTraceId());
        append(content, "path", event.getRequestPath());
        append(content, "method", event.getRequestMethod());
        if (event.getStatusCode() > 0) {
            append(content, "statusCode", String.valueOf(event.getStatusCode()));
        }
        if (event.getCostMs() > 0) {
            append(content, "costMs", String.valueOf(event.getCostMs()));
        }
        append(content, "summary", event.getSummary());
        append(content, "detail", event.getDetail());
        if (event.getStackTrace() != null && event.getStackTrace().length() > 0) {
            append(content, "stackTrace", trim(event.getStackTrace(), 4000));
        }
        return content.toString();
    }

    private void append(StringBuilder builder, String name, String value) {
        if (value != null && value.length() > 0) {
            builder.append(name).append(": ").append(value).append('\n');
        }
    }

    private String trim(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private String safe(String value) {
        return value == null ? "unknown" : value;
    }
}
