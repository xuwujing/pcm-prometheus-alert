package com.pcm.alert.core;

/**
 * 告警推送器接口。
 * <p>
 * 实现类负责将 {@link AlertMessage} 发送到目标通道（webhook、IM 等）。
 * 推送失败不应向上抛出异常，仅记录日志。
 * </p>
 */
public interface AlertPublisher {

    /**
     * 推送告警消息。
     *
     * @param message 告警消息
     */
    void publish(AlertMessage message);
}
