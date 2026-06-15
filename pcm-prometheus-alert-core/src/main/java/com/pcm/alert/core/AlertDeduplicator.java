package com.pcm.alert.core;

/**
 * 告警去重器接口。
 * <p>
 * 实现类负责按维度（类型 + 服务 + 路径 + 摘要）做冷却窗口去重，
 * 避免同类告警短时间内重复刷屏。
 * </p>
 */
public interface AlertDeduplicator {

    /**
     * 判断当前事件是否允许发送。
     *
     * @param event 告警事件
     * @return true 允许发送，false 应抑制
     */
    boolean allow(AlertEvent event);

    /**
     * 记录事件已发送（用于更新冷却时间）。
     *
     * @param event 已发送的告警事件
     */
    void record(AlertEvent event);
}
