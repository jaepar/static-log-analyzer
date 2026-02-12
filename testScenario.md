### ForbiddenFieldLoggingRuleTest.java 

✔ matches()

* MatchType이 EXACT일 때, 텍스트가 정확히 일치하면 위반(true)을 반환한다.
* MatchType이 CONTAINS일 때, 텍스트에 키워드가 포함되어 있으면 위반(true)을 반환한다.
* MatchType이 PREFIX/SUFFIX일 때, 시작/끝 단어가 일치하면 위반(true)을 반환한다.
* MatchType이 REGEX일 때, 정규표현식 패턴에 매칭되면 위반(true)을 반환한다.
* 검사할 텍스트가 null인 경우, 에러 없이 정상적으로 false를 반환한다.

---

### JavaLoggingCallExtractorTest.java 

✔ extract()

* log.info를 사용한 내용이 있으면 해당 내용을 List<LogCall>형태로 반환한다.
* file의 경로가 java파일 위치가 아니라면 빈 배열을 반환한다.
* 만약 file의 내용이 비었을 경우 빈 배열을 반환한다.
* 일반 함수 호출(fqn==null)은 무시하고, 뒤에 있는 log.info는 정상적으로 추출한다.
* java 파일 자체에 에러가 존재할 경우 빈 배열을 반환한다.

✔ toMethodFqn()

* ‘log.-’이 입력되었을 때 동일한 형태로 반환된다.
* ‘Logger.-’, ‘logger.-’, ‘Log.-’, 이 입력되었을 때 ‘log.-’로 변환된다.
* Scope가 존재하지 않는 다면 null을 반환한다.

✔ lastToken()

* '.’이 한 개 포함된 텍스트의 경우 ‘.’ 이후 문자열을 반환한다.
* ‘.’이 한 개 이상 포함된 텍스트의 경우 마지막 ‘.’ 이후 문자열을 반환한다.

---

### LogArgInspectorTest.java

✔ extractTokens()

* 입력값이 `null`이면 예외 없이 빈 리스트를 반환한다.
* 문자열 리터럴(`"hello"`)인 경우, 해당 문자열 값만 토큰으로 추출한다.
* 텍스트 블록(`"""..."""`)인 경우, 블록 내부의 내용을 토큰으로 추출한다.
* 변수명(`NameExpr`)인 경우, 변수 이름(예: `password`)을 추출한다.
* 필드 접근(`user.password`)인 경우, 필드명과 객체명을 분리하여 토큰화한다 (예: `["password", "user"]`).
* 문자열 결합(`"pw=" + password`)인 경우, 결합된 각 부분을 재귀적으로 토큰화한다.
* 일반 연산(`password == "x"`)인 경우, 문자열 결합(`+`)이 아니면 하위 토큰을 분해하지 않는다.
* 메서드 호출(`user.getPw("x")`)인 경우, 메서드명, 인자값, 호출 객체명을 모두 토큰화한다.
* 객체 생성(`new String(pw)`)인 경우, 클래스명과 생성자 인자를 토큰화한다.
* 괄호(`(expr)`)로 감싸진 경우, 괄호 내부의 표현식을 재귀적으로 분석한다.
* 배열 접근(`arr[0]`) 등 별도 처리가 없는 표현식은 코드 자체(`toString`)를 토큰으로 저장한다.

---

### PolicyLoaderTest.java 

✔ load()

* 유효한 YAML 파일을 로드하면 LoggingPolicy 객체가 정상 생성된다.
* forbiddenFields가 리스트가 아니면 ParserException("forbiddenFields must be a list") 발생한다.
* forbiddenFields의 match 필드가 없으면 기본값 EXACT로 설정된다.
* YAML root가 Map이 아니면 ParserException("root is not a map")이 발생한다.

✔ validateFileExtension()

* 확장자가 .yml/.yaml이면 예외가 발생하지 않는다.
* 확장자가 yml/yaml이 아니면 ParserException("유효하지 않은 파일 확장자")이 발생한다.

---

### ReportWriterTest.java 

✔ write()

* 위반 사항(Violation)이 존재할 경우, 상세 내역(File, Line, Rule 등)이 포함된 리포트를 생성한다.
* 위반 사항이 없을 경우, [OK] 메시지가 담긴 클린 리포트를 생성한다.
* 리포트 저장 경로의 상위 디렉토리가 없을 경우, 폴더를 자동 생성한 후 파일을 저장한다.
* 파일 쓰기 권한이 없거나 시스템 오류 발생 시, RuntimeException을 발생시킨다.

---

### StaticLogAnalyzerTest.java 

✔ analyze()

* 검사할 Java 파일이 하나도 없으면 위반 사항도 0건이어야 한다.
* 로그 호출(`log.info` 등)이 있어도, 정책(Policy)에서 금지한 필드(예: password)가 포함되지 않았다면 위반 사항은 없다.
* 로그 인자에 금지된 필드가 포함되어 있다면 위반 사항(Violation)이 1건 이상 발생해야 한다.
* 여러 개의 Java 파일에서 각각 위반 사항이 발생하면, 이를 모두 수집하여 리스트로 반환해야 한다.

✔ writeReport()

* 리포트 작성 요청 시, 실제 파일 쓰기 작업을 수행하는 `ReportWriter`에게 올바르게 위임해야 한다.

✔ constructor()

* 기본 생성자로 객체를 생성했을 때, 내부 의존성들이 null이 아닌 상태로 정상 초기화되어야 한다.