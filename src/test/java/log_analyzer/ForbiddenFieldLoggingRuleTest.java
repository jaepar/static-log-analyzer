package log_analyzer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import log_analyzer.exception.ParserException;
import log_analyzer.policy.entity.ForbiddenFieldRule;
import log_analyzer.policy.entity.MatchType;
import log_analyzer.policy.service.LoggingPolicy;
import log_analyzer.policy.service.PolicyLoader;

class ForbiddenFieldLoggingRuleTest {
    @Test
    @DisplayName("유효한 YAML 파일을 로드하면 LoggingPolicy 객체가 정상적으로 생성된다.")
    void testLoad_ValidYamlFile_ReturnsLoggingPolicy(@TempDir Path tempDir) throws IOException {
        // Given
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

        // When
        LoggingPolicy policy = loader.load(policyFile);

        // Then
        assertNotNull(policy);
        assertEquals(2, policy.getForbiddenFields().size());
        assertEquals(2, policy.getLogMethods().size());
    }
}
