package log_analyzer.engine;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;

import java.nio.file.Path;
import java.util.*;

public class JavaLoggingCallExtractor {

    private final ParserConfiguration config = new ParserConfiguration();

    public List<LogCall> extract(Path file, Set<String> allowedLogMethods) {
        try {
            JavaParser parser = new JavaParser(config);
            ParseResult<CompilationUnit> result = parser.parse(file);

            if (result.getResult().isEmpty()) return List.of();
            CompilationUnit cu = result.getResult().get();

            List<LogCall> calls = new ArrayList<>();

            cu.findAll(MethodCallExpr.class).forEach(call -> {
                String fqn = toMethodFqn(call);
                if (fqn == null) return;

                if (!allowedLogMethods.contains(fqn)) return;

                int line = call.getBegin().map(p -> p.line).orElse(-1);
                calls.add(new LogCall(file, line, fqn, call.getArguments()));
            });

            return calls;
        } catch (Exception e) {
            // 파일 파싱 실패가 전체 분석을 죽이지 않도록 파일 단위로 무시
            return List.of();
        }
    }

    private String toMethodFqn(MethodCallExpr call) {
        String method = call.getNameAsString();

        if (call.getScope().isPresent()) {
            String scope = call.getScope().get().toString();

            // scope가 "this.log" / "some.logger" 처럼 될 수 있으니 마지막 토큰만 추출
            String normalizedScope = lastToken(scope);

            // java.util.logging.Logger.* 를 약식으로 지원
            if (normalizedScope.endsWith("Logger")) {
                return "Logger." + method;
            }

            return normalizedScope + "." + method;
        }

        // scope 없는 호출은 정책 매칭하기 어려워 제외
        return null;
    }

    private String lastToken(String scope) {
        int idx = scope.lastIndexOf('.');
        return (idx >= 0) ? scope.substring(idx + 1) : scope;
    }
}