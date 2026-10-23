# 유레카 Backend

여행했던 지역의 경로를 기록하고 공유하는 여행 커뮤니티 서비스

## 기술 스택

- Java 21 / Spring Boot 3.5.9 / Gradle (Kotlin DSL)
- Spring Data JPA / PostgreSQL (Supabase)
- Spring Security + JWT
- SpringDoc OpenAPI (Swagger)
- AWS S3 (이미지 업로드)
- Caffeine Cache

## 빌드 & 실행

```bash
./gradlew build      # 빌드
./gradlew bootRun    # 실행
./gradlew test       # 테스트
```

## 패키지 구조

```
com.eureka
├── global/           # config, common, security, exception, util
└── domain/{feature}/ # controller, service, repository, entity, dto
```

## 코딩 컨벤션

- 들여쓰기: 4 spaces
- 클래스: PascalCase / 메서드·변수: camelCase
- DTO: `*Request`, `*Response` 접미사
- 패턴: `docs/coding-patterns.md` 참조

## Git

- 브랜치: `feature/`, `fix/`, `refactor/`
- PR 대상: `dev`
- **커밋 시 반드시 메시지만 텍스트로 먼저 보여주고, 사용자가 커밋을 요청하면 실행할 것** (공동 커밋 방지)
- **Co-Authored-By 넣지 말 것**
- **PR 머지 후**: `dev`로 체크아웃 → `git pull` → 로컬 feature 브랜치 삭제

## 개발 가이드

**코드 작성 전 관련 문서를 반드시 먼저 읽을 것:**

| 작업 | 참조 문서 |
|------|----------|
| API 구현 | `docs/api-design.md`, `docs/error-codes.md` |
| Entity 추가/수정 | `docs/entity-design.md` |
| 인증/권한 구현 | `docs/auth-policy.md` |
| 비즈니스 로직 | `docs/business-rules.md` |
| 설계 확인 | `docs/decisions.md`, `docs/flow.md` |
| 코딩 패턴 | `docs/coding-patterns.md` |

## 문서 목록

| 문서 | 설명 |
|------|------|
| `docs/entity-design.md` | Entity 구조 (V1~V4) |
| `docs/api-design.md` | API 엔드포인트 개요 |
| `docs/business-rules.md` | 비즈니스 규칙 |
| `docs/auth-policy.md` | 인증/권한 정책 |
| `docs/error-codes.md` | 에러 코드 정의 |
| `docs/flow.md` | 화면/서비스 플로우 |
| `docs/decisions.md` | 설계 결정 사항 |
| `docs/coding-patterns.md` | 코딩 패턴 |

## 프로젝트 배경

- 2021년 학원(중앙 HTA) 팀 프로젝트 "유레카"를 최신 스택으로 리빌딩
- 레거시 코드: https://github.com/hanheeda/teamZoom
- GOTCHA! 프로젝트 구조를 벤치마킹
- 상세: `docs/claude.md` 참조

## 버전별 로드맵

| 버전 | 내용 |
|------|--------|
| v1 | 핵심 도메인 (auth, user, post, region, category, image, like) |
| v2 | 인프라/품질 (Docker, CI/CD, 테스트, 배포 환경) |
| v3 | 부가 기능 (comment, bookmark, report, search) |
| v4 | 소셜 기능 (follow, notification, tag, 대댓글) |
| v5 | OAuth, admin |
