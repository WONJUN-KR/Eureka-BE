# Entity 설계

## 개요

- **V1 (MVP)**: users, posts, post_places, regions, cities, categories, sub_categories, post_images, post_likes, recommend_cities, common_code_groups, common_codes
- **V2**: 인프라/품질 (Docker, CI/CD, 테스트, 배포 환경)
- **V3**: comments, bookmarks, post_reports
- **V4**: tags, post_tags, 대댓글 (comment.parent_id), follows, notifications

---

## BaseEntity (공통)

| 필드 | 타입 | 설명 |
|------|------|------|
| created_at | LocalDateTime | 생성일시 |
| updated_at | LocalDateTime | 수정일시 |

---

# V1 Entity

---

## users

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| email | String (UK) | 이메일 |
| password | String | BCrypt 해시 |
| nickname | String (UK) | 닉네임 |
| name | String | 이름 |
| birth | LocalDate | 생년월일 |
| gender | Enum | MALE, FEMALE, OTHER |
| email_verified | Boolean | 이메일 인증 여부 |
| role | Enum | USER, ADMIN |
| status | String | 공통코드 (ACTIVE, INACTIVE, BANNED) |
| deleted_at | LocalDateTime | soft delete |
| provider | Enum | 가입 경로 LOCAL, KAKAO (기본 LOCAL) |
| provider_id | String(64) | 카카오 회원번호 (LOCAL 은 null) |
| created_at, updated_at | LocalDateTime | BaseEntity |

- UNIQUE(provider, provider_id)

---

## posts

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| user_id | Long (FK → users) | 작성자 |
| city_id | Long (FK → cities) | 도시 |
| sub_category_id | Long (FK → sub_categories) | 서브카테고리 |
| title | String | 제목 |
| content | String (TEXT) | 내용 |
| summary | String | 요약 |
| view_count | Integer | 조회수 |
| like_count | Integer | 좋아요 수 (역정규화) |
| status | String | 공통코드 (DRAFT, PUBLISHED, HIDDEN) |
| deleted_at | LocalDateTime | soft delete |
| created_at, updated_at | LocalDateTime | BaseEntity |

---

## regions

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| name | String | 지역명 (예: 서울, 부산) |
| created_at, updated_at | LocalDateTime | BaseEntity |

---

## cities

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| region_id | Long (FK → regions) | 지역 |
| name | String | 도시명 (예: 강남구, 해운대구) |
| created_at, updated_at | LocalDateTime | BaseEntity |

---

## categories

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| name | String | 카테고리명 |
| type | String | 공통코드 (TRAVEL, FOOD, ACTIVITY) |
| created_at, updated_at | LocalDateTime | BaseEntity |

---

## sub_categories

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| category_id | Long (FK → categories) | 상위 카테고리 |
| name | String | 서브카테고리명 |
| created_at, updated_at | LocalDateTime | BaseEntity |

---

## post_images

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| post_id | Long (FK → posts) | 게시글 |
| place_id | Long (FK → post_places, nullable) | 장소 사진이면 그 장소, 게시글 전체 사진이면 null |
| url | String | S3 이미지 URL |
| seq | Integer | 표시 순서 |
| created_at | LocalDateTime | |

---

## post_places

게시글(여행 한 번)에서 들른 장소. seq 순서대로 이으면 지도 경로가 됨.

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| post_id | Long (FK → posts) | 게시글 |
| seq | Integer | 들른 순서 (1부터) |
| name | String(100) | 장소 이름 |
| address | String | 주소 (nullable) |
| latitude | Double | 위도 |
| longitude | Double | 경도 |
| kakao_place_id | String(20) | 카카오 장소 검색 ID (nullable) |
| sub_category_id | Long (FK → sub_categories, nullable) | 장소 종류 |
| memo | String(100) | 한 줄 메모 (nullable) |
| visited_at | LocalTime | 들른 시간 (nullable) |
| created_at / updated_at | LocalDateTime | |

- 게시글당 1~20곳, 장소당 사진 0~3장
- 수정 시 장소 목록을 통째로 보냄 → 빠진 장소는 삭제되고 그 장소 사진은 게시글 사진으로 남음

---

## post_likes

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| post_id | Long (FK → posts) | 게시글 |
| user_id | Long (FK → users) | 사용자 |
| created_at | LocalDateTime | |

- UNIQUE(post_id, user_id)

