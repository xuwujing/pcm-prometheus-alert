# PCM-Prometheus-Alert 告警推送组件

> 轻量、可扩展、开箱即用的 Spring Boot 告警推送 starter

## 简介

PCM-Prometheus-Alert 是一个 Spring Boot Starter，业务项目只需引入依赖、配置 webhook，即可自动获得异常告警、慢请求告警、HTTP 状态码告警和 JVM 指标告警能力。

**核心特性：**

- 🚨 异常告警：自动捕获未处理异常，支持异常过滤和堆栈裁剪
- ⏱️ 慢请求告警：基于过滤器统计请求耗时，支持路径排除
- 🔢 HTTP 状态码告警：自动检测 5xx 响应并推送
- 📊 JVM 指标告警：内存、线程、CPU 采样，支持恢复事件
- 🗄️ SQL 告警：基于 Druid 的慢 SQL 检测和数据源连接池监控
- 🔇 智能降噪：冷却窗口去重，避免告警刷屏
- 📡 多平台推送：支持默认 JSON、钉钉、飞书、企业微信消息格式
- 🎯 级别路由：按 FATAL/ERROR/WARN/INFO 分发到不同 webhook
- 🔌 扩展集成：Prometheus 指标暴露、SkyWalking 链路追踪、ELK 日志采集
- 🖥️ Web 管理界面：内置仪表盘，实时查看 JVM 指标和告警状态
- 🧩 零依赖启动：不依赖 Prometheus / SkyWalking / ELK 即可独立运行
- 🧪 内置 demo：mock webhook 接收器，本地即可验证全链路

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.x | 应用框架 |
| Jackson | 2.13.x | JSON 序列化 |
| Druid | 1.2.x | 数据库连接池（SQL 告警模块） |
| Micrometer | 1.9.x | Prometheus 指标暴露 |
| JUnit | 4.13.x | 单元测试 |
| Mockito | 4.x | Mock 测试 |

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+

### 方式一：本地 demo 体验（零依赖）

```bash
# 1. 克隆项目
git clone https://github.com/your-org/pcm-prometheus-alert.git
cd pcm-prometheus-alert

# 2. 编译
mvn clean package -DskipTests

# 3. 启动 demo
java -jar pcm-prometheus-alert-demo/target/pcm-prometheus-alert-demo-0.1.0-SNAPSHOT.jar
```

启动后访问：

| 端点 | 说明 |
|------|------|
| `GET /demo/ok` | 正常响应 |
| `GET /demo/error` | 触发异常告警 |
| `GET /demo/slow?millis=1500` | 触发慢请求告警 |
| `GET /demo/status-500` | 触发 HTTP 状态码告警 |
| `POST /mock/webhook` | Mock webhook 接收器（查看推送内容） |

### 方式二：业务项目接入

1. 添加 Maven 依赖：

```xml
<dependency>
    <groupId>com.pcm.alert</groupId>
    <artifactId>pcm-prometheus-alert-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

2. 配置 application.yml：

```yaml
pcm:
  alert:
    enabled: true
    webhook: https://your-im-webhook-url
    service-name: my-service
    environment: prod
