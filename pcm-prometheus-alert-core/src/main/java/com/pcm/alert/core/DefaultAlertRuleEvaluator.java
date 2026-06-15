package com.pcm.alert.core;

public class DefaultAlertRuleEvaluator implements AlertRuleEvaluator {
    @Override
    public boolean shouldAlert(AlertEvent event) {
        return event != null && event.getType() != null;
    }
}
