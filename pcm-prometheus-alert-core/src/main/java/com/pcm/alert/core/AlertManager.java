package com.pcm.alert.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 告警管理器 —— 告警链路的核心调度器。
 * <p>
 * 职责：接收事件 → 规则判断 → 去重/冷却 → 渲染消息 → 推送。
 * 不关心事件来源，只做统一调度。
 * </p>
 * <p>
 * 支持按告警级别路由到不同 webhook 地址。
 * </p>
 */
public class AlertManager {
    private static final Logger log = LoggerFactory.getLogger(AlertManager.class);

    private final AlertRuleEvaluator ruleEvaluator;
    private final AlertDeduplicator deduplicator;
    private final AlertMessageRenderer messageRenderer;
    private final AlertPublisher publisher;

    /** 按级别路由的 webhook 地址 */
    private String fatalWebhook;
    private String errorWebhook;
    private String warnWebhook;
    private String infoWebhook;
    private boolean levelRoutingEnabled;

    public AlertManager(AlertRuleEvaluator ruleEvaluator,
                        AlertDeduplicator deduplicator,
                        AlertMessageRenderer messageRenderer,
                        AlertPublisher publisher) {
        this.ruleEvaluator = ruleEvaluator;
        this.deduplicator = deduplicator;
        this.messageRenderer = messageRenderer;
        this.publisher = publisher;
    }

    /**
     * 配置按告警级别的 webhook 路由。
     */
    public void configureLevelRouting(boolean enabled,
                                      String fatalWebhook, String errorWebhook,
                                      String warnWebhook, String infoWebhook) {
        this.levelRoutingEnabled = enabled;
        this.fatalWebhook = fatalWebhook;
        this.errorWebhook = errorWebhook;
        this.warnWebhook = warnWebhook;
        this.infoWebhook = infoWebhook;
    }

    /**
     * 处理告警事件。
     * <p>
     * 流程：规则评估 → 去重检查 → 渲染消息 → 级别路由 → 异步推送 → 记录去重状态。
     * 任一步骤不满足则静默丢弃。
     * </p>
     */
    public void onEvent(AlertEvent event) {
        if (event == null) {
            return;
        }
        if (!ruleEvaluator.shouldAlert(event)) {
            return;
        }
        if (!deduplicator.allow(event)) {
            log.debug("Alert event suppressed by deduplicator. type={}, summary={}", event.getType(), event.getSummary());
            return;
        }
        AlertMessage message = messageRenderer.render(event);
        applyLevelRouting(message);
        publisher.publish(message);
        deduplicator.record(event);
    }

    /**
     * 发送恢复事件（当前与 onEvent 行为一致，预留扩展）。
     */
    public void recover(AlertEvent event) {
        onEvent(event);
    }

    /**
     * 直接推送自定义消息（供外部 API 调用）。
     *
     * @param message 自定义告警消息
     */
    public void publish(AlertMessage message) {
        if (message == null) {
            return;
        }
        applyLevelRouting(message);
        publisher.publish(message);
    }

    private void applyLevelRouting(AlertMessage message) {
        if (!levelRoutingEnabled || message.getLevel() == null) {
            return;
        }
        String routedWebhook = null;
        switch (message.getLevel()) {
            case FATAL: routedWebhook = fatalWebhook; break;
            case ERROR: routedWebhook = errorWebhook; break;
            case WARN:  routedWebhook = warnWebhook;  break;
            case INFO:  routedWebhook = infoWebhook;  break;
        }
        if (routedWebhook != null && !routedWebhook.trim().isEmpty()) {
            message.setWebhook(routedWebhook);
        }
    }
}
