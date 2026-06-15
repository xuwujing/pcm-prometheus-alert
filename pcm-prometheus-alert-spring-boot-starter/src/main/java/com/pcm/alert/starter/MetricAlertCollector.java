package com.pcm.alert.starter;

import com.pcm.alert.core.AlertManager;
import com.pcm.alert.core.AlertType;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

/**
 * 指标告警采集器 —— 通过 @Scheduled 定时采样 JVM 指标。
 * <p>
 * 当前支持：
 * <ul>
 *   <li>JVM 堆内存使用率（超过 jvmMemoryThreshold 触发）</li>
 *   <li>线程数（超过 threadThreshold 触发）</li>
 *   <li>CPU 使用率（超过 cpuThreshold 触发，默认关闭）</li>
 * </ul>
 * 恢复事件：指标从超阈值回落到正常范围时，发送 INFO 级别恢复通知（默认关闭）。
 * </p>
 */
public class MetricAlertCollector {
    private final AlertProperties properties;
    private final SpringAlertEventFactory eventFactory;
    private final AlertManager alertManager;

    /** 上一次是否处于告警状态（用于判断恢复） */
    private volatile boolean jvmMemoryAlerted;
    private volatile boolean threadAlerted;
    private volatile boolean cpuAlerted;

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
        if (properties.getMetric().isCpuEnabled()) {
            checkCpuUsage();
        }
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
        boolean overThreshold = usage >= properties.getMetric().getJvmMemoryThreshold();
        if (overThreshold) {
            alertManager.onEvent(eventFactory.fromJvmMemory(usage, used, max));
            jvmMemoryAlerted = true;
        } else if (jvmMemoryAlerted && properties.getMetric().isRecoveryEnabled()) {
            alertManager.onEvent(eventFactory.recovery(AlertType.JVM_MEMORY, "JVM memory"));
            jvmMemoryAlerted = false;
        }
    }

    private void checkThreadCount() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        int count = threadMXBean.getThreadCount();
        boolean overThreshold = count >= properties.getMetric().getThreadThreshold();
        if (overThreshold) {
            alertManager.onEvent(eventFactory.fromThreadCount(count));
            threadAlerted = true;
        } else if (threadAlerted && properties.getMetric().isRecoveryEnabled()) {
            alertManager.onEvent(eventFactory.recovery(AlertType.THREAD_COUNT, "Thread count"));
            threadAlerted = false;
        }
    }

    private void checkCpuUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemLoadAverage();
        // getSystemLoadAverage 返回 -1 表示不可用
        if (cpuLoad < 0) {
            return;
        }
        int processors = Runtime.getRuntime().availableProcessors();
        double usage = cpuLoad / processors;
        boolean overThreshold = usage >= properties.getMetric().getCpuThreshold();
        if (overThreshold) {
            alertManager.onEvent(eventFactory.fromCpuUsage(usage));
            cpuAlerted = true;
        } else if (cpuAlerted && properties.getMetric().isRecoveryEnabled()) {
            alertManager.onEvent(eventFactory.recovery(AlertType.CPU_USAGE, "CPU usage"));
            cpuAlerted = false;
        }
    }
}
