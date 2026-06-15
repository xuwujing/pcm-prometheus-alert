package com.pcm.alert.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertManager {
    private static final Logger log = LoggerFactory.getLogger(AlertManager.class);

    private final AlertRuleEvaluator ruleEvaluator;
    private final AlertDeduplicator deduplicator;
    private final AlertMessageRenderer messageRenderer;
    private final AlertPublisher publisher;

    public AlertManager(AlertRuleEvaluator ruleEvaluator,
                        AlertDeduplicator deduplicator,
                        AlertMessageRenderer messageRenderer,
                        AlertPublisher publisher) {
        this.ruleEvaluator = ruleEvaluator;
        this.deduplicator = deduplicator;
        this.messageRenderer = messageRenderer;
        this.publisher = publisher;
    }

    public void onEvent(AlertEvent event) {
        if (event == null) {
            return;
        }
        if (!ruleEvaluator.shouldAlert(event)) {
            return;
        }
        if (!deduplicator.allow(event)) {
            log.debug("Alert event suppressed by deduplicator. type={}, summary={}", event.getType(), event.getSummary());
            return;
        }
        AlertMessage message = messageRenderer.render(event);
        publisher.publish(message);
        deduplicator.record(event);
    }

    public void recover(AlertEvent event) {
        onEvent(event);
    }
}
