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

// 정적 로그 검증
public class StaticLogAnalyzer {
	
    // java 파일 스캐너
	private final SourceScanner scanner = new SourceScanner();
    //java 내부의 log 추출기
    private final JavaLoggingCallExtractor extractor = new JavaLoggingCallExtractor();
    //보고서 작성
    private final ReportWriter reportWriter = new ReportWriter();

    // 감지된 전달 log 명령어 탐지
    // 루트 경로와 규정을 저장한 클래스를 통해 실행
    public List<Violation> analyze(Path root, LoggingPolicy policy) {
        // 중복되는 위반 사항은 제거하기 위해 Set방식으로 저장
        Set<String> logMethods = new HashSet<>(policy.getLogMethods());
        
        // log에 대한 규정 사항 검증 클래스 호출(규정 사항을 넘김)
        ForbiddenFieldLoggingRule rule = new ForbiddenFieldLoggingRule(policy.getForbiddenFields());

        // java 파일 서칭
        List<Path> javaFiles = scanner.findJavaFiles(root);

        // 규정이 있는 log에 대하여 해당 규정을 위반한 내역을 저장하는 Violation List 객체
        List<Violation> all = new ArrayList<>();

        //서칭한 java 파일에 대해서 
        for (Path f : javaFiles) {
            // java 파일 내부에 존재하는 log 명령어 추출
            List<LogCall> calls = extractor.extract(f, logMethods);
            for (LogCall call : calls) {
                // 추출한 로그에 대해서 위반사항 검사
                all.addAll(rule.evaluate(call));
            }
        }

        return all;
    }

    public void writeReport(Path reportPath, List<Violation> violations) {
        reportWriter.write(reportPath, violations);
    }
	
}
