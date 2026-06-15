package com.pcm.alert.core;

import org.junit.Assert;
import org.junit.Test;

public class DefaultAlertMessageRendererTest {
    @Test
    public void shouldRenderEventFieldsIntoMessage() {
        AlertEvent event = new AlertEvent();
        event.setType(AlertType.SLOW_REQUEST);
        event.setLevel(AlertLevel.WARN);
        event.setServiceName("demo-service");
        event.setEnvironment("test");
        event.setHost("localhost");
        event.setTraceId("trace-1");
        event.setRequestPath("/demo/slow");
        event.setRequestMethod("GET");
        event.setCostMs(1200L);
        event.setStatusCode(500);
        event.setSummary("slow request");
        event.setDetail("threshold exceeded");

        DefaultAlertMessageRenderer renderer = new DefaultAlertMessageRenderer("http://127.0.0.1/mock");
        AlertMessage message = renderer.render(event);

        Assert.assertEquals(AlertType.SLOW_REQUEST, message.getType());
        Assert.assertEquals(AlertLevel.WARN, message.getLevel());
        Assert.assertEquals("http://127.0.0.1/mock", message.getWebhook());
        Assert.assertTrue(message.getTitle().contains("demo-service"));
        Assert.assertTrue(message.getContent().contains("path: /demo/slow"));
        Assert.assertTrue(message.getContent().contains("costMs: 1200"));
        Assert.assertTrue(message.getContent().contains("statusCode: 500"));
        Assert.assertEquals("trace-1", message.getAttributes().get("traceId"));
    }
}
