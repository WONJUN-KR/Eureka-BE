plugins {
    java
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.eureka"
version = "0.0.1-SNAPSHOT"
description = "eureka"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot  Starters
    // implementation = "요리할 때도 쓰고, 먹을 때도 필요해" (Spring Boot)
    // compileOnly = "레시피 볼 때만 필요해, 먹을 땐 필요 없어" (Lombok)
    // runtimeOnly = "먹을 때만 필요해, 레시피엔 안 써도 돼" (PostgreSQL 드라이버)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-cache") // 캐싱 기능 활성화

    // implementation이란?
    // Gradle에서 "이 라이브러리를 프로젝트에서 쓸게" 라는 의미입니다.

    // JWT (Json Web Token)
    // 로그인 후 토큰 발급/검증에 사용
    implementation("io.jsonwebtoken:jjwt-api:0.12.6") // JWT 인터페이스 (코드 작성용)
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")     // JWT 구현체 (실행용)
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")  // JWT JSON 변환 (실행용)

    // Swagger (SpringDoc OpenAPI)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")

    // AWS S3
    implementation(platform("software.amazon.awssdk:bom:2.31.4")) // AWS SDK 버전 관리
    implementation("software.amazon.awssdk:s3")                              // S3 클라이언트

    // Caffeine (인메모리 캐시)
    // 자주 조회되는 데이터 캐싱 (지역, 카테고리 등)
    implementation("com.github.ben-manes.caffeine:caffeine")

    // Lombok (코드 자동 생성)
    // @Getter, @Setter, @Builder 등 보일러플레이트 제거

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // DB
    runtimeOnly("org.postgresql:postgresql") // PostgreSQL 드라이버

    // TEST
    testImplementation("org.springframework.boot:spring-boot-starter-test")  // JUnit, Mockito  등 포함
    testImplementation("org.springframework.security:spring-security-test") // Security 테스트 유형
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2") // 테스트용 인메모리 DB
}

tasks.withType<Test> {
    useJUnitPlatform()
}
