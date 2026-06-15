package com.pcm.alert.extensions;

import com.pcm.alert.core.AlertManager;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 扩展集成自动装配类。
 * <p>
 * 条件：pcm.alert.enabled=true 且对应扩展开关打开。
 * </p>
 */
@Configuration
@ConditionalOnBean(AlertManager.class)
public class ExtensionsAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ExtensionsAutoConfiguration.class);

    /**
     * Prometheus 指标暴露。
     * 条件：pcm.alert.extensions.prometheus-enabled=true 且 MeterRegistry 在 classpath 上。
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnProperty(prefix = "pcm.alert.extensions", name = "prometheus-enabled", havingValue = "true")
    public PrometheusAlertMetrics prometheusAlertMetrics(MeterRegistry meterRegistry, Environment environment) {
        String serviceName = environment.getProperty("spring.application.name", "unknown");
        return new PrometheusAlertMetrics(meterRegistry, serviceName);
    }

    /**
     * SkyWalking 链路追踪集成。
     * 条件：pcm.alert.extensions.skywalking-enabled=true。
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "pcm.alert.extensions", name = "skywalking-enabled", havingValue = "true")
    public SkyWalkingTraceExtractor skyWalkingTraceExtractor() {
        return new SkyWalkingTraceExtractor();
    }

    /**
     * ELK 日志集成。
     * 条件：pcm.alert.extensions.elk-enabled=true。
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "pcm.alert.extensions", name = "elk-enabled", havingValue = "true")
    public ElkAlertLogger elkAlertLogger() {
        return new ElkAlertLogger();
    }
}
