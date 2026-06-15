package com.pcm.alert.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 告警管理器 —— 告警链路的核心调度器。
 * <p>
 * 职责：接收事件 → 规则判断 → 去重/冷却 → 渲染消息 → 推送。
 * 不关心事件来源，只做统一调度。
 * </p>
 */
public class AlertManager {
    private static final Logger log = LoggerFactory.getLogger(AlertManager.class);

    private final AlertRuleEvaluator ruleEvaluator;
    private final AlertDeduplicator deduplicator;
    private final AlertMessageRenderer messageRenderer;
    private final AlertPublisher publisher;

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
     * 处理告警事件。
     * <p>
     * 流程：规则评估 → 去重检查 → 渲染消息 → 异步推送 → 记录去重状态。
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
        publisher.publish(message);
        deduplicator.record(event);
    }

    /**
     * 发送恢复事件（当前与 onEvent 行为一致，预留扩展）。
     */
    public void recover(AlertEvent event) {
        onEvent(event);
    }
}
