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

public class SpringAlertEventFactory {
    private final AlertProperties properties;
    private final Environment environment;
    private final String hostName;

    public SpringAlertEventFactory(AlertProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
        this.hostName = resolveHostName();
    }

    public AlertEvent fromException(Throwable throwable, HttpServletRequest request) {
        AlertEvent event = baseEvent(AlertType.EXCEPTION, AlertLevel.ERROR, AlertSource.MVC);
        event.setSummary(throwable.getClass().getName() + ": " + safe(throwable.getMessage()));
        event.setDetail("Unhandled request exception");
        event.setStackTrace(stackTrace(throwable));
        fillRequest(event, request);
        return event;
    }

    public AlertEvent fromSlowRequest(HttpServletRequest request, long costMs) {
        AlertEvent event = baseEvent(AlertType.SLOW_REQUEST, AlertLevel.WARN, AlertSource.FILTER);
        event.setSummary("Slow request cost " + costMs + "ms");
        event.setDetail("Request exceeded threshold " + properties.getRequest().getSlowThresholdMs() + "ms");
        event.setCostMs(costMs);
        fillRequest(event, request);
        return event;
    }

    public AlertEvent fromJvmMemory(double usage, long used, long max) {
        AlertEvent event = baseEvent(AlertType.JVM_MEMORY, AlertLevel.WARN, AlertSource.METRIC);
        event.setSummary("JVM memory usage " + String.format("%.2f", usage * 100) + "%");
        event.setDetail("used=" + used + ", max=" + max);
        return event;
    }

    public AlertEvent fromThreadCount(int count) {
        AlertEvent event = baseEvent(AlertType.THREAD_COUNT, AlertLevel.WARN, AlertSource.METRIC);
        event.setSummary("Thread count " + count);
        event.setDetail("threshold=" + properties.getMetric().getThreadThreshold());
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

    private String stackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
