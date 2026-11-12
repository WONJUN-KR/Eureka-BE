# 서비스 플로우

## 화면 플로우

```
메인 (홈)
    ├── 회원가입 / 로그인
    ├── 게시글 목록 (지역별 / 카테고리별)
    │   └── 게시글 상세
    │       ├── 좋아요
    │       ├── 댓글 (v3)
    │       └── 북마크 (v3)
    ├── 게시글 작성
    │   └── 이미지 업로드 (S3)
    ├── 검색 (게시글 목록 keyword)
    └── 마이페이지
        ├── 프로필 수정
        ├── 내 게시글
        ├── 내 북마크 (v3)
        └── 회원 탈퇴
```

---

## 게시글 작성 플로우

```
1. 사용자: 게시글 작성 페이지 진입
2. 지역 선택: Region → City
3. 카테고리 선택: Category → SubCategory
4. 제목, 내용 작성
5. 이미지 첨부 (선택, 최대 10장)
6. 임시저장 (DRAFT) 또는 게시 (PUBLISHED)
7. 백엔드: POST /api/posts → 게시글 생성
8. 이미지 있으면: POST /api/posts/{id}/images → S3 업로드
```

---

## 게시글 조회 플로우

```
1. 메인 페이지: GET /api/posts (최신순)
2. 필터링:
   - 지역: ?regionId=1&cityId=3
   - 카테고리: ?categoryId=2&subCategoryId=5
   - 정렬: ?sort=newest|popular|views
3. 페이징: ?page=0&size=20
4. 상세 조회: GET /api/posts/{id}
   - 조회수 증가
   - 좋아요 상태 포함 (로그인 시)
```

---

## 인증 플로우

```
회원가입:
1. POST /api/auth/signup (email, password, nickname, name)
2. 비밀번호 BCrypt 해시 → DB 저장
3. 성공 응답

로그인:
1. POST /api/auth/login (email, password)
2. BCrypt 비밀번호 검증
3. Access Token + Refresh Token 발급
4. Refresh Token DB 저장

토큰 재발급:
1. POST /api/auth/reissue (refreshToken)
2. DB에서 Refresh Token 검증
3. 새 Access Token + Refresh Token 발급
4. 기존 Refresh Token 삭제 (Rotation)
```
