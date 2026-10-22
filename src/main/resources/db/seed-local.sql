-- 로컬 시드 데이터 (application-local.yml 의 spring.sql.init 으로 시작할 때마다 들어감 - 로컬 전용)
-- 지역·도시·카테고리·세부 테마는 2021 원본 프로젝트(유레카) 글쓰기 화면(write.jsp) 목록 기준
-- 원본의 '누구와'(main_option: 나홀로 여행·가족 여행 등) 단계는 현재 엔티티에 없어서 세부 테마(detail_option)만 sub_categories 로 넣음
-- 테스트 계정: test@footprint.local / footprint-local-1234  (로컬 DB 에만 존재. 좋아요용 보조 계정 3개는 로그인 불필요)

INSERT INTO common_code_groups (code, name, description, is_active, created_at, updated_at) VALUES
  ('CATEGORY_TYPE', '카테고리 유형', '게시글 카테고리 유형', true, now(), now());
INSERT INTO common_codes (group_id, code, name, sort_order, is_active, created_at, updated_at) VALUES
  (1, 'TRAVEL', '여행가자', 1, true, now(), now()),
  (1, 'FOOD', '맛집가자', 2, true, now(), now()),
  (1, 'ACTIVITY', '놀러가자', 3, true, now(), now());

INSERT INTO regions (name, created_at, updated_at) VALUES
  ('수도권', now(), now()),
  ('강원도', now(), now()),
  ('경상도/부산', now(), now()),
  ('제주도', now(), now()),
  ('전라도/광주', now(), now()),
  ('충청도/대전', now(), now());
INSERT INTO cities (region_id, name, created_at, updated_at) VALUES
  (1, '서울', now(), now()),
  (1, '경기', now(), now()),
  (1, '인천', now(), now()),
  (1, '송도', now(), now()),
  (1, '가평', now(), now()),
  (2, '강릉', now(), now()),
  (2, '속초', now(), now()),
  (2, '춘천', now(), now()),
  (2, '동해', now(), now()),
  (2, '정선', now(), now()),
  (3, '부산', now(), now()),
  (3, '포항', now(), now()),
  (3, '경주', now(), now()),
  (3, '거제', now(), now()),
  (3, '고성', now(), now()),
  (4, '서귀포', now(), now()),
  (4, '제주시', now(), now()),
  (4, '서제주', now(), now()),
  (4, '동제주', now(), now()),
  (5, '전주', now(), now()),
  (5, '여수', now(), now()),
  (5, '순천', now(), now()),
  (5, '담양', now(), now()),
  (5, '해남', now(), now()),
  (6, '대전', now(), now()),
  (6, '공주', now(), now()),
  (6, '태안', now(), now()),
  (6, '단양', now(), now());
-- 떠나기 좋은 도시 10곳 (원본 recommend_city) - 이미지는 FE 정적 파일 /images/recommendImages/
INSERT INTO recommend_cities (city_id, thumbnail_url, image_url, display_order, is_active, created_at, updated_at) VALUES
  (1, '/images/recommendImages/seoul.jpg', '/images/recommendImages/seoul2.jpg', 1, true, now(), now()),
  (6, '/images/recommendImages/gangneung.jpg', '/images/recommendImages/gangneung2.jpg', 2, true, now(), now()),
  (7, '/images/recommendImages/sokcho.jpg', '/images/recommendImages/sokcho2.jpg', 3, true, now(), now()),
  (4, '/images/recommendImages/songdo.jpg', '/images/recommendImages/songdo2.jpg', 4, true, now(), now()),
  (11, '/images/recommendImages/busan.jpg', '/images/recommendImages/busan2.jpg', 5, true, now(), now()),
  (21, '/images/recommendImages/yeosu.jpg', '/images/recommendImages/yeosu2.jpg', 6, true, now(), now()),
  (17, '/images/recommendImages/jeju.jpg', '/images/recommendImages/jeju2.jpg', 7, true, now(), now()),
  (5, '/images/recommendImages/gapyeong.jpg', '/images/recommendImages/gapyeong2.jpg', 8, true, now(), now()),
  (23, '/images/recommendImages/damyang.jpg', '/images/recommendImages/damyang2.jpg', 9, true, now(), now()),
  (12, '/images/recommendImages/pohang.jpg', '/images/recommendImages/pohang2.jpg', 10, true, now(), now());

INSERT INTO categories (name, type, created_at, updated_at) VALUES
  ('여행가자', 'TRAVEL', now(), now()),
  ('맛집가자', 'FOOD', now(), now()),
  ('놀러가자', 'ACTIVITY', now(), now());
