package com.pcm.alert.core;

/**
 * 告警消息渲染器接口。
 * <p>
 * 将 {@link AlertEvent} 转换为 {@link AlertMessage}，
 * 负责格式化标题、内容、附加属性等。
 * </p>
 */
public interface AlertMessageRenderer {

    /**
     * 将事件渲染为可推送的消息。
     *
     * @param event 告警事件
     * @return 渲染后的消息
     */
    AlertMessage render(AlertEvent event);
}
