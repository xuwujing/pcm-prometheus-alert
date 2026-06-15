package com.pcm.alert.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class MockWebhookController {
    private static final Logger log = LoggerFactory.getLogger(MockWebhookController.class);

    @PostMapping("/mock/webhook")
    public Map<String, Object> receive(@RequestBody String payload) {
        log.info("Mock webhook received: {}", payload);
        return Collections.singletonMap("success", true);
    }
}
