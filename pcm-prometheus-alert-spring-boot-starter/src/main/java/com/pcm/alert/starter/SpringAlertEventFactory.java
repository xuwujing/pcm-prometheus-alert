package com.pcm.alert.starter;

import com.pcm.alert.core.AlertEvent;
import com.pcm.alert.core.AlertLevel;
import com.pcm.alert.core.AlertSource;
import com.pcm.alert.core.AlertType;
import org.springframework.core.env.Environment;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Spring 环境下的告警事件工厂。
 * <p>
 * 负责将框架对象（HttpServletRequest、Throwable、JVM 指标）转换为统一的 {@link AlertEvent}，
 * 自动填充服务名、环境、主机名、traceId 等上下文信息。
 * </p>
 */
public class SpringAlertEventFactory {
    private final AlertProperties properties;
    private final Environment environment;
    private final String hostName;

    public SpringAlertEventFactory(AlertProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
        this.hostName = resolveHostName();
    }

    /**
     * 从异常构造事件。
     * <p>
     * 堆栈按 stackTraceMaxLines 截断，超出部分丢弃。
     * </p>
     */
    public AlertEvent fromException(Throwable throwable, HttpServletRequest request) {
        AlertEvent event = baseEvent(AlertType.EXCEPTION, AlertLevel.ERROR, AlertSource.MVC);
        event.setSummary(throwable.getClass().getName() + ": " + safe(throwable.getMessage()));
        event.setDetail("Unhandled request exception");
        event.setStackTrace(stackTrace(throwable, properties.getException().getStackTraceMaxLines()));
        fillRequest(event, request);
        return event;
    }

    /**
     * 从慢请求构造事件。
     */
    public AlertEvent fromSlowRequest(HttpServletRequest request, long costMs) {
        AlertEvent event = baseEvent(AlertType.SLOW_REQUEST, AlertLevel.WARN, AlertSource.FILTER);
        event.setSummary("Slow request cost " + costMs + "ms");
        event.setDetail("Request exceeded threshold " + properties.getRequest().getSlowThresholdMs() + "ms");
        event.setCostMs(costMs);
        fillRequest(event, request);
        return event;
    }

    /**
     * 从 JVM 内存使用率构造事件。
     */
    public AlertEvent fromJvmMemory(double usage, long used, long max) {
        AlertEvent event = baseEvent(AlertType.JVM_MEMORY, AlertLevel.WARN, AlertSource.METRIC);
        event.setSummary("JVM memory usage " + String.format("%.2f", usage * 100) + "%");
        event.setDetail("used=" + used + ", max=" + max);
        return event;
    }

    /**
     * 从线程数构造事件。
     */
    public AlertEvent fromThreadCount(int count) {
        AlertEvent event = baseEvent(AlertType.THREAD_COUNT, AlertLevel.WARN, AlertSource.METRIC);
        event.setSummary("Thread count " + count);
        event.setDetail("threshold=" + properties.getMetric().getThreadThreshold());
        return event;
    }

    /**
     * 从 HTTP 错误状态码构造事件。
     */
    public AlertEvent fromHttpStatus(HttpServletRequest request, int statusCode, long costMs) {
        AlertEvent event = baseEvent(AlertType.HTTP_STATUS, AlertLevel.ERROR, AlertSource.FILTER);
        event.setSummary("HTTP status " + statusCode);
        event.setDetail("Response returned error status " + statusCode + ", cost " + costMs + "ms");
        event.setStatusCode(statusCode);
        event.setCostMs(costMs);
        fillRequest(event, request);
        return event;
    }

    /**
     * 从 CPU 使用率构造事件。
     */
    public AlertEvent fromCpuUsage(double usage) {
        AlertEvent event = baseEvent(AlertType.CPU_USAGE, AlertLevel.WARN, AlertSource.METRIC);
        event.setSummary("CPU usage " + String.format("%.2f", usage * 100) + "%");
        event.setDetail("threshold=" + properties.getMetric().getCpuThreshold());
        return event;
    }

    /**
     * 构造恢复事件 —— 指标回落后通知。
     */
    public AlertEvent recovery(AlertType type, String metricName) {
        AlertEvent event = baseEvent(type, AlertLevel.INFO, AlertSource.METRIC);
        event.setSummary(metricName + " recovered");
        event.setDetail(metricName + " has returned to normal");
        return event;
    }

    private AlertEvent baseEvent(AlertType type, AlertLevel level, AlertSource source) {
        AlertEvent event = new AlertEvent();
        event.setType(type);
        event.setLevel(level);
        event.setSource(source);
        event.setServiceName(resolveServiceName());
        event.setEnvironment(resolveEnvironment());
        event.setHost(hostName);
        return event;
    }

    private void fillRequest(AlertEvent event, HttpServletRequest request) {
        if (request == null) {
            return;
        }
        event.setRequestMethod(request.getMethod());
        event.setRequestPath(request.getRequestURI());
        event.setTraceId(resolveTraceId(request));
    }

    /**
     * 按优先级提取 traceId：X-B3-TraceId → traceId → X-Request-Id。
     */
    private String resolveTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-B3-TraceId");
        if (traceId == null || traceId.length() == 0) {
            traceId = request.getHeader("traceId");
        }
        if (traceId == null || traceId.length() == 0) {
            traceId = request.getHeader("X-Request-Id");
        }
        return traceId;
    }

    private String resolveServiceName() {
        if (hasText(properties.getServiceName())) {
            return properties.getServiceName();
        }
        return environment.getProperty("spring.application.name", "unknown");
    }

    private String resolveEnvironment() {
        if (hasText(properties.getEnvironment())) {
            return properties.getEnvironment();
        }
        String[] profiles = environment.getActiveProfiles();
        return profiles.length == 0 ? "default" : String.join(",", profiles);
    }

    private String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * 将异常堆栈转为字符串，按 maxLines 截断。
     */
    private String stackTrace(Throwable throwable, int maxLines) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        String full = stringWriter.toString();
        if (maxLines <= 0) {
            return full;
        }
        String[] lines = full.split("\\r?\\n");
        if (lines.length <= maxLines) {
            return full;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxLines; i++) {
            sb.append(lines[i]).append('\n');
        }
        sb.append("\t... (").append(lines.length - maxLines).append(" more lines)");
        return sb.toString();
    }

    /**
     * 判断异常是否在排除列表中。
     */
    public boolean isExcluded(Throwable throwable) {
        String[] excludeExceptions = properties.getException().getExcludeExceptions();
        if (excludeExceptions == null || excludeExceptions.length == 0) {
            return false;
        }
        String className = throwable.getClass().getName();
        for (String exclude : excludeExceptions) {
            if (className.equals(exclude)) {
                return true;
            }
        }
        return false;
    }
}
