package com.pcm.alert.core;

public interface AlertRuleEvaluator {
    boolean shouldAlert(AlertEvent event);
}
