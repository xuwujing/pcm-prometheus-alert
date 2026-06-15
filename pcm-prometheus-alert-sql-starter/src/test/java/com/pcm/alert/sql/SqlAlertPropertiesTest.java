package com.pcm.alert.sql;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class SqlAlertPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @EnableConfigurationProperties(SqlAlertProperties.class)
    static class TestConfig {
    }

    @Test
    public void shouldBindProperties() {
        contextRunner
                .withPropertyValues(
                        "pcm.alert.sql.enabled=true",
                        "pcm.alert.sql.slow-sql-threshold-ms=2000",
                        "pcm.alert.sql.datasource-alert-enabled=false",
                        "pcm.alert.sql.active-count-threshold=100",
                        "pcm.alert.sql.wait-thread-count-threshold=20"
                )
                .run(context -> {
                    SqlAlertProperties properties = context.getBean(SqlAlertProperties.class);
                    Assert.assertTrue(properties.isEnabled());
                    Assert.assertEquals(2000L, properties.getSlowSqlThresholdMs());
                    Assert.assertFalse(properties.isDatasourceAlertEnabled());
                    Assert.assertEquals(100, properties.getActiveCountThreshold());
                    Assert.assertEquals(20, properties.getWaitThreadCountThreshold());
                });
    }

    @Test
    public void shouldUseDefaults() {
        contextRunner
                .run(context -> {
                    SqlAlertProperties properties = context.getBean(SqlAlertProperties.class);
                    Assert.assertFalse(properties.isEnabled());
                    Assert.assertEquals(1000L, properties.getSlowSqlThresholdMs());
                    Assert.assertTrue(properties.isDatasourceAlertEnabled());
                    Assert.assertEquals(50, properties.getActiveCountThreshold());
                    Assert.assertEquals(10, properties.getWaitThreadCountThreshold());
                });
    }
}
