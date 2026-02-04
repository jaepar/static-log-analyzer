package log_analyzer.report;

import java.nio.file.Path;

public class Violation {
    private final Path file;
    private final int line;
    private final String rule;
    private final String message;
    private final String code;

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
