package com.pcm.alert.sql;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.pcm.alert.core.AlertManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SQL 告警自动装配类。
 * <p>
 * 条件：pcm.alert.sql.enabled=true 且 Druid 在 classpath 上。
 * 依赖 pcm-prometheus-alert-core 的 AlertManager。
 * </p>
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(SqlAlertProperties.class)
@ConditionalOnProperty(prefix = "pcm.alert.sql", name = "enabled", havingValue = "true")
@ConditionalOnClass(DruidDataSourceAutoConfigure.class)
@AutoConfigureAfter(DruidDataSourceAutoConfigure.class)
public class SqlAlertAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SqlAlertAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public DruidEventAdapter druidEventAdapter(SqlAlertProperties properties, Environment environment) {
        String serviceName = environment.getProperty("spring.application.name", "unknown");
        String env = resolveEnvironment(environment);
        String host = resolveHostName();
        return new DruidEventAdapter(properties, serviceName, env, host);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AlertManager.class)
    public SqlAlertCollector sqlAlertCollector(SqlAlertProperties properties,
                                               DruidEventAdapter eventAdapter,
                                               AlertManager alertManager,
                                               ObjectProvider<DataSource> dataSources) {
        Map<String, DataSource> dsMap = dataSources.stream()
                .collect(Collectors.toMap(
                        ds -> ds.getClass().getSimpleName(),
                        ds -> ds,
                        (a, b) -> a));
        log.info("PCM Alert: SQL alert enabled, found {} datasource(s)", dsMap.size());
        return new SqlAlertCollector(properties, eventAdapter, alertManager, dsMap);
    }

    private String resolveEnvironment(Environment environment) {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length == 0 ? "default" : String.join(",", profiles);
    }

    private String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
