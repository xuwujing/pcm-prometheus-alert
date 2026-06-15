package com.pcm.alert.core;

import org.junit.Assert;
import org.junit.Test;

public class SimpleAlertDeduplicatorTest {
    @Test
    public void shouldSuppressSameEventWithinCooldown() {
        SimpleAlertDeduplicator deduplicator = new SimpleAlertDeduplicator(true, 60);
        AlertEvent event = new AlertEvent();
        event.setType(AlertType.EXCEPTION);
        event.setServiceName("demo");
        event.setRequestPath("/demo/error");
        event.setSummary("IllegalStateException");

        Assert.assertTrue(deduplicator.allow(event));
        deduplicator.record(event);
        Assert.assertFalse(deduplicator.allow(event));
    }

    @Test
    public void shouldAllowWhenDeduplicationDisabled() {
        SimpleAlertDeduplicator deduplicator = new SimpleAlertDeduplicator(false, 60);
        AlertEvent event = new AlertEvent();
        event.setType(AlertType.EXCEPTION);

        Assert.assertTrue(deduplicator.allow(event));
        deduplicator.record(event);
        Assert.assertTrue(deduplicator.allow(event));
    }
}
