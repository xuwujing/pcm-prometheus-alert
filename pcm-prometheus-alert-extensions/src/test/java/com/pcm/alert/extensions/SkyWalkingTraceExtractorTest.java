package com.pcm.alert.extensions;

import com.pcm.alert.core.AlertEvent;
import org.junit.Assert;
import org.junit.Test;

public class SkyWalkingTraceExtractorTest {

    @Test
    public void shouldCreateWithoutError() {
        SkyWalkingTraceExtractor extractor = new SkyWalkingTraceExtractor();
        Assert.assertNotNull(extractor);
    }

    @Test
    public void shouldReturnNullWhenNotAvailable() {
        SkyWalkingTraceExtractor extractor = new SkyWalkingTraceExtractor();
        // SkyWalking agent is not loaded in test, so should return null
        String traceId = extractor.extractTraceId();
        Assert.assertNull(traceId);
    }

    @Test
    public void shouldNotOverwriteExistingTraceId() {
        SkyWalkingTraceExtractor extractor = new SkyWalkingTraceExtractor();
        AlertEvent event = new AlertEvent();
        event.setTraceId("existing-trace-id");

        extractor.enrich(event);

        Assert.assertEquals("existing-trace-id", event.getTraceId());
    }

    @Test
    public void shouldHandleNullEvent() {
        SkyWalkingTraceExtractor extractor = new SkyWalkingTraceExtractor();
        extractor.enrich(null);
        // should not throw
    }

    @Test
    public void shouldNotSetTraceIdWhenNull() {
        SkyWalkingTraceExtractor extractor = new SkyWalkingTraceExtractor();
        AlertEvent event = new AlertEvent();
        extractor.enrich(event);

        // traceId should remain null since SkyWalking is not available
        Assert.assertNull(event.getTraceId());
    }
}
