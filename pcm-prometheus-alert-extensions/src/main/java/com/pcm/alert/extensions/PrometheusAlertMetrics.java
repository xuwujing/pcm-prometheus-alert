package com.pcm.alert.extensions;

import com.pcm.alert.core.AlertEvent;
import com.pcm.alert.core.AlertManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prometheus 指标暴露器。
 * <p>
 * 将告警事件计数暴露为 Prometheus 指标，可通过 /actuator/prometheus 采集。
 * 指标命名：pcm_alert_total{type, level, service}
 * </p>
 */
public class PrometheusAlertMetrics {
    private static final Logger log = LoggerFactory.getLogger(PrometheusAlertMetrics.class);

    private final MeterRegistry meterRegistry;
    private final String serviceName;

    public PrometheusAlertMetrics(MeterRegistry meterRegistry, String serviceName) {
        this.meterRegistry = meterRegistry;
        this.serviceName = serviceName;
        log.info("PCM Alert: Prometheus metrics registered for service '{}'", serviceName);
    }

    /**
     * 记录告警事件到 Prometheus Counter。
     */
    public void record(AlertEvent event) {
        if (event == null || meterRegistry == null) {
            return;
        }
        Counter.builder("pcm_alert_total")
                .description("Total alert events")
                .tag("type", event.getType() != null ? event.getType().name() : "unknown")
                .tag("level", event.getLevel() != null ? event.getLevel().name() : "unknown")
                .tag("service", serviceName)
                .register(meterRegistry)
                .increment();
    }
}
