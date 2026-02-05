package log_analyzer.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import log_analyzer.engine.LogArgInspector;
import log_analyzer.engine.LogCall;
import log_analyzer.policy.entity.ForbiddenFieldRule;
import log_analyzer.report.Violation;

// 추출한 log에 대해서 위반사항을 검증하는 클래스
public class ForbiddenFieldLoggingRule {
	public static final String RULE_NAME = "FORBIDDEN_FIELD_LOGGING";


    //
    private final List<ForbiddenFieldRule> forbiddenFields;
    private final LogArgInspector inspector = new LogArgInspector();

    //규정의 내용을 ForbiddenFieldRule 형식으로 저장 name과 match 변수명 사용함.
    public ForbiddenFieldLoggingRule(List<ForbiddenFieldRule> forbiddenFields) {
        this.forbiddenFields = forbiddenFields;
    }

    // 감지된 log에 대한 위반 사항을 확인하는 메소드
    // call -> 감지된 log에 대한 LogCall객체
    public List<Violation> evaluate(LogCall call) {
    	List<Violation> violations = new ArrayList<>();

        List<String> allTokens = new ArrayList<>();
        //LogCall에 저장된 log사용 코드를 잘게 쪼게어 사용(변수명, 텍스트 등)
        call.getArgs().forEach(arg -> allTokens.addAll(inspector.extractTokens(arg)));

        Set<String> dedup = new HashSet<>();

        // 감지된 log에 대해 위반 사항 확인
        // log에 작성된 내용을 실제로 읽는 영역
        for (String token : allTokens) {
            // 규정을 하나씩 대입
            for (ForbiddenFieldRule rule : forbiddenFields) {
                //실제 규칙 적용하여 위반사항 확인(포함, 정확 일치 등)
                if (rule.matches(token)) {

                    //key를 통해 동일한 로그가 중복되어 작성되었는지 검사
                    String key = call.getFile() + ":" +
                                 call.getLine() + ":" +
                                 RULE_NAME + ":" +
                                 rule.getName() + ":" +
                                 call.getMethodFqn();

                    if (!dedup.add(key)) continue;

                    //파일 위치, 라인, 위반한 규정, 실제 작성된 코드를 사용하여 새로운 Violation객체 생성
                    //생성한 객체를 반환하기 위해 Violation 리스트 객체에 추가
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
        //String 가변 객체 생성
        StringBuilder sb = new StringBuilder();

        //실제로 사용된 코드 내용 기입 후 반환 ex) logger.info("Logging Text")
        sb.append(call.getMethodFqn()).append("(");
        for (int i = 0; i < call.getArgs().size(); i++) {
            sb.append(call.getArgs().get(i));
            if (i < call.getArgs().size() - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
	
}
