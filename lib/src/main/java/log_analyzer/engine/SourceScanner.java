package log_analyzer.engine;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class SourceScanner {

    public List<Path> findJavaFiles(Path root) {
        List<Path> files = new ArrayList<>();

        try {
            Files.walk(root)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    // 필요 시 빌드 산출물 제외:
                    // .filter(p -> !p.toString().contains("/build/"))
                    // .filter(p -> !p.toString().contains("/out/"))
                    .forEach(files::add);
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan source files: " + root, e);
        }

        return files;
    }
}