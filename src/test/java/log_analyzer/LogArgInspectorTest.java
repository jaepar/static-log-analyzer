package log_analyzer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;

import log_analyzer.engine.LogArgInspector;

class LogArgInspectorTest {

    private final LogArgInspector inspector = new LogArgInspector();

    private Expression expr(String code) {
        return StaticJavaParser.parseExpression(code);
    }

    @Test
    @DisplayName("null 입력이면 빈 리스트를 반환한다")
    void extractTokens_null_returnsEmpty() {
        List<String> tokens = inspector.extractTokens(null);
        assertNotNull(tokens);
        assertTrue(tokens.isEmpty());
    }

    @Test
    @DisplayName("문자열 리터럴이면 문자열 값만 토큰으로 뽑는다")
    void extractTokens_stringLiteral() {
        List<String> tokens = inspector.extractTokens(expr("\"hello\""));
        assertEquals(List.of("hello"), tokens);
    }

    @Test
    @DisplayName("텍스트 블록 리터럴이면 내용(value)을 토큰으로 뽑는다")
    void extractTokens_textBlockLiteral_direct() {
        var tb = new TextBlockLiteralExpr("line1\nline2");
        List<String> tokens = inspector.extractTokens(tb);
        assertEquals(List.of("line1\nline2"), tokens);
    }

    @Test
    @DisplayName("NameExpr이면 변수명만 토큰으로 뽑는다")
    void extractTokens_nameExpr() {
        List<String> tokens = inspector.extractTokens(expr("password"));
        assertEquals(List.of("password"), tokens);
    }

    @Test
    @DisplayName("FieldAccessExpr이면 필드명 + 스코프를 재귀적으로 토큰화한다")
    void extractTokens_fieldAccessExpr() {
        // user.password -> ["password", "user"] (현재 구현은 필드명 먼저, 스코프 나중)
        List<String> tokens = inspector.extractTokens(expr("user.password"));
        assertEquals(List.of("password", "user"), tokens);
    }

    @Test
    @DisplayName("BinaryExpr '+' 문자열 결합이면 좌/우를 재귀적으로 토큰화한다")
    void extractTokens_binaryPlusConcat() {
        // "pw=" + password -> ["pw=", "password"]
        List<String> tokens = inspector.extractTokens(expr("\"pw=\" + password"));
        assertEquals(List.of("pw=", "password"), tokens);
    }

    @Test
    @DisplayName("BinaryExpr가 '+'가 아니면 하위 토큰을 더 뽑지 않는다(현재 구현 기준)")
    void extractTokens_binaryNotPlus() {
        // password == "x" -> BinaryExpr 이지만 PLUS가 아니라서 tokens는 비어야 함
        List<String> tokens = inspector.extractTokens(expr("password == \"x\""));
        assertTrue(tokens.isEmpty());
    }

    @Test
    @DisplayName("MethodCallExpr이면 메서드명 + 인자 + 스코프를 재귀적으로 토큰화한다")
    void extractTokens_methodCallExpr() {
        // user.getPassword("x") -> ["getPassword", "x", "user"]
        List<String> tokens = inspector.extractTokens(expr("user.getPassword(\"x\")"));
        assertEquals(List.of("getPassword", "x", "user"), tokens);
    }

    @Test
    @DisplayName("ObjectCreationExpr이면 타입명 + 생성자 인자를 재귀적으로 토큰화한다")
    void extractTokens_objectCreationExpr() {
        // new StringBuilder(password) -> ["StringBuilder", "password"]
        List<String> tokens = inspector.extractTokens(expr("new StringBuilder(password)"));
        assertEquals(List.of("StringBuilder", "password"), tokens);
    }

    @Test
    @DisplayName("EnclosedExpr이면 내부 표현식을 재귀적으로 토큰화한다")
    void extractTokens_enclosedExpr() {
        // (user.password) -> ["password","user"]
        List<String> tokens = inspector.extractTokens(expr("(user.password)"));
        assertEquals(List.of("password", "user"), tokens);
    }

    @Test
    @DisplayName("지원하지 않는 Expression은 fallback으로 expr.toString()을 토큰으로 넣는다")
    void extractTokens_fallback() {
        // ArrayAccessExpr 같은 케이스는 위 분기들에 안 걸릴 수 있음 -> fallback
        List<String> tokens = inspector.extractTokens(expr("arr[0]"));
        assertEquals(List.of("arr[0]"), tokens);
    }
}