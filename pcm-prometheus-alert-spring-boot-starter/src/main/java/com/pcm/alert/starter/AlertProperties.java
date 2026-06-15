package com.pcm.alert.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 告警配置属性 —— 映射 yml 中 pcm.alert.* 配置。
 * <p>
 * 支持按子模块独立开关：exception / request / metric / dedupe / publisher。
 * </p>
 */
@ConfigurationProperties(prefix = "pcm.alert")
public class AlertProperties {

    /** 总开关，默认 false */
    private boolean enabled = false;

    /** webhook 地址 */
    private String webhook;

    /** 服务名（未配置时取 spring.application.name） */
    private String serviceName;

    /** 环境标识（未配置时取 spring.profiles.active） */
    private String environment;

    private Publisher publisher = new Publisher();
    private Dedupe dedupe = new Dedupe();
    private ExceptionAlert exception = new ExceptionAlert();
    private Request request = new Request();
    private Metric metric = new Metric();

    // ---- getters / setters ----

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
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

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public Dedupe getDedupe() {
        return dedupe;
    }

    public void setDedupe(Dedupe dedupe) {
        this.dedupe = dedupe;
    }

    public ExceptionAlert getException() {
        return exception;
    }

    public void setException(ExceptionAlert exception) {
        this.exception = exception;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    /** 推送器配置 */
    public static class Publisher {
        /** 是否异步推送，默认 true */
        private boolean async = true;
        /** 异步队列容量，默认 1000 */
        private int queueSize = 1000;
        /** HTTP 超时（毫秒），默认 3000 */
        private int timeoutMs = 3000;

        public boolean isAsync() {
            return async;
        }

        public void setAsync(boolean async) {
            this.async = async;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public void setQueueSize(int queueSize) {
            this.queueSize = queueSize;
        }

        public int getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }

    /** 去重配置 */
    public static class Dedupe {
        /** 是否启用去重，默认 true */
        private boolean enabled = true;
        /** 冷却时间（秒），默认 300 */
        private long cooldownSeconds = 300;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getCooldownSeconds() {
            return cooldownSeconds;
        }

        public void setCooldownSeconds(long cooldownSeconds) {
            this.cooldownSeconds = cooldownSeconds;
        }
    }

    /** 异常告警配置 */
    public static class ExceptionAlert {
        /** 是否启用异常告警，默认 true */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /** 请求告警配置 */
    public static class Request {
        /** 是否启用请求告警，默认 true */
        private boolean enabled = true;
        /** 慢请求阈值（毫秒），默认 1000 */
        private long slowThresholdMs = 1000;
        /** 是否启用 HTTP 状态码告警，默认 true */
        private boolean statusCodeAlertEnabled = true;
        /** 触发告警的状态码列表，默认 [500, 502, 503, 504] */
        private int[] statusCodeAlertThresholds = new int[]{500, 502, 503, 504};
        /** 排除路径（Ant 风格），默认排除 actuator/health/favicon */
        private String[] excludePaths = new String[]{
                "/actuator/**",
                "/health",
                "/favicon.ico"
        };

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getSlowThresholdMs() {
            return slowThresholdMs;
        }

        public void setSlowThresholdMs(long slowThresholdMs) {
            this.slowThresholdMs = slowThresholdMs;
        }

        public boolean isStatusCodeAlertEnabled() {
            return statusCodeAlertEnabled;
        }

        public void setStatusCodeAlertEnabled(boolean statusCodeAlertEnabled) {
            this.statusCodeAlertEnabled = statusCodeAlertEnabled;
        }

        public int[] getStatusCodeAlertThresholds() {
            return statusCodeAlertThresholds;
        }

        public void setStatusCodeAlertThresholds(int[] statusCodeAlertThresholds) {
            this.statusCodeAlertThresholds = statusCodeAlertThresholds;
        }

        public String[] getExcludePaths() {
            return excludePaths;
        }

        public void setExcludePaths(String[] excludePaths) {
            this.excludePaths = excludePaths;
        }
    }

    /** 指标告警配置 */
    public static class Metric {
        /** 是否启用指标告警，默认 true */
        private boolean enabled = true;
        /** 采集间隔（秒），默认 30 */
        private long intervalSeconds = 30;
        /** JVM 堆内存使用率阈值（0-1），默认 0.8 */
        private double jvmMemoryThreshold = 0.8;
        /** 线程数阈值，默认 500 */
        private int threadThreshold = 500;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getIntervalSeconds() {
            return intervalSeconds;
        }

        public void setIntervalSeconds(long intervalSeconds) {
            this.intervalSeconds = intervalSeconds;
        }

        public double getJvmMemoryThreshold() {
            return jvmMemoryThreshold;
        }

        public void setJvmMemoryThreshold(double jvmMemoryThreshold) {
            this.jvmMemoryThreshold = jvmMemoryThreshold;
        }

        public int getThreadThreshold() {
            return threadThreshold;
        }

        public void setThreadThreshold(int threadThreshold) {
            this.threadThreshold = threadThreshold;
        }
    }
}
