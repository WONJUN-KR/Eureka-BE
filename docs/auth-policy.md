# 인증/권한 정책

## 인증 방식

### JWT 토큰

| 토큰 | 만료 시간 | 용도 | 저장 위치 |
|------|----------|------|----------|
| Access Token | 30분 | API 인증 | 클라이언트 |
| Refresh Token | 14일 | Access Token 재발급 | DB + 클라이언트 |

### 토큰 구조

```json
// Access Token Payload
{
  "sub": "1",
  "email": "user@example.com",
  "role": "USER",
  "iat": 1704067200,
  "exp": 1704070800
}
```

### Refresh Token 관리

- **DB 저장**: 로그인 성공 시 저장
- **로그아웃**: DB에서 Refresh Token 삭제
- **재발급**: 기존 토큰 삭제 후 새 토큰 저장 (Rotation)

---

## API 권한 매트릭스

### Public (인증 불필요)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /api/auth/signup | 회원가입 |
| POST | /api/auth/login | 로그인 |
| POST | /api/auth/reissue | 토큰 재발급 |
| GET | /api/auth/kakao | 카카오 로그인 시작 (302) |
| GET | /login/oauth2/code/kakao | 카카오 로그인 콜백 (302) |
| GET | /api/posts | 게시글 목록 |
| GET | /api/posts/{id} | 게시글 상세 |
| GET | /api/regions | 지역 목록 |
| GET | /api/categories | 카테고리 목록 |
| GET | /swagger-ui/** | Swagger UI |
| GET | /v3/api-docs/** | API 문서 |

### Authenticated (로그인 필요)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/users/me | 내 프로필 | 본인 |
| PATCH | /api/users/me | 프로필 수정 | 본인 |
| DELETE | /api/users/me | 회원 탈퇴 | 본인 |
| POST | /api/posts | 게시글 작성 | 로그인 사용자 |
| PATCH | /api/posts/{id} | 게시글 수정 | 작성자 본인 |
| DELETE | /api/posts/{id} | 게시글 삭제 | 작성자 본인 |
| POST | /api/posts/{id}/images | 이미지 업로드 | 작성자 본인 |
| POST | /api/posts/{id}/like | 좋아요 토글 | 로그인 사용자 |
| POST | /api/posts/{id}/comments | 댓글 작성 | 로그인 사용자 |
| PATCH | /api/comments/{id} | 댓글 수정 | 작성자 본인 |
| DELETE | /api/comments/{id} | 댓글 삭제 | 작성자 본인 |
| POST | /api/posts/{id}/bookmark | 북마크 토글 | 로그인 사용자 |

---

## 권한 체크 로직

### 본인 확인

```java
if (!post.getUser().getId().equals(currentUserId)) {
    throw PostException.unauthorized();
}
```

---

## 에러 응답

### 인증 실패 (401)

```json
{
  "status": 401,
  "message": "로그인이 필요합니다",
  "data": null
}
```

### 권한 부족 (403)

```json
{
  "status": 403,
  "message": "권한이 없습니다",
  "data": null
}
```

---

## 토큰 재발급

### POST /api/auth/reissue

**Request**
```json
{
  "refreshToken": "리프레시 토큰"
}
```

**Response (200)**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "accessToken": "새 액세스 토큰",
    "refreshToken": "새 리프레시 토큰"
  }
}
```

---

## 카카오 로그인

브라우저가 이동하는 흐름이라 JSON 대신 302 로 응답합니다.

1. 프론트가 브라우저를 `GET /api/auth/kakao` 로 보냄
2. 백엔드가 state 를 `kakao_oauth_state` 쿠키(HttpOnly, 5분)에 두고 카카오 로그인 화면으로 302
3. 카카오가 `GET /login/oauth2/code/kakao?code=...&state=...` 로 돌려보냄 (카카오 콘솔에 등록한 리다이렉트 URI)
4. state 가 쿠키와 같으면 인가 코드로 카카오 회원번호 · 닉네임을 받아옴
5. 처음이면 가입, 이미 있으면 그 계정으로 토큰 발급
6. `oauth.kakao.frontend-redirect-uri` 로 302
   - 성공: `#accessToken=...&refreshToken=...`
   - 실패(state 다름 · 카카오 오류): `#error=kakao_login_failed`

토큰은 서버 로그 · Referer 에 남지 않게 `#` 뒤에 붙입니다. 프론트는 콜백 화면에서 읽어 저장한 뒤 주소에서 지웁니다.

### 카카오로 가입한 계정

| 필드 | 값 |
|------|----|
| provider / provider_id | KAKAO / 카카오 회원번호 |
| email | `kakao_{회원번호}@kakao.eureka.local` (내부용, 이메일 로그인 불가) |
| password | 아무도 모르는 무작위 값 (비밀번호 로그인 불가) |
| nickname | 카카오 닉네임(최대 16자) - 겹치면 뒤에 숫자 4자리, 없으면 '여행자' |

카카오에서는 닉네임만 선택 동의로 받습니다. 이메일은 받지 않습니다.

### 설정 (application-local.yml, gitignore)

```yaml
oauth:
  kakao:
    client-id: 카카오 REST API 키
    client-secret: 카카오 클라이언트 시크릿
    redirect-uri: http://localhost:8095/login/oauth2/code/kakao
    frontend-redirect-uri: http://localhost:3000/auth/kakao/callback
```
