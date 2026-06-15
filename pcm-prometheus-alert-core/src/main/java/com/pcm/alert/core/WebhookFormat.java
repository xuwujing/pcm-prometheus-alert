package com.pcm.alert.core;

/**
 * Webhook 消息格式枚举。
 * <p>
 * 不同 IM 平台的消息结构不同，推送器根据此枚举选择对应的 payload 构建策略。
 * </p>
 */
public enum WebhookFormat {
    /** 默认 JSON 格式（通用） */
    DEFAULT,
    /** 钉钉机器人消息格式 */
    DINGTALK,
    /** 飞书机器人消息格式 */
    FEISHU,
    /** 企业微信机器人消息格式 */
    WECOM
}
