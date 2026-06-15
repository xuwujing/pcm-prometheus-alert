package com.pcm.alert.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 企业微信机器人消息格式 payload 构建器。
 * <p>
 * 使用 markdown 类型消息，格式参考企业微信机器人开发文档。
 * </p>
 */
public class WeComWebhookPayloadBuilder implements WebhookPayloadBuilder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String build(AlertMessage message) throws Exception {
        String levelText = levelText(message.getLevel());
        String markdown = new StringBuilder()
                .append("# ").append(levelText).append(" ").append(message.getTitle()).append("\n")
                .append("> 服务: <font color=\"comment\">").append(attr(message, "serviceName")).append("</font>\n")
                .append("> 环境: <font color=\"comment\">").append(attr(message, "environment")).append("</font>\n")
                .append("> 时间: <font color=\"comment\">").append(message.getOccurredAt()).append("</font>\n")
                .append("\n")
                .append(message.getContent())
                .toString();

        Map<String, Object> markdownObj = new LinkedHashMap<>();
        markdownObj.put("content", markdown);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgtype", "markdown");
        payload.put("markdown", markdownObj);
        return objectMapper.writeValueAsString(payload);
    }

    private String levelText(AlertLevel level) {
        if (level == null) return "通知";
        switch (level) {
            case FATAL: return "🔥致命";
            case ERROR: return "❌错误";
            case WARN:  return "⚠️警告";
            case INFO:  return "ℹ️通知";
            default:    return "通知";
        }
    }

    private String attr(AlertMessage message, String key) {
        Object value = message.getAttributes().get(key);
        return value != null ? String.valueOf(value) : "-";
    }
}
