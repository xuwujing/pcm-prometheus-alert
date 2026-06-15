package com.pcm.alert.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 飞书机器人消息格式 payload 构建器。
 * <p>
 * 使用 interactive 卡片消息，格式参考飞书机器人开发文档。
 * </p>
 */
public class FeishuWebhookPayloadBuilder implements WebhookPayloadBuilder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String build(AlertMessage message) throws Exception {
        String levelColor = levelColor(message.getLevel());
        String levelText = levelText(message.getLevel());

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("title", new LinkedHashMap<String, Object>() {{
            put("tag", "plain_text");
            put("content", levelText + " " + message.getTitle());
        }});
        header.put("template", levelColor);

        List<Map<String, Object>> elements = new ArrayList<>();

        Map<String, Object> serviceField = field("服务", attr(message, "serviceName"));
        Map<String, Object> envField = field("环境", attr(message, "environment"));
        Map<String, Object> timeField = field("时间", String.valueOf(message.getOccurredAt()));

        elements.add(serviceField);
        elements.add(envField);
        elements.add(timeField);

        // 内容作为 markdown 块
        Map<String, Object> contentBlock = new LinkedHashMap<>();
        contentBlock.put("tag", "markdown");
        contentBlock.put("content", message.getContent());
        elements.add(contentBlock);

        Map<String, Object> card = new LinkedHashMap<>();
        card.put("header", header);
        card.put("elements", elements);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msg_type", "interactive");
        payload.put("card", card);
        return objectMapper.writeValueAsString(payload);
    }

    private Map<String, Object> field(String label, String value) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("tag", "div");
        Map<String, Object> text = new LinkedHashMap<>();
        text.put("tag", "lark_md");
        text.put("content", "**" + label + "**: " + value);
        field.put("text", text);
        return field;
    }

    private String levelColor(AlertLevel level) {
        if (level == null) return "grey";
        switch (level) {
            case FATAL: return "red";
            case ERROR: return "red";
            case WARN:  return "orange";
            case INFO:  return "blue";
            default:    return "grey";
        }
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