INSERT INTO sub_categories (category_id, name, created_at, updated_at) VALUES
  (1, '산으로', now(), now()),
  (1, '바다로', now(), now()),
  (1, '도심속으로', now(), now()),
  (1, '역사속으로', now(), now()),
  (1, '국내 속 작은 해외로', now(), now()),
  (2, '한식', now(), now()),
  (2, '중식', now(), now()),
  (2, '일식', now(), now()),
  (2, '양식', now(), now()),
  (2, '분식', now(), now()),
  (2, '카페', now(), now()),
  (3, '활동적인', now(), now()),
  (3, '힐링/휴식', now(), now()),
  (3, '자기개발', now(), now()),
  (3, '사진관', now(), now()),
  (3, '스파/워터파크', now(), now()),
  (3, '테마파크', now(), now()),
  (3, '레저/액티비티', now(), now()),
  (3, '게임/스포츠', now(), now()),
  (3, '노래방/코노', now(), now()),
  (3, '전시/공연', now(), now()),
  (3, '동물/식물원', now(), now()),
  (3, '공방/체험', now(), now()),
  (3, '만화/북카페', now(), now()),
  (3, '영화관', now(), now()),
  (3, '체험/레저', now(), now()),
  (3, '캠핑', now(), now()),
  (3, '키즈카페', now(), now()),
  (3, '체험학습', now(), now()),
  (3, '반려동물 놀이터', now(), now()),
  (3, '반려동물 유치원', now(), now()),
  (3, '반려동물 물놀이', now(), now()),
  (3, '펫페어', now(), now());

INSERT INTO users (email, password, nickname, name, email_verified, role, status, created_at, updated_at) VALUES
  ('test@footprint.local', '$2a$10$cfeHUNrQOfuQRvo/0tHXa.6mPKgVwmm/FnjCMTzeKcCrOM8hf5ohe', '발자취테스터', '테스트', true, 'USER', 'ACTIVE', now(), now()),
  ('walker1@footprint.local', '$2a$10$cfeHUNrQOfuQRvo/0tHXa.6mPKgVwmm/FnjCMTzeKcCrOM8hf5ohe', '바다산책러', '보조1', true, 'USER', 'ACTIVE', now(), now()),
  ('walker2@footprint.local', '$2a$10$cfeHUNrQOfuQRvo/0tHXa.6mPKgVwmm/FnjCMTzeKcCrOM8hf5ohe', '골목탐험가', '보조2', true, 'USER', 'ACTIVE', now(), now()),
  ('walker3@footprint.local', '$2a$10$cfeHUNrQOfuQRvo/0tHXa.6mPKgVwmm/FnjCMTzeKcCrOM8hf5ohe', '맛집지도', '보조3', true, 'USER', 'ACTIVE', now(), now());

INSERT INTO posts (user_id, city_id, sub_category_id, title, content, summary, view_count, like_count, status, created_at, updated_at) VALUES
  (1, 6, 2, '강릉 안목해변 아침 산책', '해 뜨기 전에 안목해변을 걸었다. 커피거리 문 열기 전이라 조용했고, 파도 소리만 들렸다.', '안목해변 해돋이 산책 코스', 120, 4, 'PUBLISHED', now() - interval '240 hour', now() - interval '240 hour'),
  (1, 7, 6, '속초 중앙시장 닭강정이랑 오징어순대', '시장 입구부터 줄이 길었다. 닭강정은 식어도 바삭했다.', '속초 중앙시장 먹거리 정리', 85, 3, 'PUBLISHED', now() - interval '200 hour', now() - interval '200 hour'),
  (1, 11, 3, '부산 감천문화마을 골목 여행', '알록달록한 집들 사이 계단을 오르내렸다. 어린왕자 포토존은 줄이 있다.', '감천문화마을 도보 코스', 140, 4, 'PUBLISHED', now() - interval '180 hour', now() - interval '180 hour'),
  (1, 21, 13, '여수 밤바다 낭만포차 거리', '해 지고 나서 돌산대교 조명이 켜지면 분위기가 확 바뀐다.', '여수 밤바다 산책', 97, 2, 'PUBLISHED', now() - interval '160 hour', now() - interval '160 hour'),
  (1, 17, 1, '한라산 관음사 코스 당일치기', '새벽 6시에 출발해서 백록담까지. 하산이 더 힘들었다.', '관음사 코스 시간표', 160, 3, 'PUBLISHED', now() - interval '140 hour', now() - interval '140 hour'),
  (1, 1, 11, '성수동 카페 세 곳', '공장을 고친 카페들이라 천장이 높다. 평일 오전이 한가하다.', '성수동 작업하기 좋은 카페', 72, 1, 'PUBLISHED', now() - interval '120 hour', now() - interval '120 hour'),
  (1, 5, 18, '가평 수상레저 하루 코스', '오전에 웨이크보드, 오후엔 바나나보트. 구명조끼는 꼭 맞는 걸로.', '가평 수상레저 후기', 55, 2, 'PUBLISHED', now() - interval '100 hour', now() - interval '100 hour'),
  (1, 23, 4, '담양 죽녹원이랑 소쇄원', '대나무숲 사이로 바람이 불면 소리가 좋다. 소쇄원은 조용히 보기.', '죽녹원 → 소쇄원 코스', 64, 1, 'PUBLISHED', now() - interval '80 hour', now() - interval '80 hour'),
  (1, 12, 8, '포항 죽도시장 물회', '물회에 국수 말아 먹는 게 포항식이다. 시장 안쪽 가게가 덜 붐볐다.', '죽도시장 물회 맛집', 48, 2, 'PUBLISHED', now() - interval '60 hour', now() - interval '60 hour'),
  (1, 4, 15, '송도 센트럴파크 야경이랑 사진관', '보트 타고 한 바퀴 돈 뒤 근처 셀프 사진관에서 사진 남김.', '송도 야경 데이트 코스', 39, 0, 'PUBLISHED', now() - interval '40 hour', now() - interval '40 hour'),
  (1, 17, 27, '제주 협재 해변 캠핑', '바다 바로 앞 캠핑장. 바람이 세서 팩을 단단히 박아야 한다.', '협재 캠핑 준비물', 58, 1, 'PUBLISHED', now() - interval '20 hour', now() - interval '20 hour'),
  (1, 1, 5, '서울 서래마을 유럽 골목', '프랑스 빵집이랑 작은 공원. 짧게 해외 온 기분.', '서래마을 반나절 코스', 33, 0, 'PUBLISHED', now() - interval '5 hour', now() - interval '5 hour');
