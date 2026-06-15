package com.pcm.alert.core;

/**
 * 告警规则评估器接口。
 * <p>
 * 实现类负责判断一个事件是否满足告警条件。
 * 默认实现仅检查 type 非空，业务方可替换为更复杂的规则引擎。
 * </p>
 */
public interface AlertRuleEvaluator {

    /**
     * 判断事件是否应触发告警。
     *
     * @param event 告警事件
     * @return true 应告警
     */
    boolean shouldAlert(AlertEvent event);
}
