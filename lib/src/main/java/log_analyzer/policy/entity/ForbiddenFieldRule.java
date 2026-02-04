package log_analyzer.policy.entity;

public class ForbiddenFieldRule {
	private final String name;
    private final MatchType match;

    public ForbiddenFieldRule(String name, MatchType match) {
        this.name = name;
        this.match = match;
    }

    public String getName() {
        return name;
    }

    public MatchType getMatch() {
        return match;
    }

    public boolean matches(String text) {
        if (text == null) return false;

        return switch (match) {
            case EXACT -> text.equals(name);
            case CONTAINS -> text.contains(name);
            case PREFIX -> text.startsWith(name);
            case SUFFIX -> text.endsWith(name);
            case REGEX -> text.matches(name);
        };
    }
    
}
