package com.pcm.alert.sql;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SQL 告警配置属性。
 */
@ConfigurationProperties(prefix = "pcm.alert.sql")
public class SqlAlertProperties {

    /** 是否启用 SQL 告警，默认 false */
    private boolean enabled = false;

    /** 慢 SQL 阈值（毫秒），默认 1000 */
    private long slowSqlThresholdMs = 1000;

    /** 是否启用数据源连接告警 */
    private boolean datasourceAlertEnabled = true;

    /** 数据源活跃连接数阈值 */
    private int activeCountThreshold = 50;

    /** 数据源等待连接数阈值 */
    private int waitThreadCountThreshold = 10;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getSlowSqlThresholdMs() {
        return slowSqlThresholdMs;
    }

    public void setSlowSqlThresholdMs(long slowSqlThresholdMs) {
        this.slowSqlThresholdMs = slowSqlThresholdMs;
    }

    public boolean isDatasourceAlertEnabled() {
        return datasourceAlertEnabled;
    }

    public void setDatasourceAlertEnabled(boolean datasourceAlertEnabled) {
        this.datasourceAlertEnabled = datasourceAlertEnabled;
    }

    public int getActiveCountThreshold() {
        return activeCountThreshold;
    }

    public void setActiveCountThreshold(int activeCountThreshold) {
        this.activeCountThreshold = activeCountThreshold;
    }

    public int getWaitThreadCountThreshold() {
        return waitThreadCountThreshold;
    }

    public void setWaitThreadCountThreshold(int waitThreadCountThreshold) {
        this.waitThreadCountThreshold = waitThreadCountThreshold;
    }
}
