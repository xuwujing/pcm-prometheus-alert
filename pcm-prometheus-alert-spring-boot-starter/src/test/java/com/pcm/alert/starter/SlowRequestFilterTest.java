package com.pcm.alert.starter;

import com.pcm.alert.core.AlertDeduplicator;
import com.pcm.alert.core.AlertManager;
import com.pcm.alert.core.AlertMessageRenderer;
import com.pcm.alert.core.AlertPublisher;
import com.pcm.alert.core.AlertRuleEvaluator;
import com.pcm.alert.core.DefaultAlertRuleEvaluator;
import com.pcm.alert.core.SimpleAlertDeduplicator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.List;

public class SlowRequestFilterTest {

    private SlowRequestFilter filter;
    private AlertProperties properties;
    private List<String> publishedTitles;

    @Before
    public void setUp() {
        properties = new AlertProperties();
        properties.setServiceName("test");
        properties.setEnvironment("unit");
        properties.getRequest().setEnabled(true);
        properties.getRequest().setSlowThresholdMs(1000);
        properties.getRequest().setStatusCodeAlertEnabled(true);
        properties.getRequest().setStatusCodeAlertThresholds(new int[]{500, 502, 503, 504});
        properties.getRequest().setExcludePaths(new String[]{"/actuator/**", "/health"});
        properties.getDedupe().setEnabled(false);

        MockEnvironment env = new MockEnvironment();
        SpringAlertEventFactory eventFactory = new SpringAlertEventFactory(properties, env);
        AlertRuleEvaluator ruleEvaluator = new DefaultAlertRuleEvaluator();
        AlertDeduplicator deduplicator = new SimpleAlertDeduplicator(false, 0);

        publishedTitles = new ArrayList<>();
        AlertMessageRenderer renderer = event -> {
            com.pcm.alert.core.AlertMessage msg = new com.pcm.alert.core.AlertMessage();
            msg.setTitle("[" + event.getLevel() + "] " + event.getType());
            return msg;
        };
        AlertPublisher publisher = message -> publishedTitles.add(message.getTitle());

        AlertManager alertManager = new AlertManager(ruleEvaluator, deduplicator, renderer, publisher);
        filter = new SlowRequestFilter(properties, eventFactory, alertManager);
    }

    @Test
    public void shouldAlertOnSlowRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Simulate slow request by sleeping in chain
        chain = new MockFilterChain() {
            @Override
            public void doFilter(javax.servlet.ServletRequest req, javax.servlet.ServletResponse res) {
                try {
                    Thread.sleep(1100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        filter.doFilter(request, response, chain);

        Assert.assertFalse("Should have at least one alert", publishedTitles.isEmpty());
        Assert.assertTrue(publishedTitles.get(0).contains("SLOW_REQUEST"));
    }

    @Test
    public void shouldNotAlertOnFastRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/fast");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Assert.assertTrue("Should not alert on fast request", publishedTitles.isEmpty());
    }

    @Test
    public void shouldExcludeActuatorPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(javax.servlet.ServletRequest req, javax.servlet.ServletResponse res) {
                try {
                    Thread.sleep(1100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        filter.doFilter(request, response, chain);

        Assert.assertTrue("Actuator paths should be excluded", publishedTitles.isEmpty());
    }

    @Test
    public void shouldExcludeHealthPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(javax.servlet.ServletRequest req, javax.servlet.ServletResponse res) {
                try {
                    Thread.sleep(1100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        filter.doFilter(request, response, chain);

        Assert.assertTrue("Health path should be excluded", publishedTitles.isEmpty());
    }

    @Test
    public void shouldAlertOnStatusCode500() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/error");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Assert.assertFalse("Should alert on 500", publishedTitles.isEmpty());
        Assert.assertTrue(publishedTitles.get(0).contains("HTTP_STATUS"));
    }

    @Test
    public void shouldNotAlertOnStatusCode200() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ok");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Assert.assertTrue("Should not alert on 200", publishedTitles.isEmpty());
    }

    @Test
    public void shouldNotAlertWhenRequestDisabled() throws Exception {
        properties.getRequest().setEnabled(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Assert.assertTrue("Should not alert when disabled", publishedTitles.isEmpty());
    }
}
