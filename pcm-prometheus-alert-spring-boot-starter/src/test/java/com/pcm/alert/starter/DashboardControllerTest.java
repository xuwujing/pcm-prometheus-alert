package com.pcm.alert.starter;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class DashboardControllerTest {

    @Test
    public void shouldReturnDashboardData() {
        DashboardController controller = new DashboardController();
        Map<String, Object> result = controller.dashboard();

        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsKey("jvm"));
        Assert.assertTrue(result.containsKey("system"));
        Assert.assertTrue(result.containsKey("alertStatus"));

        @SuppressWarnings("unchecked")
        Map<String, Object> jvm = (Map<String, Object>) result.get("jvm");
        Assert.assertTrue(jvm.containsKey("heapUsed"));
        Assert.assertTrue(jvm.containsKey("heapMax"));
        Assert.assertTrue(jvm.containsKey("heapUsagePercent"));
        Assert.assertTrue(jvm.containsKey("threadCount"));
        Assert.assertTrue(jvm.containsKey("peakThreadCount"));
        Assert.assertTrue(jvm.containsKey("daemonThreadCount"));

        @SuppressWarnings("unchecked")
        Map<String, Object> system = (Map<String, Object>) result.get("system");
        Assert.assertTrue(system.containsKey("osName"));
        Assert.assertTrue(system.containsKey("availableProcessors"));
        Assert.assertTrue(system.containsKey("javaVersion"));

        @SuppressWarnings("unchecked")
        Map<String, Object> alertStatus = (Map<String, Object>) result.get("alertStatus");
        Assert.assertTrue((Boolean) alertStatus.get("enabled"));
        Assert.assertEquals("active", alertStatus.get("exceptionAlert"));
    }
}
