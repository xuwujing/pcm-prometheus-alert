package com.pcm.alert.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class DemoController {
    @GetMapping("/demo/ok")
    public Map<String, Object> ok() {
        return Collections.singletonMap("status", "ok");
    }

    @GetMapping("/demo/error")
    public Map<String, Object> error() {
        throw new IllegalStateException("Demo exception for pcm alert");
    }

    @GetMapping("/demo/slow")
    public Map<String, Object> slow(@RequestParam(defaultValue = "1500") long millis) throws InterruptedException {
        Thread.sleep(millis);
        return Collections.singletonMap("costMs", millis);
    }

    @GetMapping("/demo/pressure/slow-request")
    public Map<String, Object> pressureSlowRequest(@RequestParam(defaultValue = "20") int times,
                                                   @RequestParam(defaultValue = "1200") long millis) throws InterruptedException {
        for (int i = 0; i < times; i++) {
            Thread.sleep(Math.min(millis, 100));
        }
        return Collections.singletonMap("times", times);
    }
}
