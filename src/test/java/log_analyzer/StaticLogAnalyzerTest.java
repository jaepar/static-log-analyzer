package log_analyzer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.javaparser.ast.expr.NameExpr;

import log_analyzer.engine.JavaLoggingCallExtractor;
import log_analyzer.engine.LogCall;
import log_analyzer.engine.SourceScanner;
import log_analyzer.policy.entity.ForbiddenFieldRule;
import log_analyzer.policy.entity.MatchType;
import log_analyzer.policy.service.LoggingPolicy;
import log_analyzer.report.ReportWriter;
import log_analyzer.report.Violation;

@ExtendWith(MockitoExtension.class)
public class StaticLogAnalyzerTest {

    @Mock
    private SourceScanner scanner;

    @Mock
    private JavaLoggingCallExtractor extractor;

    @Mock
    private ReportWriter reportWriter;

    private LoggingPolicy policy() {
        return new LoggingPolicy(
            List.of(new ForbiddenFieldRule("password", MatchType.CONTAINS)),
            List.of("log.info")
        );
    }

    @Test
    @DisplayName("Java 파일이 없으면 위반 사항은 없다")
    void analyze_noJavaFiles() {
        // given
        StaticLogAnalyzer analyzer = new StaticLogAnalyzer(scanner, extractor, reportWriter);
        given(scanner.findJavaFiles(any(Path.class))).willReturn(List.of());

        // when
        List<Violation> violations = analyzer.analyze(Path.of("."), policy());

        // then
        assertTrue(violations.isEmpty());
        then(extractor).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("로그 호출은 있으나 forbidden field가 없으면 위반은 없다")
    void analyze_noForbiddenField() {
        // given
        StaticLogAnalyzer analyzer = new StaticLogAnalyzer(scanner, extractor, reportWriter);

        Path root = Path.of(".");
        Path file = Path.of("Test.java");

        given(scanner.findJavaFiles(any(Path.class))).willReturn(List.of(file));

        LogCall call = new LogCall(file, 10, "log.info", List.of(new NameExpr("username")));

        // 일관성 유지로 인해 anySet()을 써서 eq(file)을 사용
        given(extractor.extract(eq(file), anySet())).willReturn(List.of(call));

        // when
        List<Violation> violations = analyzer.analyze(root, policy());

        // then
        assertTrue(violations.isEmpty());
        then(scanner).should().findJavaFiles(any(Path.class));
        then(extractor).should().extract(eq(file), anySet());
    }

    @Test
    @DisplayName("forbidden field가 로그 인자에 포함되면 위반 1건을 반환한다")
    void analyze_hasForbiddenField() {
        // given
        StaticLogAnalyzer analyzer = new StaticLogAnalyzer(scanner, extractor, reportWriter);

        Path root = Path.of(".");
        Path file = Path.of("A.java");

        given(scanner.findJavaFiles(any(Path.class))).willReturn(List.of(file));

        LogCall call = new LogCall(file, 10, "log.info", List.of(new NameExpr("password")));

        given(extractor.extract(eq(file), anySet())).willReturn(List.of(call));

        // when
        List<Violation> violations = analyzer.analyze(root, policy());

        // then
        assertEquals(1, violations.size());
        assertEquals(file, violations.get(0).getFile());
        assertEquals(10, violations.get(0).getLine());
    }

    @Test
    @DisplayName("여러 파일에서 발생한 위반을 모두 수집한다")
    void analyze_multipleFiles() {
        // given
        StaticLogAnalyzer analyzer = new StaticLogAnalyzer(scanner, extractor, reportWriter);

        Path root = Path.of(".");
        Path file1 = Path.of("A.java");
        Path file2 = Path.of("B.java");

        given(scanner.findJavaFiles(any(Path.class))).willReturn(List.of(file1, file2));

        LogCall call1 = new LogCall(file1, 10, "log.info", List.of(new NameExpr("password")));
        LogCall call2 = new LogCall(file2, 20, "log.info", List.of(new NameExpr("password")));

        given(extractor.extract(eq(file1), anySet())).willReturn(List.of(call1));
        given(extractor.extract(eq(file2), anySet())).willReturn(List.of(call2));

        // when
        List<Violation> violations = analyzer.analyze(root, policy());

        // then
        assertEquals(2, violations.size());
        then(scanner).should().findJavaFiles(any(Path.class));
        then(extractor).should(times(2)).extract(any(Path.class), anySet());
    }

    @Test
    @DisplayName("writeReport는 ReportWriter에게 그대로 위임한다")
    void writeReport_delegates() {
        // given
        StaticLogAnalyzer analyzer = new StaticLogAnalyzer(scanner, extractor, reportWriter);
        Path reportPath = Path.of("build/report.txt");
        List<Violation> violations = List.of();

        // when
        analyzer.writeReport(reportPath, violations);

        // then
        then(reportWriter).should().write(reportPath, violations);
    }
    
    @Test
    @DisplayName("기본 생성자는 내부 의존성을 생성해서 초기화된다")
    void defaultConstructor_covered() {
        // when
        StaticLogAnalyzer analyzer = new StaticLogAnalyzer();

        // then
        assertNotNull(analyzer);
    }
}