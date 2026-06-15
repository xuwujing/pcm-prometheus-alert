package com.pcm.alert.starter;

import com.pcm.alert.core.AlertDeduplicator;
import com.pcm.alert.core.AlertManager;
import com.pcm.alert.core.AlertMessageRenderer;
import com.pcm.alert.core.AlertPublisher;
import com.pcm.alert.core.AlertRuleEvaluator;
import com.pcm.alert.core.AsyncAlertPublisher;
import com.pcm.alert.core.DefaultAlertMessageRenderer;
import com.pcm.alert.core.DefaultAlertRuleEvaluator;
import com.pcm.alert.core.NoopAlertPublisher;
import com.pcm.alert.core.SimpleAlertDeduplicator;
import com.pcm.alert.core.WebhookAlertPublisher;
import com.pcm.alert.core.WebhookFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * PCM Alert 自动装配类。
 * <p>
 * 条件：pcm.alert.enabled=true 时生效。
 * 装配顺序：事件工厂 → 规则评估器 → 去重器 → 消息渲染器 → 推送器 → 告警管理器 → 采集器。
 * </p>
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(AlertProperties.class)
@ConditionalOnProperty(prefix = "pcm.alert", name = "enabled", havingValue = "true")
public class PcmAlertAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(PcmAlertAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public SpringAlertEventFactory springAlertEventFactory(AlertProperties properties, Environment environment) {
        return new SpringAlertEventFactory(properties, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public AlertRuleEvaluator alertRuleEvaluator() {
        return new DefaultAlertRuleEvaluator();
    }

    @Bean
    @ConditionalOnMissingBean
    public AlertDeduplicator alertDeduplicator(AlertProperties properties) {
        return new SimpleAlertDeduplicator(
                properties.getDedupe().isEnabled(),
                properties.getDedupe().getCooldownSeconds());
    }

    @Bean
    @ConditionalOnMissingBean
    public AlertMessageRenderer alertMessageRenderer(AlertProperties properties) {
        WebhookFormat format = parseWebhookFormat(properties.getWebhookFormat());
        return new DefaultAlertMessageRenderer(properties.getWebhook(), format);
    }

    /**
     * 异步推送器（默认启用）。
     * 当 pcm.alert.publisher.async=false 时使用同步推送器。
     */
    @Bean(destroyMethod = "shutdown")
    @Primary
    @ConditionalOnProperty(prefix = "pcm.alert.publisher", name = "async", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AlertPublisher alertPublisher(AlertProperties properties,
                                         @Qualifier("delegateAlertPublisher") AlertPublisher delegateAlertPublisher) {
        return new AsyncAlertPublisher(delegateAlertPublisher, properties.getPublisher().getQueueSize());
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "pcm.alert.publisher", name = "async", havingValue = "false")
    @ConditionalOnMissingBean
    public AlertPublisher syncAlertPublisher(@Qualifier("delegateAlertPublisher") AlertPublisher delegateAlertPublisher) {
        return delegateAlertPublisher;
    }

    /**
     * 底层推送器：有 webhook 配置时用 WebhookAlertPublisher，否则用 NoopAlertPublisher。
     */
    @Bean
    @ConditionalOnMissingBean(name = "delegateAlertPublisher")
    public AlertPublisher delegateAlertPublisher(AlertProperties properties) {
        return StringUtils.hasText(properties.getWebhook())
                ? new WebhookAlertPublisher(properties.getPublisher().getTimeoutMs())
                : new NoopAlertPublisher();
    }

    @Bean
    @ConditionalOnMissingBean
    public AlertManager alertManager(AlertRuleEvaluator ruleEvaluator,
                                     AlertDeduplicator deduplicator,
                                     AlertMessageRenderer messageRenderer,
                                     AlertPublisher publisher,
                                     AlertProperties properties) {
        AlertManager alertManager = new AlertManager(ruleEvaluator, deduplicator, messageRenderer, publisher);

        // 配置按级别路由
        AlertProperties.Custom custom = properties.getCustom();
        alertManager.configureLevelRouting(
                custom.isLevelRoutingEnabled(),
                custom.getFatalWebhook(),
                custom.getErrorWebhook(),
                custom.getWarnWebhook(),
                custom.getInfoWebhook()
        );

        // 扩展集成日志
        AlertProperties.Extensions extensions = properties.getExtensions();
        if (extensions.isPrometheusEnabled()) {
            log.info("PCM Alert: Prometheus integration enabled (metrics exposed via /actuator/prometheus)");
        }
        if (extensions.isSkywalkingEnabled()) {
            log.info("PCM Alert: SkyWalking integration enabled (traceId from SkyWalking agent)");
        }
        if (extensions.isElkEnabled()) {
            log.info("PCM Alert: ELK integration enabled (alert events logged for logstash collection)");
        }

        return alertManager;
    }

    /**
     * 异常告警处理器 —— 仅在 DispatcherServlet 存在时注册。
     */
    @Bean
    @ConditionalOnClass(DispatcherServlet.class)
    @ConditionalOnMissingBean
    public AlertExceptionResolver alertExceptionResolver(AlertProperties properties,
                                                        SpringAlertEventFactory eventFactory,
                                                        AlertManager alertManager) {
        return new AlertExceptionResolver(properties, eventFactory, alertManager);
    }

    /**
     * 慢请求过滤器 —— 仅在 DispatcherServlet 存在时注册。
     */
    @Bean
    @ConditionalOnClass(DispatcherServlet.class)
    @ConditionalOnMissingBean(name = "pcmAlertSlowRequestFilter")
    public FilterRegistrationBean<SlowRequestFilter> pcmAlertSlowRequestFilter(AlertProperties properties,
                                                                               SpringAlertEventFactory eventFactory,
                                                                               AlertManager alertManager) {
        FilterRegistrationBean<SlowRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setName("pcmAlertSlowRequestFilter");
        registrationBean.setFilter(new SlowRequestFilter(properties, eventFactory, alertManager));
        registrationBean.setOrder(Integer.MAX_VALUE - 10);
        return registrationBean;
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricAlertCollector metricAlertCollector(AlertProperties properties,
                                                    SpringAlertEventFactory eventFactory,
                                                    AlertManager alertManager) {
        return new MetricAlertCollector(properties, eventFactory, alertManager);
    }

    /**
     * 仪表盘控制器 —— 类似 Druid 监控页面。
     * 条件：pcm.alert.dashboard.enabled=true（默认 true）。
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "pcm.alert.dashboard", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DashboardController dashboardController() {
        return new DashboardController();
    }

    /**
     * 解析 webhook 格式字符串。
     */
    private WebhookFormat parseWebhookFormat(String format) {
        if (format == null) {
            return WebhookFormat.DEFAULT;
        }
        switch (format.toLowerCase()) {
            case "dingtalk": return WebhookFormat.DINGTALK;
            case "feishu":   return WebhookFormat.FEISHU;
            case "wecom":    return WebhookFormat.WECOM;
            default:         return WebhookFormat.DEFAULT;
        }
    }
}
