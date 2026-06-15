package com.pcm.alert.starter;

import com.pcm.alert.core.AlertManager;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 慢请求与状态码告警过滤器。
 * <p>
 * 拦截所有请求，记录耗时，触发两类告警：
 * <ul>
 *   <li>慢请求：耗时 ≥ slowThresholdMs</li>
 *   <li>HTTP 状态码：响应状态码命中 statusCodeAlertThresholds</li>
 * </ul>
 * 支持 Ant 风格路径排除（excludePaths），默认排除 actuator/health/favicon。
 * </p>
 */
public class SlowRequestFilter extends OncePerRequestFilter {
    private final AlertProperties properties;
    private final SpringAlertEventFactory eventFactory;
    private final AlertManager alertManager;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public SlowRequestFilter(AlertProperties properties, SpringAlertEventFactory eventFactory, AlertManager alertManager) {
        this.properties = properties;
        this.eventFactory = eventFactory;
        this.alertManager = alertManager;
    }

    /**
     * 路径排除：匹配 excludePaths 中任一模式则跳过。
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String[] excludePaths = properties.getRequest().getExcludePaths();
        if (excludePaths == null) {
            return false;
        }
        for (String pattern : excludePaths) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long costMs = System.currentTimeMillis() - start;
            if (properties.getRequest().isEnabled()) {
                if (costMs >= properties.getRequest().getSlowThresholdMs()) {
                    alertManager.onEvent(eventFactory.fromSlowRequest(request, costMs));
                }
                checkStatusCode(request, response, costMs);
            }
        }
    }

    private void checkStatusCode(HttpServletRequest request, HttpServletResponse response, long costMs) {
        if (!properties.getRequest().isStatusCodeAlertEnabled()) {
            return;
        }
        int status = response.getStatus();
        int[] thresholds = properties.getRequest().getStatusCodeAlertThresholds();
        if (thresholds == null) {
            return;
        }
        for (int threshold : thresholds) {
            if (status == threshold) {
                alertManager.onEvent(eventFactory.fromHttpStatus(request, status, costMs));
                return;
            }
        }
    }
}
