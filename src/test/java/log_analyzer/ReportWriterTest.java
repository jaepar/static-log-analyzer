package log_analyzer;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import log_analyzer.report.ReportWriter;
import log_analyzer.report.Violation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class ReportWriterTest {

    @TempDir
    Path tempDir; // 테스트 후 자동으로 삭제되는 임시 디렉토리

    @Test
    @DisplayName("위반 사항이 없을 때 OK 메시지가 포함된 파일이 생성되어야 한다")
    void testWrite_NoViolations() throws IOException {
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

    @Test
    @DisplayName("위반 사항이 있을 때 상세 정보가 올바른 포맷으로 기록되어야 한다")
    void testWrite_WithViolations() throws IOException {
        // Given
        Path reportPath = tempDir.resolve("violation_report.txt");
        Violation v = new Violation(
            Path.of("LoginService.java"), 15, "ForbiddenField", "Found 'password'", "log.info(password)"
        );
        ReportWriter writer = new ReportWriter();

        // When
        writer.write(reportPath, List.of(v));

        // Then
        String content = Files.readString(reportPath);
        assertTrue(content.contains("[LOGGING VIOLATION]"));
        assertTrue(content.contains("File: LoginService.java"));
        assertTrue(content.contains("Line: 15"));
        assertTrue(content.contains("Message: Found 'password'"));
    }
}