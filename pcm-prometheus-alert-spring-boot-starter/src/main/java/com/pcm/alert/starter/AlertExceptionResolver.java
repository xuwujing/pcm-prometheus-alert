package com.pcm.alert.starter;

import com.pcm.alert.core.AlertManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 异常告警处理器 —— 注册为最高优先级的 HandlerExceptionResolver。
 * <p>
 * 捕获所有未处理异常，构造告警事件并送入 {@link AlertManager}。
 * 返回 null 表示不处理响应，交给后续处理器（如 Spring 默认的 DefaultHandlerExceptionResolver）。
 * </p>
 * <p>
 * 异常过滤：匹配 excludeExceptions 的异常不触发告警（如参数校验异常、方法不支持异常等）。
 * </p>
 */
public class AlertExceptionResolver implements HandlerExceptionResolver, Ordered {
    private static final Logger log = LoggerFactory.getLogger(AlertExceptionResolver.class);

    private final AlertProperties properties;
    private final SpringAlertEventFactory eventFactory;
    private final AlertManager alertManager;

    public AlertExceptionResolver(AlertProperties properties, SpringAlertEventFactory eventFactory, AlertManager alertManager) {
        this.properties = properties;
        this.eventFactory = eventFactory;
        this.alertManager = alertManager;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception ex) {
        if (!properties.getException().isEnabled()) {
            return null;
        }
        if (eventFactory.isExcluded(ex)) {
            log.debug("Exception excluded from alert. type={}, path={}", ex.getClass().getName(), request.getRequestURI());
            return null;
        }
        alertManager.onEvent(eventFactory.fromException(ex, request));
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
