# MVP 任务拆分

## 任务 1: 工程骨架

范围：

1. 创建 Maven 父工程
2. 创建 core 模块
3. 创建 starter 模块
4. 创建 demo 模块

验收：

1. `mvn clean test` 通过
2. demo 可启动
3. demo 能提供 mock webhook

## 任务 2: 核心告警模型

范围：

1. `AlertEvent`
2. `AlertMessage`
3. `AlertLevel`
4. `AlertType`
5. `AlertContext`

验收：

1. 模型具备单元测试
2. 字段能覆盖异常、请求、指标三类事件

## 任务 3: Webhook 推送

范围：

1. `AlertPublisher`
2. `WebhookAlertPublisher`
3. 超时配置
4. 失败日志
5. 简单 JSON 消息格式

验收：

1. mock webhook 可收到消息
2. webhook 不可达不影响业务

## 任务 4: Spring Boot 自动装配

范围：

1. `AlertProperties`
2. `PcmAlertAutoConfiguration`
3. 条件装配
4. 配置元数据
5. Spring Boot 2.x `spring.factories`

验收：

1. starter 被引入后配置生效
2. `enabled=false` 时不注册采集器

## 任务 5: 异常告警

范围：

1. MVC 异常采集
2. 异常摘要生成
3. traceId 读取
4. 异步推送

验收：

1. demo 中异常接口能推送告警
2. 同类异常在冷却时间内不重复刷屏

## 任务 6: 请求耗时告警

范围：

1. 请求过滤器
2. 耗时统计
3. 慢请求规则
4. 路径排除

验收：

1. 超过阈值的接口触发告警
2. 排除路径不触发告警

## 任务 7: 基础指标告警

范围：

1. Micrometer 接入
2. JVM 内存规则
3. CPU 规则
4. 线程规则
5. 本地无 Prometheus 时可独立运行

验收：

1. 指标采集器能按周期运行
2. 达到阈值时产生告警事件

## 任务 8: 文档与示例

范围：

1. README
2. 接入示例
3. 配置说明
4. 常见问题
5. demo 压测说明

验收：

1. 新用户可按文档完成接入
2. demo 工程覆盖异常和慢请求
