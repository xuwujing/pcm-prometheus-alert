package com.pcm.alert.sql;

import com.pcm.alert.core.AlertEvent;
import com.pcm.alert.core.AlertLevel;
import com.pcm.alert.core.AlertSource;
import com.pcm.alert.core.AlertType;

/**
 * SQL 告警事件适配器 —— 将 Druid 事件转换为统一告警事件。
 */
public class DruidEventAdapter {

    private final SqlAlertProperties properties;
    private final String serviceName;
    private final String environment;
    private final String host;

    public DruidEventAdapter(SqlAlertProperties properties, String serviceName, String environment, String host) {
        this.properties = properties;
        this.serviceName = serviceName;
        this.environment = environment;
        this.host = host;
    }

    /**
     * 从 Druid FilterEvent 构造慢 SQL 告警事件。
     *
     * @param sql        SQL 语句（截断至 500 字符）
     * @param costMs     执行耗时（毫秒）
     * @param dataSource 数据源名称
     */
    public AlertEvent fromSlowSql(String sql, long costMs, String dataSource) {
        AlertEvent event = new AlertEvent();
        event.setType(AlertType.SLOW_SQL);
        event.setLevel(AlertLevel.WARN);
        event.setSource(AlertSource.SQL);
        event.setServiceName(serviceName);
        event.setEnvironment(environment);
        event.setHost(host);
        event.setCostMs(costMs);
        event.setSummary("Slow SQL cost " + costMs + "ms on " + dataSource);
        event.setDetail("SQL: " + truncate(sql, 500) + "\nthreshold=" + properties.getSlowSqlThresholdMs() + "ms");
        return event;
    }

    /**
     * 从 Druid 数据源统计构造数据源告警事件。
     */
    public AlertEvent fromDatasource(String dataSourceName, int activeCount, int waitThreadCount) {
        AlertEvent event = new AlertEvent();
        event.setType(AlertType.DATASOURCE);
        event.setLevel(AlertLevel.WARN);
        event.setSource(AlertSource.SQL);
        event.setServiceName(serviceName);
        event.setEnvironment(environment);
        event.setHost(host);
        event.setSummary("Datasource " + dataSourceName + " active=" + activeCount + " wait=" + waitThreadCount);
        event.setDetail("activeCount=" + activeCount + ", waitThreadCount=" + waitThreadCount
                + ", activeThreshold=" + properties.getActiveCountThreshold()
                + ", waitThreshold=" + properties.getWaitThreadCountThreshold());
        return event;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        if (value.length() <= maxLength) return value;
        return value.substring(0, maxLength) + "...";
    }
}
