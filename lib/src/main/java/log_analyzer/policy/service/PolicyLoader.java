package log_analyzer.policy.service;

import log_analyzer.exception.ParserException;
import log_analyzer.policy.entity.ForbiddenFieldRule;
import log_analyzer.policy.entity.MatchType;
import org.yaml.snakeyaml.Yaml;


import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// 규제 내용을 읽고 반환하는 클래스
public class PolicyLoader {

    //파일 읽기 메소드
    public LoggingPolicy load(Path policyPath) {
        // 확장자 검증
        validateFileExtension(policyPath.toString());

        // 파일 읽기 시작
        // Yml 파일 java 객체로 파싱
        try (InputStream is = Files.newInputStream(policyPath)) {
            Yaml yaml = new Yaml();
            Object obj = yaml.load(is);

            if (!(obj instanceof Map<?, ?> root)) {
                throw new ParserException("Invalid policy format (root is not a map).");
            }

            //yml내부의 규정 내역 리스트화
            List<ForbiddenFieldRule> forbidden = parseForbiddenFields(root.get("forbiddenFields"));
            //yml내부의 log 내역 리스트화
            List<String> logMethods = parseLogMethods(root.get("logMethods"));

            //LoggingPolicy 클래스 생성자 호출
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

    //yml 파일 규정 내용 리스트화
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

            // name, match 저장
            Object nameObj = m.get("name");
            Object matchObj = m.get("match");

            if (nameObj == null) {
                throw new ParserException("forbiddenFields item missing 'name'");
            }

            // 규정 이름과 조건(정확 일치 or 포함) 리스트에 추가
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