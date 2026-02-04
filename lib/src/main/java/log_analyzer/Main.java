package log_analyzer;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log_analyzer.policy.service.LoggingPolicy;
import log_analyzer.policy.service.PolicyLoader;
import log_analyzer.report.Violation;
import log_analyzer.exception.ParserException;

public class Main {
	
	public static void main(String[] args) {
		Map<String, String> opt = parseArgs(args);

        Path root = Path.of(opt.getOrDefault("--root", "."));
        Path policyPath = Path.of(opt.getOrDefault("--policy", "logging-policy.yml"));
        Path reportPath = Path.of(opt.getOrDefault("--report", "build/logging-report/report.txt"));

        LoggingPolicy policy;
        try {
            policy = new PolicyLoader().load(policyPath);
        } catch (ParserException e) {
            System.err.println("[ERROR] " + e.getMessage());
            System.exit(2);
            return;
        }

        StaticLogAnalyzer analyzer = new StaticLogAnalyzer();
        List<Violation> violations = analyzer.analyze(root, policy);
        analyzer.writeReport(reportPath, violations);

        if (!violations.isEmpty()) {
            System.err.println("[FAIL] Logging violations found: " + violations.size());
            System.err.println("Report: " + reportPath.toAbsolutePath());
            System.exit(1);
        }

        System.out.println("[OK] No violations. Report: " + reportPath.toAbsolutePath());
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (String a : args) {
            int idx = a.indexOf('=');
            if (idx > 0) {
                map.put(a.substring(0, idx), a.substring(idx + 1));
            }
        }
        return map;
	}
}