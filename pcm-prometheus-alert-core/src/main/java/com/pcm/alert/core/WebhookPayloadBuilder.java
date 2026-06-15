package com.pcm.alert.core;

/**
 * Webhook 消息体构建器接口。
 * <p>
 * 每种 IM 平台实现各自的 payload 格式。
 * </p>
 */
public interface WebhookPayloadBuilder {

    /**
     * 将告警消息转换为平台特定的 JSON 字符串。
     *
     * @param message 告警消息
     * @return JSON 字符串
     * @throws Exception 序列化异常
     */
    String build(AlertMessage message) throws Exception;
}
