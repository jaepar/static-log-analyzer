#  Static Log Analyzer 

본 프로젝트는 소스 코드 정적 분석을 통해 로그 파일에 민감한 정보(비밀번호, 인증 토큰 등)가 유출되는 것을 방지하는 보안 도구입니다. 금융 IT 환경에서 발생할 수 있는 데이터 유출 사고를 예방하기 위해 개발했습니다.

---

##  주요 기능

* **보안 정책 커스터마이징**: 외부 설정 파일(`logging-policy.yml`)을 통해 조직의 보안 가이드라인에 맞춘 금지 필드와 감시 대상 메서드를 유연하게 관리할 수 있습니다.
* **JavaParser 기반 정적 분석**: 단순 텍스트 매칭이 아닌 자바 소스 코드의 추상 구문 트리(AST)를 분석하여 로그 호출 지점과 사용된 변수를 정확하게 식별합니다.
* **자동화된 보안 리포트**: 빌드 시점에 분석을 수행하고 그 결과를 `report.txt`로 생성하여 보안 위반 사례를 즉각적으로 파악할 수 있게 합니다.
* **빌드 시스템 통합**: Gradle Task를 통해 로컬 환경 및 CI/CD 파이프라인에서 자동화된 보안 검사가 가능합니다.

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
├── engine/                 # JavaParser 기반 로그 추출 및 분석 엔진
├── exception/              # 공통 예외 처리 모듈 (ParserException)
├── policy/                 # 보안 정책 관리 핵심 모듈
│   ├── entity/             # 데이터 모델 (ForbiddenFieldRule, MatchType)
│   └── service/            # 정책 로더 및 설정 검증 (LoggingPolicy, PolicyLoader)
├── report/                 # 분석 결과 요약 및 파일 출력
└── Main.java               # 프로그램 실행 엔트리 포인트
```

## 설정 및 사용 방법
### 1. 정책 파일 설정 (logging-policy.yml)
프로젝트 루트 폴더에 위치하며, 보안 검사를 수행할 필드명과 로그 메서드를 정의합니다.

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
정책 로딩 시스템 구축: SnakeYAML 라이브러리를 활용해 텍스트 기반 설정 파일을 자바 객체로 변환하는 PolicyLoader를 구현하였습니다.

매칭 알고리즘 세분화: 필드명 전체 일치(exact)와 부분 일치(contains)를 구분하는 엔티티 구조를 설계하여 검사의 정밀도를 높였습니다.

예외 처리 및 유효성 검증: 파일 확장자 확인 로직을 추가하고 파싱 실패 시 명확한 에러 메시지를 제공하는 ParserException 체계를 구축했습니다.

빌드 환경 최적화: 윈도우 환경에서 발생할 수 있는 한글 인코딩 문제를 해결하기 위해 Gradle 컴파일 옵션을 UTF-8로 표준화하여 협업 안정성을 확보했습니다.
