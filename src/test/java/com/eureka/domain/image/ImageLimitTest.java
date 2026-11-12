package com.eureka.domain.image;

import com.eureka.domain.category.entity.Category;
import com.eureka.domain.category.entity.SubCategory;
import com.eureka.domain.category.repository.CategoryRepository;
import com.eureka.domain.category.repository.SubCategoryRepository;
import com.eureka.domain.image.exception.ImageException;
import com.eureka.domain.image.service.ImageService;
import com.eureka.domain.image.storage.ImageStorage;
import com.eureka.domain.place.exception.PlaceException;
import com.eureka.domain.post.dto.CreatePostRequest;
import com.eureka.domain.post.dto.PostDetailResponse;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

// 사진 한도 - 장소 없는 사진 10장과 장소당 3장은 따로 셈 (H2, 저장소는 목)
@SpringBootTest
@Transactional
class ImageLimitTest {

    @Autowired private ImageService imageService;
    @Autowired private PostService postService;
    @Autowired private UserRepository userRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SubCategoryRepository subCategoryRepository;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ImageStorage imageStorage;

    private User user;
    private PostDetailResponse post;

    @BeforeEach
    void setUp() {
        given(imageStorage.store(any(), anyString())).willReturn("/uploads/test.jpg");
        user = userRepository.save(User.builder().email("i@test.local").password("x").nickname("사진").name("사진").build());
        Region region = regionRepository.save(Region.builder().name("전국").build());
        City city = cityRepository.save(City.builder().region(region).name("부산").build());
        Category category = categoryRepository.save(Category.builder().name("맛집가자").type("FOOD").build());
        SubCategory food = subCategoryRepository.save(SubCategory.builder().category(category).name("한식").build());
        List<Map<String, Object>> places = IntStream.rangeClosed(1, 20)
                .mapToObj(i -> Map.<String, Object>of("name", "장소" + i, "latitude", 35.1, "longitude", 129.0))
                .toList();
        Map<String, Object> body = Map.of("cityId", city.getId(), "subCategoryId", food.getId(),
                "title", "부산", "content", "본문", "places", places);
        post = postService.createPost(user.getId(), objectMapper.convertValue(body, CreatePostRequest.class));
    }

    @Test
    void everyPlaceCanHoldThreePhotos() {
        for (int i = 0; i < 10; i++) {
            upload(null);
        }
        for (var place : post.places()) {
            for (int i = 0; i < 3; i++) {
                assertThatCode(() -> upload(place.id())).doesNotThrowAnyException();
            }
        }
    }

    @Test
    void limitsArePerKind() {
        for (int i = 0; i < 10; i++) {
            upload(null);
        }
        assertThatThrownBy(() -> upload(null)).isInstanceOf(ImageException.class);

        Long placeId = post.places().get(0).id();
        for (int i = 0; i < 3; i++) {
            upload(placeId);
        }
        assertThatThrownBy(() -> upload(placeId)).isInstanceOf(PlaceException.class);
    }

    private void upload(Long placeId) {
        imageService.uploadImage(user.getId(), post.id(), placeId,
                new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1, 2, 3}));
    }
}
