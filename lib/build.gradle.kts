plugins {
    `java-library`

    application
}

group = "dev.loganalyzer"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Java AST 파싱
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.26.2")

    implementation(libs.guava)
    
    implementation ("com.github.javaparser:javaparser-symbol-solver-core:3.28.0")

    // YAML 로딩
    implementation("org.yaml:snakeyaml:2.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass.set("dev.loganalyzer.cli.Main")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

/**
 * Gradle task로 정적 분석기를 실행
 * - 위반 있으면 exit(1)로 빌드 실패
 * - report 파일 생성
 */
tasks.register<JavaExec>("staticLogAnalyze") {
    group = "verification"
    description = "Static logging analyzer (CWE-532) - fail on forbidden field logging"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("dev.loganalyzer.cli.Main")

    args(
        "--root=.",
        "--policy=logging-policy.yml",
        "--report=build/logging-report/report.txt"
    )
}

// gradle check 실행 시 자동으로 실행
tasks.named("check") {
    dependsOn("staticLogAnalyze")
}