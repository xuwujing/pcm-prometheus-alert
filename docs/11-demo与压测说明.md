# Demo 与压测说明

## 1. demo 模块目标

demo 模块用于验证 starter 的可接入性，不是给业务方看的教学样例。

demo 需要覆盖：

1. 正常请求。
2. 异常请求。
3. 慢请求。
4. mock webhook 接收。
5. 指标采样。

## 2. 建议的 demo 结构

```text
pcm-prometheus-alert-demo
├── DemoApplication
├── controller
│   ├── DemoController
│   └── MockWebhookController
├── service
└── resources
```

## 3. 关键接口

### 3.1 `GET /demo/ok`

正常返回，用于确认基础链路正常。

### 3.2 `GET /demo/error`

主动抛异常，用于验证异常告警。

### 3.3 `GET /demo/slow?millis=1500`

睡眠指定毫秒数，用于验证慢请求告警。

### 3.4 `POST /mock/webhook`

接收告警消息并打印到日志或控制台。

## 4. 本地启动顺序

1. 启动 demo。
2. 将 webhook 指向本地 mock 接口。
3. 调用 `/demo/error`。
4. 调用 `/demo/slow?millis=1500`。
5. 观察 mock webhook 是否收到消息。

## 5. 压测目标

压测不是为了测极限吞吐，而是验证告警组件在高频事件下的稳定性。

目标：

1. webhook 发送是否阻塞业务。
2. 队列满时是否降级为日志。
3. 冷却时间是否有效。
4. 重复异常是否会被聚合。

## 6. 压测场景

### 6.1 异常风暴

持续请求 `/demo/error`，观察：

1. 告警是否被去重。
2. 发送线程是否堆积。
3. 业务接口是否仍能返回。

### 6.2 慢请求风暴

持续请求 `/demo/slow?millis=1500`，观察：

1. 慢请求是否触发告警。
2. 冷却窗口是否生效。

### 6.3 webhook 延迟

让 mock webhook 人为延迟响应，观察：

1. 发送超时是否生效。
2. 异步队列是否能兜底。

## 7. 建议工具

1. JMeter
2. Apache Bench
3. Gatling
4. wrk

## 8. 压测验收

1. 业务接口不被 webhook 卡死。
2. 失败告警可记录日志。
3. 同类事件不会无限刷屏。

