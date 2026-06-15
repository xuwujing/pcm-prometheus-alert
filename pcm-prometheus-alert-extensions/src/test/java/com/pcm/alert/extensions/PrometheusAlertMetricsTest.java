package com.pcm.alert.extensions;

import com.pcm.alert.core.AlertEvent;
import com.pcm.alert.core.AlertLevel;
import com.pcm.alert.core.AlertType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Assert;
import org.junit.Test;

public class PrometheusAlertMetricsTest {

    @Test
    public void shouldRecordAlertEvent() {
        MeterRegistry registry = new SimpleMeterRegistry();
        PrometheusAlertMetrics m = new PrometheusAlertMetrics(registry, "test-service");

        AlertEvent event = new AlertEvent();
        event.setType(AlertType.EXCEPTION);
        event.setLevel(AlertLevel.ERROR);

        m.record(event);

        double count = registry.get("pcm_alert_total")
                .tag("type", "EXCEPTION")
                .tag("level", "ERROR")
                .tag("service", "test-service")
                .counter()
                .count();
        Assert.assertEquals(1.0, count, 0.001);
    }

    @Test
    public void shouldHandleNullEvent() {
        MeterRegistry registry = new SimpleMeterRegistry();
        PrometheusAlertMetrics m = new PrometheusAlertMetrics(registry, "test-service");
        m.record(null);
        // should not throw
    }

    @Test
    public void shouldHandleNullTypeAndLevel() {
        MeterRegistry registry = new SimpleMeterRegistry();
        PrometheusAlertMetrics m = new PrometheusAlertMetrics(registry, "test-service");

        AlertEvent event = new AlertEvent();
        m.record(event);

        // 验证 counter 存在且值为 1
        double count = registry.get("pcm_alert_total").counter().count();
        Assert.assertEquals(1.0, count, 0.001);
    }

    @Test
    public void shouldIncrementCounter() {
        MeterRegistry registry = new SimpleMeterRegistry();
        PrometheusAlertMetrics m = new PrometheusAlertMetrics(registry, "test-service");

        AlertEvent event = new AlertEvent();
        event.setType(AlertType.SLOW_REQUEST);
        event.setLevel(AlertLevel.WARN);

        m.record(event);
        m.record(event);
        m.record(event);

        double count = registry.get("pcm_alert_total")
                .tag("type", "SLOW_REQUEST")
                .tag("level", "WARN")
                .counter()
                .count();
        Assert.assertEquals(3.0, count, 0.001);
    }
}
