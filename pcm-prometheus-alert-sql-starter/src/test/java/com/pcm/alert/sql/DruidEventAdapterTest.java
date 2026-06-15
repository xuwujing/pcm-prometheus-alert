package com.pcm.alert.sql;

import com.pcm.alert.core.AlertEvent;
import com.pcm.alert.core.AlertLevel;
import com.pcm.alert.core.AlertType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DruidEventAdapterTest {

    private DruidEventAdapter adapter;

    @Before
    public void setUp() {
        SqlAlertProperties properties = new SqlAlertProperties();
        properties.setSlowSqlThresholdMs(1000);
        properties.setActiveCountThreshold(50);
        properties.setWaitThreadCountThreshold(10);
        adapter = new DruidEventAdapter(properties, "test-service", "prod", "host1");
    }

    @Test
    public void shouldCreateSlowSqlEvent() {
        AlertEvent event = adapter.fromSlowSql("SELECT * FROM users WHERE id = 1", 2500L, "ds-master");

        Assert.assertEquals(AlertType.SLOW_SQL, event.getType());
        Assert.assertEquals(AlertLevel.WARN, event.getLevel());
        Assert.assertEquals("test-service", event.getServiceName());
        Assert.assertEquals("prod", event.getEnvironment());
        Assert.assertEquals("host1", event.getHost());
        Assert.assertEquals(2500L, event.getCostMs());
        Assert.assertTrue(event.getSummary().contains("2500"));
        Assert.assertTrue(event.getSummary().contains("ds-master"));
        Assert.assertTrue(event.getDetail().contains("SELECT"));
    }

    @Test
    public void shouldTruncateLongSql() {
        StringBuilder longSql = new StringBuilder("SELECT ");
        for (int i = 0; i < 600; i++) {
            longSql.append("a");
        }
        AlertEvent event = adapter.fromSlowSql(longSql.toString(), 1500L, "ds-master");

        // SQL 截断至 500 字符 + "..."，detail 还包含前缀 "SQL: " 和后缀 "\nthreshold=..."
        Assert.assertTrue(event.getDetail().contains("..."));
        // 验证 SQL 确实被截断了（detail 中不应包含完整的 600+ 字符 SQL）
        Assert.assertFalse(event.getDetail().contains(longSql.toString()));
    }

    @Test
    public void shouldHandleNullSql() {
        AlertEvent event = adapter.fromSlowSql(null, 1500L, "ds-master");

        Assert.assertNotNull(event);
        Assert.assertTrue(event.getDetail().contains("SQL: "));
    }

    @Test
    public void shouldCreateDatasourceEvent() {
        AlertEvent event = adapter.fromDatasource("ds-master", 60, 15);

        Assert.assertEquals(AlertType.DATASOURCE, event.getType());
        Assert.assertEquals(AlertLevel.WARN, event.getLevel());
        Assert.assertTrue(event.getSummary().contains("ds-master"));
        Assert.assertTrue(event.getSummary().contains("active=60"));
        Assert.assertTrue(event.getSummary().contains("wait=15"));
        Assert.assertTrue(event.getDetail().contains("activeCount=60"));
        Assert.assertTrue(event.getDetail().contains("waitThreadCount=15"));
    }
}
