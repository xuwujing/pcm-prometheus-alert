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

@Configuration
@EnableScheduling
@EnableConfigurationProperties(AlertProperties.class)
@ConditionalOnProperty(prefix = "pcm.alert", name = "enabled", havingValue = "true")
public class PcmAlertAutoConfiguration {
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
        return new DefaultAlertMessageRenderer(properties.getWebhook());
    }

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
                                     AlertPublisher publisher) {
        return new AlertManager(ruleEvaluator, deduplicator, messageRenderer, publisher);
    }

    @Bean
    @ConditionalOnClass(DispatcherServlet.class)
    @ConditionalOnMissingBean
    public AlertExceptionResolver alertExceptionResolver(AlertProperties properties,
                                                        SpringAlertEventFactory eventFactory,
                                                        AlertManager alertManager) {
        return new AlertExceptionResolver(properties, eventFactory, alertManager);
    }

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
}
