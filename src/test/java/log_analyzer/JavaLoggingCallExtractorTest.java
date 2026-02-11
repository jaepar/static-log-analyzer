package log_analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays; // Paths 추가
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test; // Nested 임포트 필수
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import log_analyzer.engine.JavaLoggingCallExtractor;
import log_analyzer.engine.LogCall;

class JavaLoggingCallExtractorTest {

    private JavaLoggingCallExtractor javaLoggingCallExtractor = new JavaLoggingCallExtractor();

    private Set<String> allowedLogMethods = new HashSet<>(Arrays.asList("log.info","log.error"));

    @TempDir
    Path tempDir;

    private Path createJavaFile(String fileName, String content) {
        try {
            Path file = tempDir.resolve(fileName);
            Files.writeString(file, content);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e); // 테스트 중 에러나면 바로 멈추게
        }
    }
    
    @Nested
    @DisplayName("extractTest Class")
    class extractTest {
        // extract 테스트 케이스 묶음
    	// log.info를 사용한 내용이 있으면 해당 내용을 List<LogCall>형태로 반환한다.
        // file의 경로가 java파일 위치가 아니라면 빈 배열을 반환한다.
        // 만약 file의 내용이 비었을 경우 빈 배열을 반환한다. → 해당 검증은 외부 라이브러리가 담당
        // fqn == null 일 때 반복문 종료
        // fqn이 allowedLogMethods에 포함되지 않는 경우 반복문 종료

        // 임시 Directory 생성

        @Test
        @DisplayName("log.info를 사용한 내용이 있으면 해당 내용을 List<LogCall>형태로 반환한다.")
        void Add_Log_to_List() {
        	String source = """
    			class Test{
	                private static final Logger log =
	        			Logger.getLogger(LoginService.class.getName());
	        			
	        		public void login(String password, String token) {
	            			log.info("password" + password); // 6번째 줄
		        	        
	        		}
    			}
                """;
            Path file = createJavaFile("Test.java", source);
            
            String args = "\"password\" + password";
            Expression expectedArg = StaticJavaParser.parseExpression(args);
            
            // 예상값 세팅
            List<LogCall> expect = new ArrayList<LogCall>(Arrays.asList(
            			new LogCall(file, 6, "log.info", List.of(expectedArg))
            		));
            
            List<LogCall> actual = javaLoggingCallExtractor.extract(file, allowedLogMethods);

            assertAll(
        		() -> assertEquals(expect.get(0).getFile(), actual.get(0).getFile()),
        		() -> assertEquals(expect.get(0).getLine(), actual.get(0).getLine()),
        		() -> assertEquals(expect.get(0).getMethodFqn(), actual.get(0).getMethodFqn()),
        		() -> assertEquals(expect.get(0).getArgs(), actual.get(0).getArgs())
    		);
        }

        @Test
        @DisplayName("file의 경로가 java파일 위치가 아니라면 빈 배열을 반환한다.")
        void Not_Java_filePath_Test() {

            Path file = Paths.get("src/main/resources/logging-policy.yml");

            List<LogCall> expect = new ArrayList<>();
            List<LogCall> actual = javaLoggingCallExtractor.extract(file, allowedLogMethods);
            
            assertEquals(expect, actual);
        }

        @Test
        @DisplayName("만약 file의 내용이 비었을 경우 빈 배열을 반환한다.")
        void Null_Java_file_Test() {

            String source = """
                class Test {}
                """;
            Path file = createJavaFile("Test.java", source);

            List<LogCall> expect = new ArrayList<>();
            List<LogCall> actual = javaLoggingCallExtractor.extract(file, allowedLogMethods);

            assertEquals(expect, actual);
        }
        
        @Test
        @DisplayName("일반 함수 호출(fqn==null)은 무시하고, 뒤에 있는 log.info는 정상적으로 추출한다")
        void skip_method_call_without_scope() {
        	String source = """
		        class Test {
        			private static final Logger log =
        				Logger.getLogger(LoginService.class.getName());
		            void test() {
		                int a = 2;
		                String password = "1234";
		                //log.info(password);
		                log.info("ok");
		            }
		        }
		        """;
        	Path file = createJavaFile("Test.java", source);
        	
            List<LogCall> actual = javaLoggingCallExtractor.extract(file, allowedLogMethods);

            assertAll(
        		() -> assertEquals(file, actual.get(0).getFile()),
        		() -> assertEquals(8, actual.get(0).getLine()),
        		() -> assertEquals("log.info", actual.get(0).getMethodFqn()),
        		() -> assertEquals(List.of(StaticJavaParser.parseExpression("\"ok\"")), actual.get(0).getArgs())
    		);
        }
        
        @Test
        @DisplayName("java 파일 자체에 에러가 존재할 경우 빈 배열을 반환한다.")
        void skip_error_java() {
        	String source = """
		        class Test {
		            void test() {
        			    if(1+1 = 2){ //문법 오류 '}'
        			    
		                log.info("ok");
		            }
		        }
		        """;
        	Path file = createJavaFile("Test.java", source);
        	
            List<LogCall> actual = javaLoggingCallExtractor.extract(file, allowedLogMethods);
            List<LogCall> expect = new ArrayList<>();
            
    		assertEquals(expect, actual);
        }
        
    }
    
    @Nested
    @DisplayName("toMethodFqnTest Class")
    class toMethodFqnTest {
    // toMethodFqn 테스트 케이스 묶음
        // ‘log.-’이 입력되었을 때 동일한 형태로 반환 된다.
        // ‘Logger.-’, ‘logger.-’, ‘Log.-’, 이 입력되었을 때 ‘log.-’로 변환된다.
        // ‘Logger.-’, ‘logger.-’, ‘Log.-’, ‘log’가 아닌 값이 입력된다면 동일한 형태로 반환 된다.
    	@Test
    	@DisplayName("‘log.-’이 입력되었을 때 동일한 형태로 반환 된다.")
    	void Input_Log() {
    		String source = """
    				class Test{
	                    private static final Logger log =
	            			Logger.getLogger(LoginService.class.getName());
	
	            		public void login(String password, String token) {
	                			log.info("password" + password);
	    	        	        
	            		}
            		}
                    """;
    		
    		Path file = createJavaFile("Test.java", source);
    		
    		List<LogCall> actual = javaLoggingCallExtractor.extract(file ,allowedLogMethods);
    		
    		assertEquals("log.info", actual.get(0).getMethodFqn());
    	}
    	
    	@Test
    	@DisplayName("‘Logger.-’, ‘logger.-’, ‘Log.-’, 이 입력되었을 때 ‘log.-’로 변환된다.")
    	void chang_Logger() {
    		String source = """
    				class Test{
	                    private static final Logger Logger =
	            			Logger.getLogger(LoginService.class.getName());
	
	    				private static final Logger logger =
	            			Logger.getLogger(LoginService.class.getName());
	            			
	        			private static final Logger Log =
	            			Logger.getLogger(LoginService.class.getName());
	        			
	            		public void login(String password, String token) {
	                			Logger.info("Logger");
	                			logger.info("logger");
	                			Log.info("Log");
	            		}
            		}
                    """;
    		
    		Path file = createJavaFile("Test.java", source);
    		
    		List<LogCall> actual = javaLoggingCallExtractor.extract(file ,allowedLogMethods);
    		
    		assertAll(
    				() -> assertEquals("log.info", actual.get(0).getMethodFqn()),
    				() -> assertEquals("log.info", actual.get(1).getMethodFqn()),
    				() -> assertEquals("log.info", actual.get(2).getMethodFqn())
				);
    	}
    	
    	@Test
    	@DisplayName("Scope가 존재하지 않는 다면 null을 반환")
    	void Not_Scope() {
    		String source = """
    				class Test{
	                    private static final Logger Logger =
	            			Logger.getLogger(LoginService.class.getName());
	        			
	            		public void login() {
	                			info("Logger");
	                			log();
	            		}
            		}
                    """;
    		
    		Path file = createJavaFile("Test.java", source);
    		
    		List<LogCall> expect = new ArrayList<>();
    		List<LogCall> actual = javaLoggingCallExtractor.extract(file ,allowedLogMethods);
    		
    		// toMethod의 결과가 null인 경우 값을 추가하지 않기 때문에 결과적으로 빈 배열을 반환하게 된다.
			assertEquals(expect ,actual);
    	}
	}

    @Nested
    @DisplayName("lastToken Class")
    class lastToken{
    	// lastToken 테스트 케이스 묶음
    	// ‘.’이 한 개 포함된 텍스트의 경우 ‘.’ 이후 문자열을 반환한다.
    	// ‘.’이 없는 텍스트가 입력되었을 경우 그냥 반환한다.
    	// ‘.’이 두 개 이상 포함된 텍스트의 경우 마지막 ‘.’이후 문자열을 반환한다.
    	@Test
    	@DisplayName("‘.’이 한 개 포함된 텍스트의 경우 ‘.’ 이후 문자열을 반환한다.")
    	void One_Dot() {
    		String source = """
    				class Test{
	                    private static final Logger log =
	            			Logger.getLogger(LoginService.class.getName());
	        			
	            		public void login() {
	    					log.info("test");
	            		}
            		}
                    """;
    		
    		Path file = createJavaFile("Test.java", source);
 
    		List<LogCall> actual = javaLoggingCallExtractor.extract(file ,allowedLogMethods);
    		
			assertEquals("log.info" ,actual.get(0).getMethodFqn());
    	}
    	
    	@Test
    	@DisplayName("‘.’이 한 개 포함된 텍스트의 경우 ‘.’ 이후 문자열을 반환한다.")
    	void Two_Dot() {
    		String source = """
    				class Test{    			
	            		public void login() {
	    					Logger.log.info("test");
	            		}
            		}
                    """;
    		
    		Path file = createJavaFile("Test.java", source);
 
    		List<LogCall> actual = javaLoggingCallExtractor.extract(file ,allowedLogMethods);
    		
			assertEquals("log.info", actual.get(0).getMethodFqn());
    	}
    }
}
