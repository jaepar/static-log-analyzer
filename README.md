#  [우리FIS 아카데미 6기] Static Log Analyzer 

본 프로젝트는 소스 코드 정적 분석을 통해 로그 파일에 민감한 정보(비밀번호, 인증 토큰 등)가 유출되는 것을 방지하는 보안 도구입니다. 금융 IT 환경에서 발생할 수 있는 데이터 유출 사고를 예방하기 위해 개발했습니다.

---

## 프로젝트 목표

- 로그 코드에 **민감 정보가 포함되는 순간을 빌드 시점에서 차단**
- 단순 문자열 검색이 아닌 **AST(Abstract Syntax Tree) 기반 정적 분석**
- 정책을 코드가 아닌 **외부 설정 파일(YAML)** 로 관리
- **Gradle / CI 환경과 자연스럽게 연동**

---

##  주요 기능

### 1. 보안 정책 커스터마이징
- `logging-policy.yml` 파일을 통해 다음 항목을 정의
  - 로그에 출력되면 안 되는 **금지 필드 목록**
  - 로그 분석 대상이 되는 **로그 메서드 목록**
- match 방식 지원
  - `exact`, `contains`, `prefix`, `suffix`, `regex`
 
### 2. JavaParser 기반 AST 정적 분석
- JavaParser를 활용해 **자바 소스 코드를 AST로 파싱**
- 로그 호출(`log.info`, `logger.error`, `Logger.info` 등)만 정확히 추출
- 문자열 결합, 변수, 필드 접근, 메서드 호출 등 다양한 표현식 분석

### 3. 자동화된 보안 리포트 생성
- 정책 위반 시 상세 정보 출력
  - 파일 경로
  - 코드 라인 번호
  - 위반 규칙
  - 실제 로그 코드 스니펫
- 결과는 `build/logging-report/report.txt`에 저장

### 4. Gradle & CI 통합
- `staticLogAnalyze` Gradle Task 제공
- 위반 발생 시 `exit(1)` → **빌드 실패**
- `gradle check`에 자동 연결 가능

---

##  기술 스택

* **Language**: Java 21
* **Build Tool**: Gradle (Kotlin DSL)
* **Libraries**: 
    * **JavaParser**: 소스 코드 정적 분석 및 구조 파싱
    * **SnakeYAML**: YAML 설정 파일 로딩 및 객체 매핑
    * **JUnit 5**: 기능 검증을 위한 단위 테스트

---

##  프로젝트 구조

```text
log_analyzer
├── engine/                 # AST 기반 로그 추출 엔진
│   ├── SourceScanner.java
│   ├── JavaLoggingCallExtractor.java
│   ├── LogArgInspector.java
│   └── LogCall.java
│
├── policy/                 # 보안 정책 관리 모듈
│   ├── entity/
│   │   ├── ForbiddenFieldRule.java
│   │   └── MatchType.java
│   └── service/
│       ├── LoggingPolicy.java
│       └── PolicyLoader.java
│
├── rules/                  # 정적 분석 규칙
│   └── ForbiddenFieldLoggingRule.java
│
├── report/                 # 분석 결과 리포트
│   ├── Violation.java
│   └── ReportWriter.java
│
├── exception/
│   └── ParserException.java
│
├── StaticLogAnalyzer.java  # 전체 분석 오케스트레이터
└── Main.java               # CLI 실행 엔트리 포인트
```

---
## 설정 및 사용 방법
### 1. 정책 파일 설정 (logging-policy.yml)
프로젝트 루트 폴더에 위치하며, 보안 검사를 수행할 필드명과 로그 메서드를 Map 형태로 정의합니다.

YAML
```
forbiddenFields:
  - name: password
    match: exact
  - name: token
    match: contains
  - name: secretKey
    match: exact

logMethods:
  - log.info
  - log.error
  - logger.warn
  - Logger.severe
```
### 2. 분석 실행
터미널에서 아래 명령어를 실행하여 정적 분석을 수행합니다.

Bash
```
./gradlew staticLogAnalyze
```

### 3. 결과 보고서 확인
분석이 완료되면 build/logging-report/report.txt 경로에서 상세 위반 내역을 확인할 수 있습니다.

---
## 주요 기여 내용 (Individual Contribution)
보안 정책 로더 및 데이터 모델링 (Policy Module)
정책 로딩 시스템 구축: SnakeYAML 라이브러리를 활용해 텍스트 기반 설정 파일을 자바 객체로 변환하는 PolicyLoader를 구현했습니다