---

## recommend_cities

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| city_id | Long (FK → cities) | 도시 |
| thumbnail_url | String | 썸네일 이미지 |
| image_url | String | 대표 이미지 |
| display_order | Integer | 표시 순서 |
| is_active | Boolean | 활성 여부 |
| created_at, updated_at | LocalDateTime | BaseEntity |

---

## common_code_groups

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| code | String (UK) | 그룹 코드 (예: USER_STATUS) |
| name | String | 그룹명 |
| description | String | 설명 |
| is_active | Boolean | 활성 여부 |
| created_at, updated_at | LocalDateTime | BaseEntity |

---

## common_codes

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| group_id | Long (FK → common_code_groups) | 그룹 |
| code | String | 코드 |
| name | String | 코드명 |
| sort_order | Integer | 정렬 순서 |
| is_active | Boolean | 활성 여부 |
| created_at, updated_at | LocalDateTime | BaseEntity |

### 공통코드 데이터

| group | code | name | sort_order |
|-------|------|------|-----------|
| USER_STATUS | ACTIVE | 활성 | 1 |
| USER_STATUS | INACTIVE | 비활성 | 2 |
| USER_STATUS | BANNED | 정지 | 3 |
| POST_STATUS | DRAFT | 임시저장 | 1 |
| POST_STATUS | PUBLISHED | 게시됨 | 2 |
| POST_STATUS | HIDDEN | 숨김 | 3 |
| CATEGORY_TYPE | TRAVEL | 여행가자 | 1 |
| CATEGORY_TYPE | FOOD | 맛집가자 | 2 |
| CATEGORY_TYPE | ACTIVITY | 놀러가자 | 3 |
| REPORT_STATUS | PENDING | 대기 | 1 |
| REPORT_STATUS | RESOLVED | 처리완료 | 2 |
| REPORT_STATUS | REJECTED | 반려 | 3 |

---

## ENUM (고정값)

```java
public enum Gender {
    MALE, FEMALE, OTHER
}

public enum UserRole {
    USER, ADMIN
}
```

---

# V3 Entity

---

## comments

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| post_id | Long (FK → posts) | 게시글 |
| user_id | Long (FK → users) | 작성자 |
| content | String | 내용 |
| deleted_at | LocalDateTime | soft delete |
| created_at, updated_at | LocalDateTime | BaseEntity |

---

## bookmarks

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| post_id | Long (FK → posts) | 게시글 |
| user_id | Long (FK → users) | 사용자 |
| created_at | LocalDateTime | |

- UNIQUE(post_id, user_id)

---

## post_reports

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| post_id | Long (FK → posts) | 게시글 |
| user_id | Long (FK → users) | 신고자 |
| reason | Enum | 신고 사유 (FALSE_INFO, INAPPROPRIATE, SPAM, COPYRIGHT, ETC) |
| status | String | 공통코드 (PENDING, RESOLVED, REJECTED) |
| created_at | LocalDateTime | |

- UNIQUE(post_id, user_id)

---

# V4 Entity (태그/대댓글)

---

## tags

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| name | String (UK) | 태그명 |
| created_at | LocalDateTime | |

---

## post_tags

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| post_id | Long (FK → posts) | 게시글 |
| tag_id | Long (FK → tags) | 태그 |

V4에서 comments 테이블에 `parent_id (FK → self)` 추가하여 대댓글 지원.

---

# V4 Entity (소셜)

---

## follows

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| follower_id | Long (FK → users) | 팔로우 하는 사용자 |
| following_id | Long (FK → users) | 팔로우 받는 사용자 |
| created_at | LocalDateTime | |

- UNIQUE(follower_id, following_id)

---

## notifications

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | |
| user_id | Long (FK → users) | 수신자 |
| type | String | 공통코드 |
| content | String | 알림 내용 |
| is_read | Boolean | 읽음 여부 |
| created_at | LocalDateTime | |

---

## 기존 → 신규 매핑

| 기존 테이블 (2021) | 신규 테이블 |
|---------------------|-------------|
| users | users |
| board | posts |
| area | regions |
| city | cities |
| main_option | categories |
| detail_option | sub_categories |
| image | post_images |
| (recommend_cnt) | post_likes |
| (report_cnt) | post_reports |
| recommend_city | recommend_cities |
| (신규) | common_code_groups |
| (신규) | common_codes |
