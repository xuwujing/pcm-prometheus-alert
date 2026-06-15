package com.pcm.alert.sql;

import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.pcm.alert.core.AlertManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.util.Map;

/**
 * SQL 告警采集器。
 * <p>
 * 通过 Druid FilterEventAdapter 拦截 SQL 执行，检测慢 SQL。
 * 通过 @Scheduled 定时检查数据源连接池状态。
 * </p>
 */
public class SqlAlertCollector {
    private static final Logger log = LoggerFactory.getLogger(SqlAlertCollector.class);

    private final SqlAlertProperties properties;
    private final DruidEventAdapter eventAdapter;
    private final AlertManager alertManager;
    private final Map<String, DataSource> dataSources;

    public SqlAlertCollector(SqlAlertProperties properties,
                             DruidEventAdapter eventAdapter,
                             AlertManager alertManager,
                             Map<String, DataSource> dataSources) {
        this.properties = properties;
        this.eventAdapter = eventAdapter;
        this.alertManager = alertManager;
        this.dataSources = dataSources;
    }

    /**
     * 创建 Druid FilterEventAdapter，用于拦截 SQL 执行。
     */
    public FilterEventAdapter createFilterEventAdapter() {
        return new FilterEventAdapter() {
            @Override
            protected void statementExecuteAfter(StatementProxy statement, String sql, boolean firstResult) {
                long costMs = statement.getLastExecuteTimeNano() / 1_000_000;
                if (costMs >= properties.getSlowSqlThresholdMs()) {
                    String dsName = statement.getConnectionProxy().getDirectDataSource().getName();
                    alertManager.onEvent(eventAdapter.fromSlowSql(sql, costMs, dsName));
                }
            }
        };
    }

    /**
     * 定时检查数据源连接池状态。
     */
    @Scheduled(fixedDelay = 30_000)
    public void checkDatasource() {
        if (!properties.isDatasourceAlertEnabled()) {
            return;
        }
        if (dataSources == null || dataSources.isEmpty()) {
            return;
        }
        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            DataSource ds = entry.getValue();
            if (ds instanceof DruidDataSource) {
                checkDruidDatasource(entry.getKey(), (DruidDataSource) ds);
            }
        }
    }

    private void checkDruidDatasource(String name, DruidDataSource ds) {
        int activeCount = ds.getActiveCount();
        int waitThreadCount = ds.getWaitThreadCount();
        boolean alert = false;
        if (activeCount >= properties.getActiveCountThreshold()) {
            alert = true;
        }
        if (waitThreadCount >= properties.getWaitThreadCountThreshold()) {
            alert = true;
        }
        if (alert) {
            alertManager.onEvent(eventAdapter.fromDatasource(name, activeCount, waitThreadCount));
        }
    }
}
