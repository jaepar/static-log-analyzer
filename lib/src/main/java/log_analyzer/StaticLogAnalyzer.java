package log_analyzer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import log_analyzer.engine.JavaLoggingCallExtractor;
import log_analyzer.engine.LogCall;
import log_analyzer.engine.SourceScanner;
import log_analyzer.policy.service.LoggingPolicy;
import log_analyzer.report.ReportWriter;
import log_analyzer.report.Violation;
import log_analyzer.rules.ForbiddenFieldLoggingRule;

public class StaticLogAnalyzer {
	
	private final SourceScanner scanner = new SourceScanner();
    private final JavaLoggingCallExtractor extractor = new JavaLoggingCallExtractor();
    private final ReportWriter reportWriter = new ReportWriter();

    public List<Violation> analyze(Path root, LoggingPolicy policy) {
        Set<String> logMethods = new HashSet<>(policy.getLogMethods());
        ForbiddenFieldLoggingRule rule = new ForbiddenFieldLoggingRule(policy.getForbiddenFields());

        List<Path> javaFiles = scanner.findJavaFiles(root);

        List<Violation> all = new ArrayList<>();

        for (Path f : javaFiles) {
            List<LogCall> calls = extractor.extract(f, logMethods);
            for (LogCall call : calls) {
                all.addAll(rule.evaluate(call));
            }
        }

        return all;
    }

    public void writeReport(Path reportPath, List<Violation> violations) {
        reportWriter.write(reportPath, violations);
    }
	
}
