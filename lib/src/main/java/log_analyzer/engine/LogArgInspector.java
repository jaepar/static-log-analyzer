package log_analyzer.engine;

import com.github.javaparser.ast.expr.*;

import java.util.ArrayList;
import java.util.List;

public class LogArgInspector {
	/**
     * 로그 인자 표현식(Expression)에서 검사 가능한 토큰을 뽑는다.
     * - 문자열 리터럴
     * - 변수명(NameExpr)
     * - 필드명(FieldAccessExpr)
     * - 문자열 결합(BinaryExpr '+')
     * - 메서드 호출 이름(getPassword 등)
     */
    public List<String> extractTokens(Expression expr) {
        List<String> tokens = new ArrayList<>();
        collect(expr, tokens);
        return tokens;
    }

    private void collect(Expression expr, List<String> out) {
        if (expr == null) return;

        if (expr.isStringLiteralExpr()) {
            out.add(expr.asStringLiteralExpr().asString());
            return;
        }

        if (expr.isTextBlockLiteralExpr()) {
            out.add(expr.asTextBlockLiteralExpr().getValue());
            return;
        }

        if (expr.isNameExpr()) {
            out.add(expr.asNameExpr().getNameAsString());
            return;
        }

        if (expr.isFieldAccessExpr()) {
            FieldAccessExpr fa = expr.asFieldAccessExpr();
            out.add(fa.getNameAsString());      // password
            collect(fa.getScope(), out);        // user
            return;
        }

        if (expr.isBinaryExpr()) {
            BinaryExpr be = expr.asBinaryExpr();
            if (be.getOperator() == BinaryExpr.Operator.PLUS) {
                collect(be.getLeft(), out);
                collect(be.getRight(), out);
            }
            return;
        }

        if (expr.isMethodCallExpr()) {
            MethodCallExpr mc = expr.asMethodCallExpr();
            out.add(mc.getNameAsString());      // getPassword
            mc.getArguments().forEach(a -> collect(a, out));
            mc.getScope().ifPresent(s -> collect(s, out));
            return;
        }

        if (expr.isObjectCreationExpr()) {
            ObjectCreationExpr oc = expr.asObjectCreationExpr();
            out.add(oc.getTypeAsString());      // StringBuilder 등
            oc.getArguments().forEach(a -> collect(a, out));
            return;
        }

        if (expr.isEnclosedExpr()) {
            collect(expr.asEnclosedExpr().getInner(), out);
            return;
        }

        // fallback
        out.add(expr.toString());
    }

}