```

3. 启动应用，告警自动生效。

## 项目结构

```
pcm-prometheus-alert/
├── pcm-prometheus-alert-core/                  # 核心模型与接口
│   └── src/main/java/com/pcm/alert/core/
│       ├── AlertEvent.java                     # 统一告警事件模型
│       ├── AlertMessage.java                   # 渲染后的可推送消息
│       ├── AlertManager.java                   # 告警链路核心调度器
│       ├── AlertPublisher.java                 # 推送器接口
│       ├── WebhookAlertPublisher.java          # HTTP webhook 推送器
│       ├── AsyncAlertPublisher.java            # 异步推送器（内存队列）
│       ├── NoopAlertPublisher.java             # 空推送器（兜底）
│       ├── AlertDeduplicator.java              # 去重器接口
│       ├── SimpleAlertDeduplicator.java        # 基于内存的冷却窗口去重
│       ├── AlertRuleEvaluator.java             # 规则评估器接口
│       ├── DefaultAlertRuleEvaluator.java      # 默认规则评估器
│       ├── AlertMessageRenderer.java           # 消息渲染器接口
│       ├── DefaultAlertMessageRenderer.java    # 默认 key:value 渲染器
│       ├── WebhookFormat.java                  # 消息格式枚举
│       ├── WebhookPayloadBuilder.java          # Payload 构建器接口
│       ├── DefaultWebhookPayloadBuilder.java   # 默认 JSON 格式
│       ├── DingTalkWebhookPayloadBuilder.java  # 钉钉格式
│       ├── FeishuWebhookPayloadBuilder.java    # 飞书格式
│       ├── WeComWebhookPayloadBuilder.java     # 企业微信格式
│       ├── AlertType.java                      # 告警类型枚举
│       ├── AlertLevel.java                     # 告警级别枚举
│       ├── AlertSource.java                    # 事件来源枚举
│       ├── AlertRule.java                      # 告警规则模型
│       └── AlertEventCollector.java            # 采集器接口
├── pcm-prometheus-alert-spring-boot-starter/   # Spring Boot 自动装配
│   └── src/main/java/com/pcm/alert/starter/
│       ├── PcmAlertAutoConfiguration.java      # 自动装配类
│       ├── AlertProperties.java                # 配置属性
│       ├── SpringAlertEventFactory.java        # 事件工厂
│       ├── AlertExceptionResolver.java         # 异常告警处理器
│       ├── SlowRequestFilter.java              # 慢请求过滤器
│       └── MetricAlertCollector.java           # 指标采集器
├── pcm-prometheus-alert-sql-starter/           # SQL 告警（Druid 集成）
│   └── src/main/java/com/pcm/alert/sql/
│       ├── SqlAlertAutoConfiguration.java      # SQL 告警自动装配
│       ├── SqlAlertProperties.java             # SQL 告警配置
│       ├── SqlAlertCollector.java              # SQL 告警采集器
│       └── DruidEventAdapter.java              # Druid 事件适配器
├── pcm-prometheus-alert-extensions/            # 扩展集成
│   └── src/main/java/com/pcm/alert/extensions/
│       ├── ExtensionsAutoConfiguration.java    # 扩展自动装配
│       ├── PrometheusAlertMetrics.java         # Prometheus 指标暴露
│       ├── SkyWalkingTraceExtractor.java       # SkyWalking 链路追踪
│       └── ElkAlertLogger.java                # ELK 日志采集
├── pcm-prometheus-alert-web/                   # Web 管理界面
│   └── src/main/
│       ├── java/com/pcm/alert/web/
│       │   ├── WebApplication.java             # 启动类
│       │   └── DashboardController.java        # 仪表盘 API
│       └── resources/static/index.html         # 仪表盘页面
├── pcm-prometheus-alert-demo/                  # 本地演示
│   └── src/main/java/com/pcm/alert/demo/
│       ├── DemoApplication.java                # 启动类
│       ├── DemoController.java                 # 测试端点
│       └── MockWebhookController.java          # Mock webhook 接收器
└── docs/                                       # 设计文档
    ├── 06-开发记录.md
    ├── 08-MVP任务拆分.md
    ├── 10-详细设计说明.md
    └── 12-其他智能体接手说明.md
```

## 告警类型

| 类型 | 枚举 | 级别 | 来源 | 说明 |
|------|------|------|------|------|
| 未捕获异常 | `EXCEPTION` | ERROR | MVC | 全局异常处理器捕获 |
| 慢请求 | `SLOW_REQUEST` | WARN | FILTER | 请求耗时超过阈值 |
| HTTP 状态码 | `HTTP_STATUS` | ERROR | FILTER | 响应 5xx 状态码 |
| JVM 内存 | `JVM_MEMORY` | WARN | METRIC | 堆内存使用率过高 |
| CPU 使用率 | `CPU_USAGE` | WARN | METRIC | CPU 负载过高（默认关闭） |
| 线程数 | `THREAD_COUNT` | WARN | METRIC | 线程数超过阈值 |
| 慢 SQL | `SLOW_SQL` | WARN | SQL | Druid FilterEventAdapter 拦截 |
| 数据源 | `DATASOURCE` | WARN | SQL | Druid 连接池状态检查 |
| 自定义 | `CUSTOM` | — | — | 通过 API 手动触发 |

## 告警链路

```
采集器 → AlertEvent → AlertManager
                         ├── AlertRuleEvaluator.shouldAlert()
                         ├── AlertDeduplicator.allow()
                         ├── AlertMessageRenderer.render()
                         └── AlertPublisher.publish()
                              └── AsyncAlertPublisher → WebhookAlertPublisher
                                                       ├── DefaultWebhookPayloadBuilder
                                                       ├── DingTalkWebhookPayloadBuilder
                                                       ├── FeishuWebhookPayloadBuilder
                                                       └── WeComWebhookPayloadBuilder
