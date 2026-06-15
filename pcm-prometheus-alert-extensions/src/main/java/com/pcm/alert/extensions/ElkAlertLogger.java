package com.pcm.alert.extensions;

import com.pcm.alert.core.AlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ELK 日志集成。
 * <p>
 * 将告警事件以结构化 JSON 格式写入日志，供 logstash/filebeat 采集。
 * 日志 key 使用 pcm.alert.event，便于 logstash 过滤。
 * </p>
 */
public class ElkAlertLogger {
    private static final Logger log = LoggerFactory.getLogger(ElkAlertLogger.class);

    public ElkAlertLogger() {
        log.info("PCM Alert: ELK integration enabled (alert events logged as JSON)");
    }

    /**
     * 将告警事件以 JSON 格式写入日志。
     */
    public void logEvent(AlertEvent event) {
        if (event == null) {
            return;
        }
        String json = toJson(event);
        log.info("pcm.alert.event: {}", json);
    }

    private String toJson(AlertEvent event) {
        StringBuilder sb = new StringBuilder("{");
        append(sb, "eventId", event.getEventId());
        append(sb, "type", event.getType() != null ? event.getType().name() : null);
        append(sb, "level", event.getLevel() != null ? event.getLevel().name() : null);
        append(sb, "source", event.getSource() != null ? event.getSource().name() : null);
        append(sb, "serviceName", event.getServiceName());
        append(sb, "environment", event.getEnvironment());
        append(sb, "host", event.getHost());
        append(sb, "traceId", event.getTraceId());
        append(sb, "summary", event.getSummary());
        append(sb, "requestPath", event.getRequestPath());
        append(sb, "requestMethod", event.getRequestMethod());
        append(sb, "costMs", event.getCostMs() > 0 ? String.valueOf(event.getCostMs()) : null);
        append(sb, "statusCode", event.getStatusCode() > 0 ? String.valueOf(event.getStatusCode()) : null);
        // remove trailing comma
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }

    private void append(StringBuilder sb, String key, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append("\"").append(key).append("\":\"").append(escape(value)).append("\",");
        }
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
