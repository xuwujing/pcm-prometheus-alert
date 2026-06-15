package com.pcm.alert.core;

/**
 * 告警事件采集器接口（预留扩展点）。
 * <p>
 * 实现类负责从特定来源采集事件并送入 {@link AlertManager}。
 * </p>
 */
public interface AlertEventCollector {

    /** 是否支持当前环境 */
    boolean supports();

    /** 启动采集 */
    void start();

    /** 停止采集 */
    void stop();
}
