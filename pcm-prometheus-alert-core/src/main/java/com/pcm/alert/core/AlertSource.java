package com.pcm.alert.core;

/**
 * 告警事件来源枚举。
 */
public enum AlertSource {
    /** Spring MVC 异常处理器 */
    MVC,
    /** Servlet 过滤器 */
    FILTER,
    /** AOP 切面（预留） */
    ASPECT,
    /** 指标采集器 */
    METRIC,
    /** SQL 拦截（预留） */
    SQL,
    /** 手动触发 */
    MANUAL
}
