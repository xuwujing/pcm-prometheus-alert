package com.pcm.alert.extensions;

import com.pcm.alert.core.AlertEvent;
import com.pcm.alert.core.AlertLevel;
import com.pcm.alert.core.AlertType;
import org.junit.Assert;
import org.junit.Test;

public class ElkAlertLoggerTest {

    @Test
    public void shouldCreateWithoutError() {
        ElkAlertLogger logger = new ElkAlertLogger();
        Assert.assertNotNull(logger);
    }

    @Test
    public void shouldHandleNullEvent() {
        ElkAlertLogger logger = new ElkAlertLogger();
        logger.logEvent(null);
        // should not throw
    }

    @Test
    public void shouldLogEventWithoutError() {
        ElkAlertLogger logger = new ElkAlertLogger();
        AlertEvent event = new AlertEvent();
        event.setType(AlertType.EXCEPTION);
        event.setLevel(AlertLevel.ERROR);
        event.setServiceName("test-service");
        event.setEnvironment("prod");
        event.setSummary("Test exception");
        event.setTraceId("trace-123");
        event.setCostMs(150L);
        event.setStatusCode(500);

        logger.logEvent(event);
        // should not throw
    }

    @Test
    public void shouldLogMinimalEventWithoutError() {
        ElkAlertLogger logger = new ElkAlertLogger();
        AlertEvent event = new AlertEvent();
        logger.logEvent(event);
        // should not throw
    }
}
