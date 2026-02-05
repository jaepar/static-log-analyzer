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

## 주요 기여 내용 (Individual Contribution)
보안 정책 로더 및 데이터 모델링 (Policy Module)
정책 로딩 시스템 구축: SnakeYAML 라이브러리를 활용해 텍스트 기반 설정 파일을 자바 객체로 변환하는 PolicyLoader를 구현했습니다

매칭 알고리즘 세분화: 필드명 전체 일치(exact)와 부분 일치(contains)를 구분하는 엔티티 구조를 설계하여 검사의 정밀도를 높였습니다.

예외 처리 및 유효성 검증: 파일 확장자 확인 로직을 추가하고 파싱 실패 시 명확한 에러 메시지를 제공하는 ParserException 체계를 구축했습니다.

빌드 환경 최적화: 윈도우 환경에서 발생할 수 있는 한글 인코딩 문제를 해결하기 위해 Gradle 컴파일 옵션을 UTF-8로 표준화하여 협업 안정성을 확보했습니다.
