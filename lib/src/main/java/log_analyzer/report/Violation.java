package log_analyzer.report;

import java.nio.file.Path;

// 위반 사항 저장 클래스
public class Violation {
    //파일 경로
    private final Path file;
    //파일의 줄 수
    private final int line;
    //규칙
    private final String rule;
    //로그 메시지
    private final String message;
    //실제 코드
    private final String code;

    //생성자
    public Violation(Path file, int line, String rule, String message, String code) {
        this.file = file;
        this.line = line;
        this.rule = rule;
        this.message = message;
        this.code = code;
    }

    public Path getFile() { return file; }
    public int getLine() { return line; }
    public String getRule() { return rule; }
    public String getMessage() { return message; }
    public String getCode() { return code; }
}
