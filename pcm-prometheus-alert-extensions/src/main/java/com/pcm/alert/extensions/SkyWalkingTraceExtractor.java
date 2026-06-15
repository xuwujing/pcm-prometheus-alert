package com.pcm.alert.extensions;

import com.pcm.alert.core.AlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SkyWalking 链路追踪集成。
 * <p>
 * 从 SkyWalking agent 上下文中提取 traceId，补充到告警事件中。
 * 如果 SkyWalking agent 未加载，则静默降级。
 * </p>
 */
public class SkyWalkingTraceExtractor {
    private static final Logger log = LoggerFactory.getLogger(SkyWalkingTraceExtractor.class);

    private final boolean available;

    public SkyWalkingTraceExtractor() {
        this.available = isSkyWalkingAvailable();
        if (available) {
            log.info("PCM Alert: SkyWalking trace integration enabled");
        } else {
            log.info("PCM Alert: SkyWalking agent not detected, trace integration disabled");
        }
    }

    /**
     * 尝试从 SkyWalking 上下文提取 traceId。
     * 如果不可用则返回 null。
     */
    public String extractTraceId() {
        if (!available) {
            return null;
        }
        try {
            // 通过反射调用 SkyWalking TraceContext.traceId()，避免编译期依赖
            Class<?> traceContextClass = Class.forName("org.apache.skywalking.apm.toolkit.trace.TraceContext");
            return (String) traceContextClass.getMethod("traceId").invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 补充告警事件的 traceId。
     */
    public void enrich(AlertEvent event) {
        if (event == null || event.getTraceId() != null) {
            return;
        }
        String traceId = extractTraceId();
        if (traceId != null && !traceId.isEmpty() && !"N/A".equals(traceId)) {
            event.setTraceId(traceId);
        }
    }

    private boolean isSkyWalkingAvailable() {
        try {
            Class.forName("org.apache.skywalking.apm.toolkit.trace.TraceContext");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
