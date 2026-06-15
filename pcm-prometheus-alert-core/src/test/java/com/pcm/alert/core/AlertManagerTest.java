package com.pcm.alert.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * AlertManager 测试 —— 验证级别路由和自定义消息推送。
 */
public class AlertManagerTest {

    private AlertManager alertManager;
    private List<AlertMessage> publishedMessages;

    @Before
    public void setUp() {
        AlertRuleEvaluator ruleEvaluator = new DefaultAlertRuleEvaluator();
        AlertDeduplicator deduplicator = new SimpleAlertDeduplicator(false, 0);

        AlertMessageRenderer renderer = event -> {
            AlertMessage msg = new AlertMessage();
            msg.setLevel(event.getLevel());
            msg.setType(event.getType());
            msg.setWebhook("http://default-webhook");
            msg.setTitle("[" + event.getLevel() + "] " + event.getType());
            return msg;
        };

        publishedMessages = new ArrayList<>();
        AlertPublisher publisher = publishedMessages::add;

        alertManager = new AlertManager(ruleEvaluator, deduplicator, renderer, publisher);
    }

    @Test
    public void shouldRouteErrorToErrorWebhook() {
        alertManager.configureLevelRouting(true,
                null, "http://error-webhook", null, null);

        AlertEvent event = new AlertEvent();
        event.setType(AlertType.EXCEPTION);
        event.setLevel(AlertLevel.ERROR);
        event.setSummary("test error");

        alertManager.onEvent(event);

        Assert.assertEquals(1, publishedMessages.size());
        Assert.assertEquals("http://error-webhook", publishedMessages.get(0).getWebhook());
    }

    @Test
    public void shouldRouteWarnToWarnWebhook() {
        alertManager.configureLevelRouting(true,
                null, null, "http://warn-webhook", null);

        AlertEvent event = new AlertEvent();
        event.setType(AlertType.SLOW_REQUEST);
        event.setLevel(AlertLevel.WARN);
        event.setSummary("test warn");

        alertManager.onEvent(event);

        Assert.assertEquals(1, publishedMessages.size());
        Assert.assertEquals("http://warn-webhook", publishedMessages.get(0).getWebhook());
    }

    @Test
    public void shouldNotRouteWhenDisabled() {
        alertManager.configureLevelRouting(false,
                null, "http://error-webhook", null, null);

        AlertEvent event = new AlertEvent();
        event.setType(AlertType.EXCEPTION);
        event.setLevel(AlertLevel.ERROR);
        event.setSummary("test error");

        alertManager.onEvent(event);

        Assert.assertEquals(1, publishedMessages.size());
        Assert.assertEquals("http://default-webhook", publishedMessages.get(0).getWebhook());
    }

    @Test
    public void shouldNotRouteWhenWebhookEmpty() {
        alertManager.configureLevelRouting(true,
                null, "", null, null);

        AlertEvent event = new AlertEvent();
        event.setType(AlertType.EXCEPTION);
        event.setLevel(AlertLevel.ERROR);
        event.setSummary("test error");

        alertManager.onEvent(event);

        Assert.assertEquals(1, publishedMessages.size());
        Assert.assertEquals("http://default-webhook", publishedMessages.get(0).getWebhook());
    }

    @Test
    public void shouldPublishCustomMessage() {
        AlertMessage customMsg = new AlertMessage();
        customMsg.setTitle("Custom alert");
        customMsg.setContent("Custom content");
        customMsg.setLevel(AlertLevel.WARN);
        customMsg.setWebhook("http://custom-webhook");

        alertManager.publish(customMsg);

        Assert.assertEquals(1, publishedMessages.size());
        Assert.assertEquals("Custom alert", publishedMessages.get(0).getTitle());
    }

    @Test
    public void shouldRouteCustomMessageByLevel() {
        alertManager.configureLevelRouting(true,
                null, "http://error-webhook", null, null);

        AlertMessage customMsg = new AlertMessage();
        customMsg.setTitle("Custom alert");
        customMsg.setContent("Custom content");
        customMsg.setLevel(AlertLevel.ERROR);
        customMsg.setWebhook("http://custom-webhook");

        alertManager.publish(customMsg);

        Assert.assertEquals(1, publishedMessages.size());
        Assert.assertEquals("http://error-webhook", publishedMessages.get(0).getWebhook());
    }

    @Test
    public void shouldIgnoreNullEvent() {
        alertManager.onEvent(null);
        Assert.assertEquals(0, publishedMessages.size());
    }

    @Test
    public void shouldIgnoreNullMessage() {
        alertManager.publish(null);
        Assert.assertEquals(0, publishedMessages.size());
    }
}
