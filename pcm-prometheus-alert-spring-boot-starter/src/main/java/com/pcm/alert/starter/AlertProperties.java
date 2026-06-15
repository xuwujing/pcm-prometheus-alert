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

    /** webhook 消息格式：default / dingtalk / feishu / wecom */
    private String webhookFormat = "default";

    /** 服务名（未配置时取 spring.application.name） */
    private String serviceName;

    /** 环境标识（未配置时取 spring.profiles.active） */
    private String environment;

    private Publisher publisher = new Publisher();
    private Dedupe dedupe = new Dedupe();
    private ExceptionAlert exception = new ExceptionAlert();
    private Request request = new Request();
    private Metric metric = new Metric();
    private Extensions extensions = new Extensions();
    private Custom custom = new Custom();
    private Dashboard dashboard = new Dashboard();

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

    public String getWebhookFormat() {
        return webhookFormat;
    }

    public void setWebhookFormat(String webhookFormat) {
        this.webhookFormat = webhookFormat;
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

    public Extensions getExtensions() {
        return extensions;
    }

    public void setExtensions(Extensions extensions) {
        this.extensions = extensions;
    }

    public Custom getCustom() {
        return custom;
    }

    public void setCustom(Custom custom) {
        this.custom = custom;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
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
        /** 排除的异常类名（全限定名），匹配的异常不触发告警 */
        private String[] excludeExceptions = new String[]{
                "org.springframework.web.bind.MissingServletRequestParameterException",
                "org.springframework.web.method.annotation.MethodArgumentTypeMismatchException",
                "org.springframework.web.HttpRequestMethodNotSupportedException",
                "org.springframework.web.HttpMediaTypeNotSupportedException"
        };
        /** 堆栈最大行数，默认 20，超过则截断 */
        private int stackTraceMaxLines = 20;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String[] getExcludeExceptions() {
            return excludeExceptions;
        }

        public void setExcludeExceptions(String[] excludeExceptions) {
            this.excludeExceptions = excludeExceptions;
        }

        public int getStackTraceMaxLines() {
            return stackTraceMaxLines;
        }

        public void setStackTraceMaxLines(int stackTraceMaxLines) {
            this.stackTraceMaxLines = stackTraceMaxLines;
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
        /** 是否启用 CPU 使用率告警，默认 false（需要较多采样周期） */
        private boolean cpuEnabled = false;
        /** CPU 使用率阈值（0-1），默认 0.8 */
        private double cpuThreshold = 0.8;
        /** 是否启用恢复事件（指标回落后发送恢复通知），默认 false */
        private boolean recoveryEnabled = false;

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

        public boolean isCpuEnabled() {
            return cpuEnabled;
        }

        public void setCpuEnabled(boolean cpuEnabled) {
            this.cpuEnabled = cpuEnabled;
        }

        public double getCpuThreshold() {
            return cpuThreshold;
        }

        public void setCpuThreshold(double cpuThreshold) {
            this.cpuThreshold = cpuThreshold;
        }

        public boolean isRecoveryEnabled() {
            return recoveryEnabled;
        }

        public void setRecoveryEnabled(boolean recoveryEnabled) {
            this.recoveryEnabled = recoveryEnabled;
        }
    }

    /** 扩展集成配置 —— 对接 Prometheus / SkyWalking / ELK 等外部系统 */
    public static class Extensions {
        /** 是否启用 Prometheus 指标暴露（通过 /actuator/prometheus） */
        private boolean prometheusEnabled = false;
        /** 是否启用 SkyWalking 链路追踪集成（读取 traceId 等） */
        private boolean skywalkingEnabled = false;
        /** 是否启用 ELK 日志集成（将告警事件写入日志，由 logstash 采集） */
        private boolean elkEnabled = false;

        public boolean isPrometheusEnabled() {
            return prometheusEnabled;
        }

        public void setPrometheusEnabled(boolean prometheusEnabled) {
            this.prometheusEnabled = prometheusEnabled;
        }

        public boolean isSkywalkingEnabled() {
            return skywalkingEnabled;
        }

        public void setSkywalkingEnabled(boolean skywalkingEnabled) {
            this.skywalkingEnabled = skywalkingEnabled;
        }

        public boolean isElkEnabled() {
            return elkEnabled;
        }

        public void setElkEnabled(boolean elkEnabled) {
            this.elkEnabled = elkEnabled;
        }
    }

    /** 自定义告警配置 —— 多 webhook 路由 + 自定义事件 */
    public static class Custom {
        /** 是否启用自定义告警 API（通过 AlertManager 手动触发），默认 true */
        private boolean enabled = true;
        /** 按告警级别路由到不同 webhook */
        private boolean levelRoutingEnabled = false;
        /** 各告警级别的 webhook 地址 */
        private String fatalWebhook;
        private String errorWebhook;
        private String warnWebhook;
        private String infoWebhook;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isLevelRoutingEnabled() {
            return levelRoutingEnabled;
        }

        public void setLevelRoutingEnabled(boolean levelRoutingEnabled) {
            this.levelRoutingEnabled = levelRoutingEnabled;
        }

        public String getFatalWebhook() {
            return fatalWebhook;
        }

        public void setFatalWebhook(String fatalWebhook) {
            this.fatalWebhook = fatalWebhook;
        }

        public String getErrorWebhook() {
            return errorWebhook;
        }

        public void setErrorWebhook(String errorWebhook) {
            this.errorWebhook = errorWebhook;
        }

        public String getWarnWebhook() {
            return warnWebhook;
        }

        public void setWarnWebhook(String warnWebhook) {
            this.warnWebhook = warnWebhook;
        }

        public String getInfoWebhook() {
            return infoWebhook;
        }

        public void setInfoWebhook(String infoWebhook) {
            this.infoWebhook = infoWebhook;
        }
    }

    /** 仪表盘配置 —— 内置监控页面，类似 Druid 监控 */
    public static class Dashboard {
        /** 是否启用仪表盘，默认 true */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
