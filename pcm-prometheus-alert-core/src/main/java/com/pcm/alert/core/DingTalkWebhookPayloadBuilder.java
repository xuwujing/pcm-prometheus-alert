package com.pcm.alert.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 钉钉机器人消息格式 payload 构建器。
 * <p>
 * 使用 markdown 类型消息，格式参考钉钉机器人开发文档。
 * </p>
 */
public class DingTalkWebhookPayloadBuilder implements WebhookPayloadBuilder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String build(AlertMessage message) throws Exception {
        String levelEmoji = levelEmoji(message.getLevel());
        String markdown = new StringBuilder()
                .append("## ").append(levelEmoji).append(" ").append(message.getTitle()).append("\n\n")
                .append("> 服务: ").append(attr(message, "serviceName")).append("\n\n")
                .append("> 环境: ").append(attr(message, "environment")).append("\n\n")
                .append("> 时间: ").append(message.getOccurredAt()).append("\n\n")
                .append("---\n\n")
                .append(message.getContent())
                .toString();

        Map<String, Object> markdownObj = new LinkedHashMap<>();
        markdownObj.put("title", message.getTitle());
        markdownObj.put("text", markdown);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgtype", "markdown");
        payload.put("markdown", markdownObj);
        return objectMapper.writeValueAsString(payload);
    }

    private String levelEmoji(AlertLevel level) {
        if (level == null) return "📢";
        switch (level) {
            case FATAL: return "🔥";
            case ERROR: return "❌";
            case WARN:  return "⚠️";
            case INFO:  return "ℹ️";
            default:    return "📢";
        }
    }

    private String attr(AlertMessage message, String key) {
        Object value = message.getAttributes().get(key);
        return value != null ? String.valueOf(value) : "-";
    }
}
