package com.pcm.alert.core;

/**
 * 默认规则评估器 —— MVP 阶段的最简实现。
 * <p>
 * 仅检查事件非空且 type 非空即放行。
 * 后续可替换为配置驱动的规则匹配。
 * </p>
 */
public class DefaultAlertRuleEvaluator implements AlertRuleEvaluator {
    @Override
    public boolean shouldAlert(AlertEvent event) {
        return event != null && event.getType() != null;
    }
}
