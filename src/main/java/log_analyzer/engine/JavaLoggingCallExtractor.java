package log_analyzer.engine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class JavaLoggingCallExtractor {

    // 설정 파싱 라이브러리
    private final ParserConfiguration config = new ParserConfiguration();

    //
    public List<LogCall> extract(Path file, Set<String> allowedLogMethods) {
        try {
            //java 파일을 객층 구조로 변환
            JavaParser parser = new JavaParser(config);
            ParseResult<CompilationUnit> result = parser.parse(file);

            if (result.getResult().isEmpty()) return List.of();
            //java 명령어를 추출하여 저장
            CompilationUnit cu = result.getResult().get();

            List<LogCall> calls = new ArrayList<>();

            //함수 호출 부분만 골라 찾기
            cu.findAll(MethodCallExpr.class).forEach(call -> {
                String fqn = toMethodFqn(call);
                if (fqn == null) return;

                // 규정에 명시되어 있는 함수인지 확인
                if (!allowedLogMethods.contains(fqn)) return;

                // 함수가 실행된 라인을 반환
                int line = call.getBegin().map(p -> p.line).orElse(-1);
                
                // 리스트에 추가
                calls.add(new LogCall(file, line, fqn, call.getArguments()));
            });

            return calls;
        } catch (Exception e) {
            // 파일 파싱 실패가 전체 분석을 죽이지 않도록 파일 단위로 무시
            return List.of();
        }
    }

    //명령어가 log를 출력하는 명령어인지 확인하는 메소드
    private String toMethodFqn(MethodCallExpr call) {
        String method = call.getNameAsString(); //ex info, debug

        if (call.getScope().isPresent()) {
            String scope = call.getScope().get().toString();

            // scope가 "this.log" / "some.logger" 처럼 될 수 있으니 마지막 토큰만 추출
            String normalizedScope = lastToken(scope);

            // java.util.logging.Logger.* 를 약식으로 지원
            // if (normalizedScope.endsWith("Logger")) {
            //     return "Logger." + method;
            // }

            //Logger, logger, Log가 포함되게 작성한 변수명에 대해 log로 고정
            if (normalizedScope.endsWith("Logger") || 
                normalizedScope.endsWith("logger") || 
                normalizedScope.endsWith("Log")) {
                return "log." + method;
            }

            return normalizedScope + "." + method;
        }

        // scope 없는 호출은 정책 매칭하기 어려워 제외
        return null;
    }

    //scope의 마지막 텍스트만을 출력
    private String lastToken(String scope) {
        int idx = scope.lastIndexOf('.');
        return (idx >= 0) ? scope.substring(idx + 1) : scope;
    }
}