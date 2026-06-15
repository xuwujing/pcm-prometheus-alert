package com.pcm.alert.starter;

import com.pcm.alert.core.AlertEvent;
import com.pcm.alert.core.AlertType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpringAlertEventFactoryTest {

    private SpringAlertEventFactory factory;
    private AlertProperties properties;

    @Before
    public void setUp() {
        properties = new AlertProperties();
        properties.setServiceName("test-service");
        properties.setEnvironment("unit");
        properties.getRequest().setSlowThresholdMs(1000);
        properties.getMetric().setThreadThreshold(500);
        properties.getMetric().setCpuThreshold(0.8);
        properties.getException().setStackTraceMaxLines(20);
        MockEnvironment env = new MockEnvironment();
        factory = new SpringAlertEventFactory(properties, env);
    }

    @Test
    public void shouldCreateExceptionEvent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/demo/error");
        when(request.getHeader("X-B3-TraceId")).thenReturn("trace-123");

        AlertEvent event = factory.fromException(new IllegalStateException("test"), request);

        Assert.assertEquals(AlertType.EXCEPTION, event.getType());
        Assert.assertEquals("test-service", event.getServiceName());
        Assert.assertEquals("unit", event.getEnvironment());
        Assert.assertEquals("GET", event.getRequestMethod());
        Assert.assertEquals("/demo/error", event.getRequestPath());
        Assert.assertEquals("trace-123", event.getTraceId());
        Assert.assertTrue(event.getSummary().contains("IllegalStateException"));
        Assert.assertNotNull(event.getStackTrace());
    }

    @Test
    public void shouldTruncateStackTrace() {
        properties.getException().setStackTraceMaxLines(5);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/demo/error");

        AlertEvent event = factory.fromException(new RuntimeException("deep"), request);

        Assert.assertNotNull(event.getStackTrace());
        String[] lines = event.getStackTrace().split("\\r?\\n");
        Assert.assertTrue("StackTrace should be truncated to <= 5 + truncation line",
                lines.length <= 6);
        Assert.assertTrue(event.getStackTrace().contains("more lines"));
    }

    @Test
    public void shouldExcludeConfiguredExceptions() {
        // MissingServletRequestParameterException is in default exclude list
        Assert.assertTrue(factory.isExcluded(
                new org.springframework.web.bind.MissingServletRequestParameterException("id", "String")));

        // IllegalStateException is not in exclude list
        Assert.assertFalse(factory.isExcluded(new IllegalStateException("test")));
    }

    @Test
    public void shouldNotExcludeWhenListEmpty() {
        properties.getException().setExcludeExceptions(new String[0]);
        Assert.assertFalse(factory.isExcluded(
                new org.springframework.web.bind.MissingServletRequestParameterException("id", "String")));
    }

    @Test
    public void shouldCreateSlowRequestEvent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/slow");

        AlertEvent event = factory.fromSlowRequest(request, 2500L);

        Assert.assertEquals(AlertType.SLOW_REQUEST, event.getType());
        Assert.assertEquals(2500L, event.getCostMs());
        Assert.assertEquals("POST", event.getRequestMethod());
        Assert.assertEquals("/api/slow", event.getRequestPath());
        Assert.assertTrue(event.getSummary().contains("2500"));
    }

    @Test
    public void shouldCreateHttpStatusEvent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/error");

        AlertEvent event = factory.fromHttpStatus(request, 500, 120L);

        Assert.assertEquals(AlertType.HTTP_STATUS, event.getType());
        Assert.assertEquals(500, event.getStatusCode());
        Assert.assertEquals(120L, event.getCostMs());
        Assert.assertTrue(event.getSummary().contains("500"));
    }

    @Test
    public void shouldCreateJvmMemoryEvent() {
        AlertEvent event = factory.fromJvmMemory(0.85, 850_000_000L, 1_000_000_000L);

        Assert.assertEquals(AlertType.JVM_MEMORY, event.getType());
        Assert.assertTrue(event.getSummary().contains("85"));
    }

    @Test
    public void shouldCreateThreadCountEvent() {
        AlertEvent event = factory.fromThreadCount(600);

        Assert.assertEquals(AlertType.THREAD_COUNT, event.getType());
        Assert.assertTrue(event.getSummary().contains("600"));
    }

    @Test
    public void shouldCreateCpuUsageEvent() {
        AlertEvent event = factory.fromCpuUsage(0.92);

        Assert.assertEquals(AlertType.CPU_USAGE, event.getType());
        Assert.assertTrue(event.getSummary().contains("92"));
    }

    @Test
    public void shouldCreateRecoveryEvent() {
        AlertEvent event = factory.recovery(AlertType.JVM_MEMORY, "JVM memory");

        Assert.assertEquals(AlertType.JVM_MEMORY, event.getType());
        Assert.assertEquals(com.pcm.alert.core.AlertLevel.INFO, event.getLevel());
        Assert.assertTrue(event.getSummary().contains("recovered"));
    }

    @Test
    public void shouldHandleNullRequest() {
        AlertEvent event = factory.fromException(new RuntimeException("boom"), null);

        Assert.assertEquals(AlertType.EXCEPTION, event.getType());
        Assert.assertNull(event.getRequestPath());
    }
}