-- 대표 이미지: 글의 도시 사진 (seq 1 = 목록 썸네일)
INSERT INTO post_images (post_id, url, seq, created_at) VALUES
  (1, '/images/recommendImages/gangneung2.jpg', 1, now()),
  (1, '/images/recommendImages/gangneung.jpg', 2, now()),
  (2, '/images/recommendImages/sokcho2.jpg', 1, now()),
  (2, '/images/recommendImages/sokcho.jpg', 2, now()),
  (3, '/images/recommendImages/busan2.jpg', 1, now()),
  (3, '/images/recommendImages/busan.jpg', 2, now()),
  (4, '/images/recommendImages/yeosu2.jpg', 1, now()),
  (4, '/images/recommendImages/yeosu.jpg', 2, now()),
  (5, '/images/recommendImages/jeju2.jpg', 1, now()),
  (5, '/images/recommendImages/jeju.jpg', 2, now()),
  (6, '/images/recommendImages/seoul2.jpg', 1, now()),
  (6, '/images/recommendImages/seoul.jpg', 2, now()),
  (7, '/images/recommendImages/gapyeong2.jpg', 1, now()),
  (7, '/images/recommendImages/gapyeong.jpg', 2, now()),
  (8, '/images/recommendImages/damyang2.jpg', 1, now()),
  (8, '/images/recommendImages/damyang.jpg', 2, now()),
  (9, '/images/recommendImages/pohang2.jpg', 1, now()),
  (9, '/images/recommendImages/pohang.jpg', 2, now()),
  (10, '/images/recommendImages/songdo2.jpg', 1, now()),
  (10, '/images/recommendImages/songdo.jpg', 2, now()),
  (11, '/images/recommendImages/jeju2.jpg', 1, now()),
  (11, '/images/recommendImages/jeju.jpg', 2, now()),
  (12, '/images/recommendImages/seoul2.jpg', 1, now()),
  (12, '/images/recommendImages/seoul.jpg', 2, now());
-- 좋아요: posts.like_count 와 개수를 맞춤 (테스트 계정 + 보조 계정 3개)
INSERT INTO post_likes (post_id, user_id, created_at) VALUES
  (1, 1, now()),
  (1, 2, now()),
  (1, 3, now()),
  (1, 4, now()),
  (2, 1, now()),
  (2, 2, now()),
  (2, 3, now()),
  (3, 1, now()),
  (3, 2, now()),
  (3, 3, now()),
  (3, 4, now()),
  (4, 1, now()),
  (4, 2, now()),
  (5, 1, now()),
  (5, 2, now()),
  (5, 3, now()),
  (6, 1, now()),
  (7, 1, now()),
  (7, 2, now()),
  (8, 1, now()),
  (9, 1, now()),
  (9, 2, now()),
  (11, 1, now());
