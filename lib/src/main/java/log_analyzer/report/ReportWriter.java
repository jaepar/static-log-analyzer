package log_analyzer.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class ReportWriter {

    public void write(Path reportPath, List<Violation> violations) {
        try {
            if (reportPath.getParent() != null) {
                Files.createDirectories(reportPath.getParent());
            }

            StringBuilder sb = new StringBuilder();

            if (violations.isEmpty()) {
                sb.append("[OK] Logging policy check passed. No violations detected.\n");
            } else {
                for (Violation v : violations) {
                    sb.append("[LOGGING VIOLATION] Logging policy violation detected\n");
                    sb.append("File: ").append(v.getFile()).append("\n");
                    sb.append("Line: ").append(v.getLine()).append("\n");
                    sb.append("Rule: ").append(v.getRule()).append("\n");
                    sb.append("Message: ").append(v.getMessage()).append("\n");
                    sb.append("Code: ").append(v.getCode()).append("\n");
                    sb.append("\n");
                }
            }

            Files.writeString(
                    reportPath,
                    sb.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report: " + reportPath, e);
        }
    }
}
