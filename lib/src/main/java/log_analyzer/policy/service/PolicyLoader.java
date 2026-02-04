package log_analyzer.policy.service;

import log_analyzer.exception.ParserException;
import log_analyzer.policy.entity.ForbiddenFieldRule;
import log_analyzer.policy.entity.MatchType;
import org.yaml.snakeyaml.Yaml;


import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PolicyLoader {

    public LoggingPolicy load(Path policyPath) {
        validateFileExtension(policyPath.toString());

        try (InputStream is = Files.newInputStream(policyPath)) {
            Yaml yaml = new Yaml();
            Object obj = yaml.load(is);

            if (!(obj instanceof Map<?, ?> root)) {
                throw new ParserException("Invalid policy format (root is not a map).");
            }

            List<ForbiddenFieldRule> forbidden = parseForbiddenFields(root.get("forbiddenFields"));
            List<String> logMethods = parseLogMethods(root.get("logMethods"));

            return new LoggingPolicy(forbidden, logMethods);

        } catch (ParserException e) {
            throw e;
        } catch (Exception e) {
            throw new ParserException("Failed to load policy: " + policyPath, e);
        }
    }

    // ✅ 요구사항: 확장자 검증
    public void validateFileExtension(String filePath) {
        if (filePath == null || !(filePath.endsWith(".yml") || filePath.endsWith(".yaml"))) {
            throw new ParserException("유효하지 않은 파일 확장자입니다: " + filePath);
        }
    }

    private List<ForbiddenFieldRule> parseForbiddenFields(Object raw) {
        if (raw == null) return List.of();
        if (!(raw instanceof List<?> list)) {
            throw new ParserException("forbiddenFields must be a list");
        }

        List<ForbiddenFieldRule> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> m)) {
                throw new ParserException("forbiddenFields item must be a map");
            }

            Object nameObj = m.get("name");
            Object matchObj = m.get("match");

            if (nameObj == null) {
                throw new ParserException("forbiddenFields item missing 'name'");
            }

            String name = String.valueOf(nameObj).trim();
            MatchType match = MatchType.from(matchObj == null ? "exact" : String.valueOf(matchObj));
            result.add(new ForbiddenFieldRule(name, match));
        }
        return result;
    }

    private List<String> parseLogMethods(Object raw) {
        if (raw == null) return List.of();
        if (!(raw instanceof List<?> list)) {
            throw new ParserException("logMethods must be a list");
        }

        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item == null) continue;
            String s = String.valueOf(item).trim();
            if (!s.isEmpty()) result.add(s);
        }
        return result;
    }
}