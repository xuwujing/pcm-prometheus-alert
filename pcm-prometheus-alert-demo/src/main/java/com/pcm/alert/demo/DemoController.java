package com.pcm.alert.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * Demo 控制器 —— 提供各类告警触发端点。
 */
@RestController
public class DemoController {

    /** 正常响应 */
    @GetMapping("/demo/ok")
    public Map<String, Object> ok() {
        return Collections.singletonMap("status", "ok");
    }

    /** 触发异常告警 */
    @GetMapping("/demo/error")
    public Map<String, Object> error() {
        throw new IllegalStateException("Demo exception for pcm alert");
    }

    /** 触发慢请求告警（默认 1500ms） */
    @GetMapping("/demo/slow")
    public Map<String, Object> slow(@RequestParam(defaultValue = "1500") long millis) throws InterruptedException {
        Thread.sleep(millis);
        return Collections.singletonMap("costMs", millis);
    }

    /** 触发 HTTP 500 状态码告警 */
    @GetMapping("/demo/status-500")
    public ResponseEntity<Map<String, Object>> status500() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Simulated 500"));
    }

    /** 压测端点：批量慢请求 */
    @GetMapping("/demo/pressure/slow-request")
    public Map<String, Object> pressureSlowRequest(@RequestParam(defaultValue = "20") int times,
                                                   @RequestParam(defaultValue = "1200") long millis) throws InterruptedException {
        for (int i = 0; i < times; i++) {
            Thread.sleep(Math.min(millis, 100));
        }
        return Collections.singletonMap("times", times);
    }
}
