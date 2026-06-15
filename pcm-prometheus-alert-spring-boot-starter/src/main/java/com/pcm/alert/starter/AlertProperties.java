package com.pcm.alert.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pcm.alert")
public class AlertProperties {
    private boolean enabled = false;
    private String webhook;
    private String serviceName;
    private String environment;
    private Publisher publisher = new Publisher();
    private Dedupe dedupe = new Dedupe();
    private ExceptionAlert exception = new ExceptionAlert();
    private Request request = new Request();
    private Metric metric = new Metric();

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

    public static class Publisher {
        private boolean async = true;
        private int queueSize = 1000;
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

    public static class Dedupe {
        private boolean enabled = true;
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

    public static class ExceptionAlert {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Request {
        private boolean enabled = true;
        private long slowThresholdMs = 1000;

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
    }

    public static class Metric {
        private boolean enabled = true;
        private long intervalSeconds = 30;
        private double jvmMemoryThreshold = 0.8;
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
