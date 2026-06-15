package com.pcm.alert.starter;

import com.pcm.alert.core.AlertDeduplicator;
import com.pcm.alert.core.AlertManager;
import com.pcm.alert.core.AlertMessageRenderer;
import com.pcm.alert.core.AlertPublisher;
import com.pcm.alert.core.AlertRuleEvaluator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * 自动装配集成测试。
 * <p>
 * 验证 enabled=true/false 时 bean 的创建与缺失。
 * </p>
 */
public class PcmAlertAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PcmAlertAutoConfiguration.class));

    @Test
    public void shouldCreateAllBeansWhenEnabled() {
        contextRunner
                .withPropertyValues("pcm.alert.enabled=true")
                .run(context -> {
                    Assert.assertTrue(context.containsBean("springAlertEventFactory"));
                    Assert.assertTrue(context.containsBean("alertRuleEvaluator"));
                    Assert.assertTrue(context.containsBean("alertDeduplicator"));
                    Assert.assertTrue(context.containsBean("alertMessageRenderer"));
                    Assert.assertTrue(context.containsBean("alertManager"));
                    Assert.assertTrue(context.containsBean("metricAlertCollector"));

                    AlertManager alertManager = context.getBean(AlertManager.class);
                    Assert.assertNotNull(alertManager);

                    AlertRuleEvaluator evaluator = context.getBean(AlertRuleEvaluator.class);
                    Assert.assertNotNull(evaluator);

                    AlertDeduplicator deduplicator = context.getBean(AlertDeduplicator.class);
                    Assert.assertNotNull(deduplicator);

                    AlertMessageRenderer renderer = context.getBean(AlertMessageRenderer.class);
                    Assert.assertNotNull(renderer);

                    AlertPublisher publisher = context.getBean(AlertPublisher.class);
                    Assert.assertNotNull(publisher);
                });
    }

    @Test
    public void shouldNotCreateBeansWhenDisabled() {
        contextRunner
                .withPropertyValues("pcm.alert.enabled=false")
                .run(context -> {
                    Assert.assertFalse(context.containsBean("alertManager"));
                    Assert.assertFalse(context.containsBean("alertRuleEvaluator"));
                    Assert.assertFalse(context.containsBean("alertDeduplicator"));
                });
    }

    @Test
    public void shouldNotCreateBeansByDefault() {
        contextRunner
                .run(context -> {
                    Assert.assertFalse(context.containsBean("alertManager"));
                });
    }

    @Test
    public void shouldCreateAsyncPublisherByDefault() {
        contextRunner
                .withPropertyValues("pcm.alert.enabled=true", "pcm.alert.webhook=http://127.0.0.1:8089/mock/webhook")
                .run(context -> {
                    AlertPublisher publisher = context.getBean(AlertPublisher.class);
                    Assert.assertNotNull(publisher);
                    // AsyncAlertPublisher 的类名包含 "Async"
                    Assert.assertTrue(publisher.getClass().getName().contains("Async"));
                });
    }

    @Test
    public void shouldCreateSyncPublisherWhenAsyncDisabled() {
        contextRunner
                .withPropertyValues(
                        "pcm.alert.enabled=true",
                        "pcm.alert.webhook=http://127.0.0.1:8089/mock/webhook",
                        "pcm.alert.publisher.async=false"
                )
                .run(context -> {
                    AlertPublisher publisher = context.getBean(AlertPublisher.class);
                    Assert.assertNotNull(publisher);
                    Assert.assertTrue(publisher.getClass().getName().contains("Webhook"));
                });
    }

    @Test
    public void shouldCreateNoopPublisherWhenWebhookEmpty() {
        contextRunner
                .withPropertyValues("pcm.alert.enabled=true")
                .run(context -> {
                    AlertPublisher publisher = context.getBean("delegateAlertPublisher", AlertPublisher.class);
                    Assert.assertTrue(publisher.getClass().getName().contains("Noop"));
                });
    }

    @Test
    public void shouldRespectCustomProperties() {
        contextRunner
                .withPropertyValues(
                        "pcm.alert.enabled=true",
                        "pcm.alert.service-name=custom-service",
                        "pcm.alert.environment=prod",
                        "pcm.alert.dedupe.cooldown-seconds=600",
                        "pcm.alert.request.slow-threshold-ms=2000",
                        "pcm.alert.metric.jvm-memory-threshold=0.9",
                        "pcm.alert.metric.thread-threshold=200"
                )
                .run(context -> {
                    AlertProperties properties = context.getBean(AlertProperties.class);
                    Assert.assertEquals("custom-service", properties.getServiceName());
                    Assert.assertEquals("prod", properties.getEnvironment());
                    Assert.assertEquals(600L, properties.getDedupe().getCooldownSeconds());
                    Assert.assertEquals(2000L, properties.getRequest().getSlowThresholdMs());
                    Assert.assertEquals(0.9, properties.getMetric().getJvmMemoryThreshold(), 0.001);
                    Assert.assertEquals(200, properties.getMetric().getThreadThreshold());
                });
    }

    @Test
    public void shouldRespectExceptionExcludeList() {
        contextRunner
                .withPropertyValues(
                        "pcm.alert.enabled=true",
                        "pcm.alert.exception.exclude-exceptions[0]=com.example.MyException"
                )
                .run(context -> {
                    AlertProperties properties = context.getBean(AlertProperties.class);
                    Assert.assertEquals(1, properties.getException().getExcludeExceptions().length);
                    Assert.assertEquals("com.example.MyException", properties.getException().getExcludeExceptions()[0]);
                });
    }

    @Test
    public void shouldRespectStatusCodeAlertConfig() {
        contextRunner
                .withPropertyValues(
                        "pcm.alert.enabled=true",
                        "pcm.alert.request.status-code-alert-enabled=false",
                        "pcm.alert.request.status-code-alert-thresholds[0]=500",
                        "pcm.alert.request.status-code-alert-thresholds[1]=502"
                )
                .run(context -> {
                    AlertProperties properties = context.getBean(AlertProperties.class);
                    Assert.assertFalse(properties.getRequest().isStatusCodeAlertEnabled());
                    Assert.assertArrayEquals(new int[]{500, 502}, properties.getRequest().getStatusCodeAlertThresholds());
                });
    }
}
