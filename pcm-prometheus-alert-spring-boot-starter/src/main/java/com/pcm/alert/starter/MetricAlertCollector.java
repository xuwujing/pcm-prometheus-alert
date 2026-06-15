package com.pcm.alert.starter;

import com.pcm.alert.core.AlertManager;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;

public class MetricAlertCollector {
    private final AlertProperties properties;
    private final SpringAlertEventFactory eventFactory;
    private final AlertManager alertManager;

    public MetricAlertCollector(AlertProperties properties, SpringAlertEventFactory eventFactory, AlertManager alertManager) {
        this.properties = properties;
        this.eventFactory = eventFactory;
        this.alertManager = alertManager;
    }

    @Scheduled(fixedDelayString = "${pcm.alert.metric.interval-seconds:30}000")
    public void collect() {
        if (!properties.getMetric().isEnabled()) {
            return;
        }
        checkJvmMemory();
        checkThreadCount();
    }

    private void checkJvmMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        long max = heap.getMax();
        long used = heap.getUsed();
        if (max <= 0) {
            return;
        }
        double usage = used * 1.0D / max;
        if (usage >= properties.getMetric().getJvmMemoryThreshold()) {
            alertManager.onEvent(eventFactory.fromJvmMemory(usage, used, max));
        }
    }

    private void checkThreadCount() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        int count = threadMXBean.getThreadCount();
        if (count >= properties.getMetric().getThreadThreshold()) {
            alertManager.onEvent(eventFactory.fromThreadCount(count));
        }
    }
}
