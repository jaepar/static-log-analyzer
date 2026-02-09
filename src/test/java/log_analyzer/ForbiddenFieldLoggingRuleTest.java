package log_analyzer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import log_analyzer.policy.entity.ForbiddenFieldRule;
import log_analyzer.policy.entity.MatchType;
import log_analyzer.policy.service.LoggingPolicy;
import log_analyzer.policy.service.PolicyLoader;
import log_analyzer.report.ReportWriter; // 추가
import log_analyzer.report.Violation;    // 추가

class ForbiddenFieldLoggingRuleTest {

    // --- 1. 기존 PolicyLoader 테스트 (유지) ---
    @Test
    @DisplayName("유효한 YAML 파일을 로드하면 LoggingPolicy 객체가 정상적으로 생성된다.")
    void testLoad_ValidYamlFile_ReturnsLoggingPolicy(@TempDir Path tempDir) throws IOException {
        String yamlContent = """
            forbiddenFields:
              - name: password
                match: exact
              - name: token
                match: contains
            logMethods:
              - log.info
              - log.error
            """;
        Path policyFile = tempDir.resolve("policy.yml");
        Files.writeString(policyFile, yamlContent);
        PolicyLoader loader = new PolicyLoader();

        LoggingPolicy policy = loader.load(policyFile);

        assertNotNull(policy);
        assertEquals(2, policy.getForbiddenFields().size());
    }

    // --- 2. ForbiddenFieldRule 로직 테스트 (추가) ---
    @ParameterizedTest
    @DisplayName("규칙 매칭 테스트: 설정된 MatchType에 따라 정확히 판별해야 한다.")
    @CsvSource({
        "password, EXACT, password, true",
        "password, EXACT, my_password, false",
        "ssn, CONTAINS, user_ssn_number, true",
        "API_, PREFIX, API_KEY_001, true",
        "_token, SUFFIX, access_token, true"
    })
    void testForbiddenFieldRule_Matches(String name, MatchType type, String input, boolean expected) {
        ForbiddenFieldRule rule = new ForbiddenFieldRule(name, type);
        assertEquals(expected, rule.matches(input));
    }

    // --- 3. ReportWriter 테스트 (추가) ---
    @Test
    @DisplayName("ReportWriter: 위반 사항이 있을 때 파일 내용이 올바르게 생성되어야 한다.")
    void testReportWriter_WithViolations(@TempDir Path tempDir) throws IOException {
        // Given
        Path reportPath = tempDir.resolve("report.txt");
        Violation violation = new Violation(
            Path.of("TestService.java"), 10, "ForbiddenField", "Found 'password'", "log.info(password)"
        );
        ReportWriter writer = new ReportWriter();

        // When
        writer.write(reportPath, List.of(violation));

        // Then
        String content = Files.readString(reportPath);
        assertTrue(content.contains("[LOGGING VIOLATION]"));
        assertTrue(content.contains("File: TestService.java"));
        assertTrue(content.contains("Line: 10"));
    }

    @Test
    @DisplayName("ReportWriter: 위반 사항이 없을 때 [OK] 메시지가 적혀야 한다.")
    void testReportWriter_EmptyViolations(@TempDir Path tempDir) throws IOException {
        // Given
        Path reportPath = tempDir.resolve("ok_report.txt");
        ReportWriter writer = new ReportWriter();

        // When
        writer.write(reportPath, List.of());

        // Then
        String content = Files.readString(reportPath);
        assertTrue(content.contains("[OK]"));
        assertTrue(content.contains("No violations detected"));
    }
}