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
		//args에서 받은 내용 파싱
        Map<String, String> opt = parseArgs(args);

        //args에서 받은 각각 나누어 저장
        Path root = Path.of(opt.getOrDefault("--root", "."));
        Path policyPath = Path.of(opt.getOrDefault("--policy", "src/main/resources/logging-policy.yml"));
        Path reportPath = Path.of(opt.getOrDefault("--report", "build/logging-report/report.txt"));

        // yml에 있는 내용을 추출하여 LoggingPolicy에 저장
        LoggingPolicy policy;
        try {
            //규정 파일을 읽는 메소드 실행
            policy = new PolicyLoader().load(policyPath);
        } catch (ParserException e) {
            System.err.println("[ERROR] " + e.getMessage());
            System.exit(2);
            return;
        }

        // 규정 위반 사항 체크 클래스 객체 생성
        StaticLogAnalyzer analyzer = new StaticLogAnalyzer();
        //규정을 위반한 내용을 저장
        List<Violation> violations = analyzer.analyze(root, policy);
        //위반 사항 기록
        analyzer.writeReport(reportPath, violations);

        //만약 1건 이상의 문제가 발생하는 경우
        //콘솔에 규정 위반 사항이 발생했음을 출력
        if (!violations.isEmpty()) {
            System.err.println("[FAIL] Logging violations found: " + violations.size());
            System.err.println("Report: " + reportPath.toAbsolutePath());
            System.exit(1);
        }

        System.out.println("[OK] No violations. Report: " + reportPath.toAbsolutePath());
    }

    // args 파싱 메소드
    // build 파일에 존재하는 내용을 잘라서 전달
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