package com.pcm.alert.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * Webhook payload 构建器测试。
 */
public class WebhookPayloadBuilderTest {

    private AlertMessage buildMessage() {
        AlertMessage message = new AlertMessage();
        message.setTitle("[ERROR] EXCEPTION - test-service");
        message.setContent("service: test-service\nenv: prod\nsummary: Test exception");
        message.setLevel(AlertLevel.ERROR);
        message.setType(AlertType.EXCEPTION);
        message.setWebhookFormat(WebhookFormat.DEFAULT);
        message.getAttributes().put("serviceName", "test-service");
        message.getAttributes().put("environment", "prod");
        message.getAttributes().put("traceId", "trace-123");
        return message;
    }

    @Test
    public void shouldBuildDefaultPayload() throws Exception {
        DefaultWebhookPayloadBuilder builder = new DefaultWebhookPayloadBuilder();
        String json = builder.build(buildMessage());

        Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("\"title\""));
        Assert.assertTrue(json.contains("\"content\""));
        Assert.assertTrue(json.contains("\"level\""));
        Assert.assertTrue(json.contains("\"type\""));
        Assert.assertTrue(json.contains("\"attributes\""));
    }

    @Test
    public void shouldBuildDingTalkPayload() throws Exception {
        DingTalkWebhookPayloadBuilder builder = new DingTalkWebhookPayloadBuilder();
        String json = builder.build(buildMessage());

        Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("\"msgtype\""));
        Assert.assertTrue(json.contains("\"markdown\""));
        Assert.assertTrue(json.contains("test-service"));
        Assert.assertTrue(json.contains("❌"));
    }

    @Test
    public void shouldBuildFeishuPayload() throws Exception {
        FeishuWebhookPayloadBuilder builder = new FeishuWebhookPayloadBuilder();
        String json = builder.build(buildMessage());

        Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("\"msg_type\""));
        Assert.assertTrue(json.contains("\"card\""));
        Assert.assertTrue(json.contains("\"header\""));
        Assert.assertTrue(json.contains("\"elements\""));
    }

    @Test
    public void shouldBuildWeComPayload() throws Exception {
        WeComWebhookPayloadBuilder builder = new WeComWebhookPayloadBuilder();
        String json = builder.build(buildMessage());

        Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("\"msgtype\""));
        Assert.assertTrue(json.contains("\"markdown\""));
        Assert.assertTrue(json.contains("test-service"));
    }

    @Test
    public void shouldHandleInfoLevel() throws Exception {
        AlertMessage msg = buildMessage();
        msg.setLevel(AlertLevel.INFO);

        DingTalkWebhookPayloadBuilder builder = new DingTalkWebhookPayloadBuilder();
        String json = builder.build(msg);
        Assert.assertTrue(json.contains("ℹ️"));
    }

    @Test
    public void shouldHandleFatalLevel() throws Exception {
        AlertMessage msg = buildMessage();
        msg.setLevel(AlertLevel.FATAL);

        DingTalkWebhookPayloadBuilder builder = new DingTalkWebhookPayloadBuilder();
        String json = builder.build(msg);
        Assert.assertTrue(json.contains("🔥"));
    }

    @Test
    public void shouldHandleNullLevel() throws Exception {
        AlertMessage msg = buildMessage();
        msg.setLevel(null);

        DingTalkWebhookPayloadBuilder builder = new DingTalkWebhookPayloadBuilder();
        String json = builder.build(msg);
        Assert.assertTrue(json.contains("📢"));
    }
}
