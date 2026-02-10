package log_analyzer.engine;

import com.github.javaparser.ast.expr.Expression;
import java.nio.file.Path;
import java.util.List;

//실제 사용된 log에 대한 정보를 저장하는 클래스
public class LogCall {
    private final Path file;
    private final int line;
    private final String methodFqn; // ex) log.info
    private final List<Expression> args;

    public LogCall(Path file, int line, String methodFqn, List<Expression> args) {
        this.file = file;
        this.line = line;
        this.methodFqn = methodFqn;
        this.args = args;
        }

    public Path getFile() { return file; }
    public int getLine() { return line; }
    public String getMethodFqn() { return methodFqn; }
    public List<Expression> getArgs() { return args; }

}
