# 유레카 API 설계

## 공통 응답 형식

```json
{
  "status": 200,
  "message": "Success",
  "data": { ... }
}
```

---

## v1 API

### Auth (인증)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (JWT 발급) |
| POST | `/api/auth/reissue` | 토큰 재발급 |

### User (사용자)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me` | 내 프로필 조회 |
| PATCH | `/api/users/me` | 내 프로필 수정 |
| DELETE | `/api/users/me` | 회원 탈퇴 |
| GET | `/api/users/me/posts` | 내가 쓴 글 목록 |
| GET | `/api/users/me/likes` | 내가 좋아요한 글 목록 (공개 글만, 좋아요 누른 순) |
| GET | `/api/users/{userId}` | 사용자 공개 프로필 (닉네임 · 공개 글 수 · 받은 좋아요 수, 비로그인 가능) |
| GET | `/api/users/{userId}/posts` | 사용자가 쓴 공개 글 목록 (비로그인 가능) |

### Post (게시글)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/posts` | 게시글 작성 (들른 장소 1~20곳 포함) |
| GET | `/api/posts` | 게시글 목록 조회 (필터링, 페이징, `keyword` 로 제목 · 내용 · 장소 이름 검색) |
| GET | `/api/posts/{postId}` | 게시글 상세 조회 |
| PATCH | `/api/posts/{postId}` | 게시글 수정 (places 를 보내면 장소 목록 통째로 교체) |
| DELETE | `/api/posts/{postId}` | 게시글 삭제 |

### Image (이미지)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/posts/{postId}/images` | 게시글 이미지 업로드 (S3, `placeId` 를 주면 장소 사진) |
| DELETE | `/api/images/{imageId}` | 이미지 삭제 |

### Like (좋아요)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/posts/{postId}/like` | 좋아요 토글 |
| GET | `/api/posts/{postId}/like` | 좋아요 상태 조회 |

### Region (지역)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/regions` | 지역 목록 조회 |
| GET | `/api/regions/{regionId}/cities` | 도시 목록 조회 |

### Category (카테고리)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | 카테고리 목록 조회 |
| GET | `/api/categories/{categoryId}/sub` | 서브카테고리 목록 조회 |

---

## v3 API (예정)

### Comment (댓글)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/posts/{postId}/comments` | 댓글 작성 |
| GET | `/api/posts/{postId}/comments` | 댓글 목록 조회 |
| PATCH | `/api/comments/{commentId}` | 댓글 수정 |
| DELETE | `/api/comments/{commentId}` | 댓글 삭제 |

### Bookmark (북마크)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/posts/{postId}/bookmark` | 북마크 토글 |
| GET | `/api/bookmarks` | 내 북마크 목록 |

### Search (검색)

별도 경로 없이 `GET /api/posts?keyword=` 로 검색 (지역 · 카테고리 필터와 함께 쓸 수 있음)
