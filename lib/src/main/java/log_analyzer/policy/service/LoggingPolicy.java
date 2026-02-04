package log_analyzer.policy.service;

import java.util.List;

import log_analyzer.policy.entity.ForbiddenFieldRule;

public class LoggingPolicy {
    private final List<ForbiddenFieldRule> forbiddenFields;
    private final List<String> logMethods;

    public LoggingPolicy(List<ForbiddenFieldRule> forbiddenFields, List<String> logMethods) {
        this.forbiddenFields = forbiddenFields;
        this.logMethods = logMethods;
    }

    public List<ForbiddenFieldRule> getForbiddenFields() {
        return forbiddenFields;
    }

    public List<String> getLogMethods() {
        return logMethods;
    }
}