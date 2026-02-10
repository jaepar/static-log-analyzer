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
import org.junit.jupiter.params.provider.ValueSource;

import log_analyzer.exception.ParserException;
import log_analyzer.policy.entity.ForbiddenFieldRule;
import log_analyzer.policy.entity.MatchType;
import log_analyzer.policy.service.LoggingPolicy;
import log_analyzer.policy.service.PolicyLoader;

class PolicyLoaderTest {
	// load
    @Test
    @DisplayName("유효한 YAML 파일을 로드하면 LoggingPolicy 객체가 정상적으로 생성된다")
    void test1(@TempDir Path tempDir) throws IOException {
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
    
    @Test
    @DisplayName("forbiddenFields가 리스트가 아닌 경우 ParserException - forbiddenFields must be a list 에러가 발생한다")
    void test2(@TempDir Path tempDir) throws IOException {
        String yamlContent = """
            forbiddenFields: password
            logMethods:
              - log.info
              - log.error
            """;
        Path policyFile = tempDir.resolve("policy.yml");
        Files.writeString(policyFile, yamlContent);
        PolicyLoader loader = new PolicyLoader();

        ParserException exception = assertThrows(ParserException.class, () -> {
            loader.load(policyFile);
        });
        assertTrue(exception.getMessage().contains("forbiddenFields must be a list"));
    }
    
    @Test
    @DisplayName("forbiddenFields의 match 필드가 없으면 기본값은 exact로 실행된다")
    void test3(@TempDir Path tempDir) throws IOException {
        String yamlContent = """
            forbiddenFields:
              - name: password
            """;
        Path policyFile = tempDir.resolve("policy.yml");
        Files.writeString(policyFile, yamlContent);
        PolicyLoader loader = new PolicyLoader();

        // When
        LoggingPolicy policy = loader.load(policyFile);

        // Then
        assertNotNull(policy);
        assertEquals(1, policy.getForbiddenFields().size());
        assertEquals(MatchType.EXACT, policy.getForbiddenFields().get(0).getMatch());
    }
    
    @Test
    @DisplayName("root가 Map이 아닌 경우 ParserException - root is not a map에러가 발생한다")
    void test4(@TempDir Path tempDir) throws IOException {
        String yamlContent = """
                - item1
                - item2
                """;
            Path policyFile = tempDir.resolve("policy.yml");
            Files.writeString(policyFile, yamlContent);
            PolicyLoader loader = new PolicyLoader();

            ParserException exception = assertThrows(ParserException.class, () -> {
                loader.load(policyFile);
            });
            assertTrue(exception.getMessage().contains("root is not a map"));
        }
    
    // validateFileExtension
    @Test
    @ParameterizedTest
    @ValueSource(strings = {"policy.yml", "policy.yaml"})
    @DisplayName("확장자가 yml 또는 yaml이면 예외가 발생하지 않는다")
    void test5(String fileName) {
        PolicyLoader loader = new PolicyLoader();
        assertDoesNotThrow(() -> loader.validateFileExtension(fileName));
    }

    @Test
    @ParameterizedTest
    @ValueSource(strings = {"policy.txt", "policy.json", "policy.xml"})
    @DisplayName("확장자가 yml/yaml이 아니면 ParserException - 유효하지 않은 파일 확장자")
    void test6(String fileName) {
        PolicyLoader loader = new PolicyLoader();
        ParserException e = assertThrows(ParserException.class,
            () -> loader.validateFileExtension(fileName));

        assertTrue(e.getMessage().contains("유효하지 않은 파일 확장자"));
    }

}