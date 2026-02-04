package log_analyzer.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import log_analyzer.engine.LogArgInspector;
import log_analyzer.engine.LogCall;
import log_analyzer.policy.entity.ForbiddenFieldRule;
import log_analyzer.report.Violation;

public class ForbiddenFieldLoggingRule {
	public static final String RULE_NAME = "FORBIDDEN_FIELD_LOGGING";

    private final List<ForbiddenFieldRule> forbiddenFields;
    private final LogArgInspector inspector = new LogArgInspector();

    public ForbiddenFieldLoggingRule(List<ForbiddenFieldRule> forbiddenFields) {
        this.forbiddenFields = forbiddenFields;
    }

    public List<Violation> evaluate(LogCall call) {
    	List<Violation> violations = new ArrayList<>();

        List<String> allTokens = new ArrayList<>();
        call.getArgs().forEach(arg -> allTokens.addAll(inspector.extractTokens(arg)));

        java.util.Set<String> dedup = new java.util.HashSet<>();

        for (String token : allTokens) {
            for (ForbiddenFieldRule rule : forbiddenFields) {
                if (rule.matches(token)) {

                    String key = call.getFile() + ":" +
                                 call.getLine() + ":" +
                                 RULE_NAME + ":" +
                                 rule.getName() + ":" +
                                 call.getMethodFqn();

                    if (!dedup.add(key)) continue;

                    violations.add(new Violation(
                            call.getFile(),
                            call.getLine(),
                            RULE_NAME,
                            "Variable/Token '" + rule.getName() + "' is logged via " + call.getMethodFqn(),
                            buildCodeSnippet(call)
                    ));
                }
            }
        }

        return violations;
    }

    private String buildCodeSnippet(LogCall call) {
        StringBuilder sb = new StringBuilder();
        sb.append(call.getMethodFqn()).append("(");
        for (int i = 0; i < call.getArgs().size(); i++) {
            sb.append(call.getArgs().get(i));
            if (i < call.getArgs().size() - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
	
}