```

## 配置说明

### 完整配置项

```yaml
pcm:
  alert:
    # ====== 基础配置 ======
    enabled: false                  # 总开关
    webhook:                        # webhook 地址（为空则仅打日志）
    webhook-format: default         # 消息格式：default / dingtalk / feishu / wecom
    service-name:                   # 服务名（不配则取 spring.application.name）
    environment:                    # 环境标识（不配则取 spring.profiles.active）

    # ====== 推送器 ======
    publisher:
      async: true                   # 是否异步推送
      queue-size: 1000              # 异步队列容量
      timeout-ms: 3000              # HTTP 请求超时（毫秒）

    # ====== 去重 ======
    dedupe:
      enabled: true                 # 是否启用去重
      cooldown-seconds: 300         # 冷却时间（秒），同 key 在此时间内只发一次

    # ====== 异常告警 ======
    exception:
      enabled: true                 # 是否启用异常告警
      stack-trace-max-lines: 20     # 堆栈最大行数（超过截断）
      exclude-exceptions:           # 排除的异常全限定类名
        - org.springframework.web.bind.MissingServletRequestParameterException
        - org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
        - org.springframework.web.HttpRequestMethodNotSupportedException
        - org.springframework.web.HttpMediaTypeNotSupportedException

    # ====== 请求告警 ======
    request:
      enabled: true                 # 是否启用请求告警
      slow-threshold-ms: 1000       # 慢请求阈值（毫秒）
      status-code-alert-enabled: true  # 是否启用 HTTP 状态码告警
      status-code-alert-thresholds:    # 触发告警的状态码
        - 500
        - 502
        - 503
        - 504
      exclude-paths:                # 排除路径（Ant 风格）
        - /actuator/**
        - /health
        - /favicon.ico

    # ====== 指标告警 ======
    metric:
      enabled: true                 # 是否启用指标告警
      interval-seconds: 30          # 采集间隔（秒）
      jvm-memory-threshold: 0.8     # JVM 堆内存使用率阈值（0-1）
      thread-threshold: 500         # 线程数阈值
      cpu-enabled: false            # 是否启用 CPU 告警
      cpu-threshold: 0.8            # CPU 使用率阈值（0-1）
      recovery-enabled: false       # 是否启用恢复事件（指标回落后发通知）

    # ====== 扩展集成 ======
    extensions:
      prometheus-enabled: false     # 是否启用 Prometheus 指标暴露
      skywalking-enabled: false     # 是否启用 SkyWalking 链路追踪集成
      elk-enabled: false            # 是否启用 ELK 日志集成

    # ====== 自定义告警 ======
    custom:
      enabled: true                 # 是否启用自定义告警 API
      level-routing-enabled: false  # 是否启用按级别路由
      fatal-webhook:                # FATAL 级别 webhook
      error-webhook:                # ERROR 级别 webhook
      warn-webhook:                 # WARN 级别 webhook
      info-webhook:                 # INFO 级别 webhook
```

### 配置项说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `pcm.alert.enabled` | false | 总开关，设为 true 后自动装配生效 |
| `pcm.alert.webhook` | — | webhook 地址，为空时仅打日志不推送 |
| `pcm.alert.webhook-format` | default | 消息格式：default / dingtalk / feishu / wecom |
| `pcm.alert.service-name` | spring.application.name | 服务名 |
| `pcm.alert.environment` | spring.profiles.active | 环境标识 |
| `pcm.alert.publisher.async` | true | 是否异步推送 |
| `pcm.alert.publisher.queue-size` | 1000 | 异步队列容量 |
| `pcm.alert.publisher.timeout-ms` | 3000 | HTTP 请求超时（毫秒） |
| `pcm.alert.dedupe.enabled` | true | 是否启用去重 |
| `pcm.alert.dedupe.cooldown-seconds` | 300 | 冷却时间（秒） |
| `pcm.alert.exception.enabled` | true | 是否启用异常告警 |
| `pcm.alert.exception.stack-trace-max-lines` | 20 | 堆栈最大行数 |
| `pcm.alert.request.enabled` | true | 是否启用请求告警 |
| `pcm.alert.request.slow-threshold-ms` | 1000 | 慢请求阈值（毫秒） |
| `pcm.alert.request.status-code-alert-enabled` | true | 是否启用状态码告警 |
| `pcm.alert.metric.enabled` | true | 是否启用指标告警 |
| `pcm.alert.metric.interval-seconds` | 30 | 采集间隔（秒） |
| `pcm.alert.metric.jvm-memory-threshold` | 0.8 | JVM 堆内存使用率阈值 |
| `pcm.alert.metric.thread-threshold` | 500 | 线程数阈值 |
| `pcm.alert.metric.cpu-enabled` | false | 是否启用 CPU 告警 |
| `pcm.alert.metric.recovery-enabled` | false | 是否启用恢复事件 |
| `pcm.alert.extensions.prometheus-enabled` | false | 是否启用 Prometheus 集成 |
| `pcm.alert.extensions.skywalking-enabled` | false | 是否启用 SkyWalking 集成 |
| `pcm.alert.extensions.elk-enabled` | false | 是否启用 ELK 集成 |
| `pcm.alert.custom.level-routing-enabled` | false | 是否启用按级别路由 |

### SQL 告警配置（需引入 pcm-prometheus-alert-sql-starter）

```yaml
pcm:
  alert:
    sql:
      enabled: false                # 是否启用 SQL 告警
      slow-sql-threshold-ms: 1000   # 慢 SQL 阈值（毫秒）
      datasource-alert-enabled: true  # 是否启用数据源连接告警
      active-count-threshold: 50    # 活跃连接数阈值
      wait-thread-count-threshold: 10  # 等待线程数阈值
```

## Web 管理界面

启动 web 模块后访问 `http://localhost:8090`：

```bash
cd pcm-prometheus-alert-web
mvn spring-boot:run
```

仪表盘提供 JVM 内存、线程、系统信息和告警状态的实时监控。

## 测试

```bash
mvn test
```

当前测试覆盖：

| 模块 | 测试类 | 用例数 |
|------|--------|--------|
| core | `AlertManagerTest` | 8 |
| core | `DefaultAlertMessageRendererTest` | 1 |
| core | `SimpleAlertDeduplicatorTest` | 2 |
| core | `WebhookPayloadBuilderTest` | 7 |
| starter | `SpringAlertEventFactoryTest` | 11 |
| starter | `SlowRequestFilterTest` | 7 |
| starter | `PcmAlertAutoConfigurationTest` | 9 |
| demo | `DemoApplicationTests` | 3 |
| **合计** | | **48** |

## 扩展集成

引入 `pcm-prometheus-alert-extensions` 模块后，通过配置即可启用：

```yaml
pcm:
  alert:
    extensions:
      prometheus-enabled: true    # 通过 /actuator/prometheus 暴露 pcm_alert_total 指标
      skywalking-enabled: true    # 从 SkyWalking agent 自动提取 traceId
      elk-enabled: true           # 将告警事件以 JSON 格式写入日志供 logstash 采集
```

启用后，`ExtensionsAutoConfiguration` 自动装配对应的 Bean：
- `PrometheusAlertMetrics`：将告警事件计数注册为 `pcm_alert_total` Counter
- `SkyWalkingTraceExtractor`：通过反射调用 `TraceContext.traceId()` 补充 traceId
- `ElkAlertLogger`：以 `pcm.alert.event:` 前缀输出结构化 JSON 日志

## 设计文档

> 设计文档托管在独立私有仓库中，公开仓库仅包含代码和 README。
> 如需访问完整设计文档，请联系项目维护者。

## 常见问题

### 没有 Prometheus 能启动吗？

可以。项目不依赖 Prometheus / SkyWalking / ELK，默认即可独立运行。

### webhook 地址为空怎么办？

系统自动降级为 `NoopAlertPublisher`，仅打印 WARN 日志，不影响业务。

### 如何自定义消息格式？

实现 `WebhookPayloadBuilder` 接口，通过 `WebhookAlertPublisher.registerPayloadBuilder()` 注册即可。

### 如何通过 API 手动触发告警？

注入 `AlertManager`，调用 `alertManager.publish(alertMessage)` 即可。

### 如何按告警级别分发到不同群？

配置 `pcm.alert.custom.level-routing-enabled=true` 并设置各级别 webhook 地址。

## 版本

| 版本 | 说明 |
|------|------|
| v0.1.0 | MVP：异常/慢请求/状态码/指标告警 + 多平台推送 + 级别路由 + 扩展集成预留 |

## 开源协议

MIT License
