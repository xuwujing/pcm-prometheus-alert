package com.pcm.alert.starter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 告警仪表盘 API 控制器。
 * <p>
 * 内置在 starter 中，类似 Druid 的监控页面。
 * 通过 pcm.alert.dashboard.enabled=true 启用。
 * </p>
 */
@RestController
public class DashboardController {

    @GetMapping("/pcm-alert/api/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jvm", jvmInfo());
        result.put("system", systemInfo());
        result.put("alertStatus", alertStatus());
        return result;
    }

    private Map<String, Object> jvmInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        info.put("heapUsed", heap.getUsed());
        info.put("heapMax", heap.getMax());
        info.put("heapUsagePercent", heap.getMax() > 0
                ? String.format("%.1f", heap.getUsed() * 100.0 / heap.getMax()) : "N/A");

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        info.put("threadCount", threadBean.getThreadCount());
        info.put("peakThreadCount", threadBean.getPeakThreadCount());
        info.put("daemonThreadCount", threadBean.getDaemonThreadCount());
        return info;
    }

    private Map<String, Object> systemInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        info.put("osName", osBean.getName());
        info.put("osVersion", osBean.getVersion());
        info.put("availableProcessors", osBean.getAvailableProcessors());
        info.put("systemLoadAverage", String.format("%.2f", osBean.getSystemLoadAverage()));
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));
        return info;
    }

    private Map<String, Object> alertStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", true);
        status.put("exceptionAlert", "active");
        status.put("slowRequestAlert", "active");
        status.put("statusCodeAlert", "active");
        status.put("metricAlert", "active");
        return status;
    }
}