매칭 알고리즘 세분화: 필드명 전체 일치(exact)와 부분 일치(contains)를 구분하는 엔티티 구조를 설계하여 검사의 정밀도를 높였습니다.

예외 처리 및 유효성 검증: 파일 확장자 확인 로직을 추가하고 파싱 실패 시 명확한 에러 메시지를 제공하는 ParserException 체계를 구축했습니다.

빌드 환경 최적화: 윈도우 환경에서 발생할 수 있는 한글 인코딩 문제를 해결하기 위해 Gradle 컴파일 옵션을 UTF-8로 표준화하여 협업 안정성을 확보했습니다.

---
##  테스트 코드 & 테스트 시나리오
정적 분석 정확도와 정책 파싱 안정성 보장을 위해 핵심 모듈에 대해 단위 테스트를 작성했습니다.

### 1. Covered Modules

ForbiddenFieldRule

JavaLoggingCallExtractor

PolicyLoader

ReportWriter

StaticLogAnalyzer

### 2. 검증 항목

MatchType 별 동작 정확성

AST 로그 추출 정확성

YAML 파싱 예외 처리

파일 생성 및 디렉토리 자동 생성

빌드 실패 동작 검증

### ForbiddenFieldLoggingRuleTest.java 

✔ matches()

MatchType이 EXACT일 때, 텍스트가 정확히 일치하면 위반(true)을 반환한다.

MatchType이 CONTAINS일 때, 텍스트에 키워드가 포함되어 있으면 위반(true)을 반환한다.

MatchType이 PREFIX/SUFFIX일 때, 시작/끝 단어가 일치하면 위반(true)을 반환한다.

MatchType이 REGEX일 때, 정규표현식 패턴에 매칭되면 위반(true)을 반환한다.

검사할 텍스트가 null인 경우, 에러 없이 정상적으로 false를 반환한다.

### JavaLoggingCallExtractorTest.java 

✔ extract()

log.info를 사용한 내용이 있으면 해당 내용을 List<LogCall>형태로 반환한다.

file의 경로가 java파일 위치가 아니라면 빈 배열을 반환한다.

만약 file의 내용이 비었을 경우 빈 배열을 반환한다.

일반 함수 호출(fqn==null)은 무시하고, 뒤에 있는 log.info는 정상적으로 추출한다.

java 파일 자체에 에러가 존재할 경우 빈 배열을 반환한다.

✔ toMethodFqn()

‘log.-’이 입력되었을 때 동일한 형태로 반환된다.

‘Logger.-’, ‘logger.-’, ‘Log.-’, 이 입력되었을 때 ‘log.-’로 변환된다.

Scope가 존재하지 않는 다면 null을 반환한다.

✔ lastToken()

'.’이 한 개 포함된 텍스트의 경우 ‘.’ 이후 문자열을 반환한다.

‘.’이 한 개 이상 포함된 텍스트의 경우 마지막 ‘.’ 이후 문자열을 반환한다.

### LogArgInspectorTest.java


### PolicyLoaderTest.java 

✔ load()
유효한 YAML 파일을 로드하면 LoggingPolicy 객체가 정상 생성된다.

forbiddenFields가 리스트가 아니면 ParserException("forbiddenFields must be a list") 발생한다.

forbiddenFields의 match 필드가 없으면 기본값 EXACT로 설정된다.

YAML root가 Map이 아니면 ParserException("root is not a map")이 발생한다.

✔ validateFileExtension()
확장자가 .yml/.yaml이면 예외가 발생하지 않는다

확장자가 yml/yaml이 아니면 ParserException("유효하지 않은 파일 확장자")이 발생한다

### ReportWriterTest.java 

✔ write()

위반 사항(Violation)이 존재할 경우, 상세 내역(File, Line, Rule 등)이 포함된 리포트를 생성한다.

위반 사항이 없을 경우, [OK] 메시지가 담긴 클린 리포트를 생성한다.

리포트 저장 경로의 상위 디렉토리가 없을 경우, 폴더를 자동 생성한 후 파일을 저장한다.

파일 쓰기 권한이 없거나 시스템 오류 발생 시, RuntimeException을 발생시킨다.

### StaticLogAnalyzerTest.java 

---
## Test Coverage Report
아래는 jacoco를 통한 테스트 커버리지 측정 결과입니다.
