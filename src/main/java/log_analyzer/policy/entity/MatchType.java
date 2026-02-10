package log_analyzer.policy.entity;

public enum MatchType {
    EXACT,
    CONTAINS,
    PREFIX,
    SUFFIX,
    REGEX;

    public static MatchType from(String raw) {
        if (raw == null) return EXACT;
        return MatchType.valueOf(raw.trim().toUpperCase());
    }
    
}