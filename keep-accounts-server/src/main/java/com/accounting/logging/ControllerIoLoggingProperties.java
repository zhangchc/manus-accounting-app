package com.accounting.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller 入参/出参日志配置。
 */
@Component
@ConfigurationProperties(prefix = "logging.controller-io")
public class ControllerIoLoggingProperties {

    /**
     * 总开关。
     */
    private boolean enabled = true;

    /**
     * 是否记录入参。
     */
    private boolean logArgs = true;

    /**
     * 是否记录出参。
     */
    private boolean logResult = true;

    /**
     * 单条日志最大长度，超出会截断。
     */
    private int maxLen = 32 * 1024;

    /**
     * 脱敏策略：none / default（预留）。
     * 你当前选择 raw，因此默认 none。
     */
    private String masking = "none";

    /**
     * 排除路径（按 requestURI 的前缀匹配）。默认排除登录接口。
     * 注意：本项目 context-path 为 /api，因此 requestURI 形如 /api/user/login。
     */
    private List<String> excludePaths = new ArrayList<>();

    public ControllerIoLoggingProperties() {
        excludePaths.add("/api/user/login");
        excludePaths.add("/api/user/wxLogin");
        excludePaths.add("/error");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLogArgs() {
        return logArgs;
    }

    public void setLogArgs(boolean logArgs) {
        this.logArgs = logArgs;
    }

    public boolean isLogResult() {
        return logResult;
    }

    public void setLogResult(boolean logResult) {
        this.logResult = logResult;
    }

    public int getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(int maxLen) {
        this.maxLen = maxLen;
    }

    public String getMasking() {
        return masking;
    }

    public void setMasking(String masking) {
        this.masking = masking;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }
}

