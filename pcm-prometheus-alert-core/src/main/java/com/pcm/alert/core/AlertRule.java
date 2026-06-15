package com.pcm.alert.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 告警规则模型（预留规则引擎）。
 * <p>
 * 当前 MVP 阶段由 {@link DefaultAlertRuleEvaluator} 做简单判断，
 * 后续可通过配置驱动此模型实现更复杂的规则匹配。
 * </p>
 */
public class AlertRule {

    /** 规则唯一标识 */
    private String ruleId;

    /** 是否启用 */
    private boolean enabled = true;

    /** 匹配的告警类型 */
    private AlertType type;

    /** 告警级别 */
    private AlertLevel level = AlertLevel.WARN;

    /** 阈值 */
    private double threshold;

    /** 冷却时间（秒） */
    private long cooldownSeconds = 300;

    /** 滑动窗口大小 */
    private int windowSize = 3;

    /** 是否支持恢复事件 */
    private boolean recoverable;

    /** 包含路径模式 */
    private List<String> include = new ArrayList<>();

    /** 排除路径模式 */
    private List<String> exclude = new ArrayList<>();

    // ---- getters / setters ----

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public AlertLevel getLevel() {
        return level;
    }

    public void setLevel(AlertLevel level) {
        this.level = level;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public long getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(long cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public boolean isRecoverable() {
        return recoverable;
    }

    public void setRecoverable(boolean recoverable) {
        this.recoverable = recoverable;
    }

    public List<String> getInclude() {
        return include;
    }

    public void setInclude(List<String> include) {
        this.include = include;
    }

    public List<String> getExclude() {
        return exclude;
    }

    public void setExclude(List<String> exclude) {
        this.exclude = exclude;
    }
}
