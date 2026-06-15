package com.pcm.alert.core;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 统一告警事件模型。
 * <p>
 * 覆盖异常、慢请求、HTTP 状态码、JVM 指标、线程等事件类型，
 * 由采集器构造后送入 {@link AlertManager} 统一处理。
 * </p>
 */
public class AlertEvent {

    /** 事件唯一标识，默认 UUID */
    private String eventId = UUID.randomUUID().toString();

    /** 告警类型 */
    private AlertType type;

    /** 告警级别，默认 WARN */
    private AlertLevel level = AlertLevel.WARN;

    /** 事件来源 */
    private AlertSource source = AlertSource.MANUAL;

    /** 服务名 */
    private String serviceName;

    /** 环境标识 */
    private String environment;

    /** 主机名 */
    private String host;

    /** 分布式链路 traceId */
    private String traceId;

    /** 告警摘要（短描述） */
    private String summary;

    /** 告警详情（补充信息） */
    private String detail;

    /** 自定义标签 */
    private Map<String, String> tags = new LinkedHashMap<>();

    /** 事件发生时间 */
    private Instant occurredAt = Instant.now();

    /** 异常堆栈（仅异常事件） */
    private String stackTrace;

    /** 请求耗时（毫秒），仅请求类事件 */
    private long costMs;

    /** 请求路径 */
    private String requestPath;

    /** 请求方法（GET/POST 等） */
    private String requestMethod;

    /** HTTP 响应状态码（仅 HTTP_STATUS 事件） */
    private int statusCode;

    // ---- getters / setters ----

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public AlertLevel getLevel() {
        return level;
    }

    public void setLevel(AlertLevel level) {
        this.level = level;
    }

    public AlertSource getSource() {
        return source;
    }

    public void setSource(AlertSource source) {
        this.source = source;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public long getCostMs() {
        return costMs;
    }

    public void setCostMs(long costMs) {
        this.costMs = costMs;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
