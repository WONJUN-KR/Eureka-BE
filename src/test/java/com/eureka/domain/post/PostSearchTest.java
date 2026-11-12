package com.eureka.domain.post;

import com.eureka.domain.category.entity.Category;
import com.eureka.domain.category.entity.SubCategory;
import com.eureka.domain.category.repository.CategoryRepository;
import com.eureka.domain.category.repository.SubCategoryRepository;
import com.eureka.domain.post.dto.CreatePostRequest;
import com.eureka.domain.post.dto.PostResponse;
import com.eureka.domain.post.service.PostService;
import com.eureka.domain.region.entity.City;
import com.eureka.domain.region.entity.Region;
import com.eureka.domain.region.repository.CityRepository;
import com.eureka.domain.region.repository.RegionRepository;
import com.eureka.domain.user.entity.User;
import com.eureka.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// 게시글 목록 keyword 검색 - 제목 · 내용 · 장소 이름, 대소문자 무시, 다른 필터와 함께 (H2)
@SpringBootTest
@Transactional
class PostSearchTest {

    @Autowired private PostService postService;
    @Autowired private UserRepository userRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SubCategoryRepository subCategoryRepository;
    @Autowired private ObjectMapper objectMapper;

    private User user;
    private City busan;
    private City seoul;
    private SubCategory food;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder().email("s@test.local").password("x").nickname("검색").name("검색").build());
        Region region = regionRepository.save(Region.builder().name("전국").build());
        busan = cityRepository.save(City.builder().region(region).name("부산").build());
        seoul = cityRepository.save(City.builder().region(region).name("서울").build());
        Category category = categoryRepository.save(Category.builder().name("맛집가자").type("FOOD").build());
        food = subCategoryRepository.save(SubCategory.builder().category(category).name("한식").build());

        create(busan, "부산 하루 코스", "바다 보고 국밥", "감천문화마을");
        create(busan, "Busan Night", "광안리 야경", "광안대교 전망대");
        create(seoul, "서울 골목", "100% 만족한 하루", "익선동 한옥거리");
    }

    @Test
    void matchesTitleContentAndPlaceName() {
        assertThat(titles(null, "국밥")).containsExactly("부산 하루 코스");
        assertThat(titles(null, "익선동")).containsExactly("서울 골목");
        assertThat(titles(null, "night")).containsExactly("Busan Night");
    }

    @Test
    void combinesWithFiltersAndIgnoresBlank() {
        assertThat(titles(busan.getId(), "하루")).containsExactly("부산 하루 코스");
        assertThat(titles(null, "  ")).hasSize(3);
    }

    @Test
    void wildcardCharactersAreLiteral() {
        assertThat(titles(null, "100%")).containsExactly("서울 골목");
        assertThat(titles(null, "%")).containsExactly("서울 골목");
        assertThat(titles(null, "_")).isEmpty();
    }

    private List<String> titles(Long cityId, String keyword) {
        return postService.getPosts(null, cityId, null, null, keyword, PageRequest.of(0, 10))
                .getContent().stream().map(PostResponse::title).toList();
    }

    private void create(City city, String title, String content, String placeName) {
        Map<String, Object> place = Map.of("name", placeName, "latitude", 35.1, "longitude", 129.0);
        Map<String, Object> body = Map.of(
                "cityId", city.getId(),
                "subCategoryId", food.getId(),
                "title", title,
                "content", content,
                "places", List.of(place));
        postService.createPost(user.getId(), objectMapper.convertValue(body, CreatePostRequest.class));
    }
}
