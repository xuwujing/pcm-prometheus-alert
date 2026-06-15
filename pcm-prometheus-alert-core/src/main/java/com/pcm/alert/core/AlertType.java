package com.pcm.alert.core;

/**
 * 告警类型枚举。
 */
public enum AlertType {
    /** 未捕获异常 */
    EXCEPTION,
    /** 慢请求 */
    SLOW_REQUEST,
    /** JVM 堆内存使用率过高 */
    JVM_MEMORY,
    /** CPU 使用率过高（预留） */
    CPU_USAGE,
    /** 线程数过多 */
    THREAD_COUNT,
    /** 慢 SQL（预留） */
    SLOW_SQL,
    /** 数据源异常（预留） */
    DATASOURCE,
    /** HTTP 错误状态码（5xx） */
    HTTP_STATUS,
    /** 自定义事件 */
    CUSTOM
}
