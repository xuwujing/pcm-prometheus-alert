package com.pcm.alert.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PCM Alert Demo 启动类。
 * <p>
 * 本地启动后访问：
 * <ul>
 *   <li>GET /demo/ok —— 正常响应</li>
 *   <li>GET /demo/error —— 触发异常告警</li>
 *   <li>GET /demo/slow?millis=1500 —— 触发慢请求告警</li>
 *   <li>GET /demo/status-500 —— 触发 HTTP 状态码告警</li>
 * </ul>
 * Mock webhook 接收器在 /mock/webhook。
 * </p>
 */
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
