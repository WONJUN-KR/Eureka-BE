package com.eureka.domain.place;

import com.eureka.domain.category.entity.Category;
import com.eureka.domain.category.entity.SubCategory;
import com.eureka.domain.category.repository.CategoryRepository;
import com.eureka.domain.category.repository.SubCategoryRepository;
import com.eureka.domain.image.entity.PostImage;
import com.eureka.domain.image.repository.PostImageRepository;
import com.eureka.domain.place.dto.PlaceResponse;
import com.eureka.domain.place.exception.PlaceException;
import com.eureka.domain.place.repository.PostPlaceRepository;
import com.eureka.domain.post.dto.CreatePostRequest;
import com.eureka.domain.post.dto.PostDetailResponse;
import com.eureka.domain.post.dto.PostResponse;
import com.eureka.domain.post.dto.UpdatePostRequest;
import com.eureka.domain.post.entity.Post;
import com.eureka.domain.post.repository.PostRepository;
import com.eureka.domain.post.service.PostService;
import com.eureka.domain.region.entity.City;
import com.eureka.domain.region.entity.Region;
import com.eureka.domain.region.repository.CityRepository;
import com.eureka.domain.region.repository.RegionRepository;
import com.eureka.domain.user.entity.User;
import com.eureka.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 장소가 들어간 게시글 작성 · 수정 · 조회 흐름 (H2)
@SpringBootTest
@Transactional
class PostPlaceFlowTest {

    @Autowired private PostService postService;
    @Autowired private PostRepository postRepository;
    @Autowired private PostPlaceRepository postPlaceRepository;
    @Autowired private PostImageRepository postImageRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SubCategoryRepository subCategoryRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EntityManager em;

    private User user;
    private City city;
    private SubCategory food;
    private SubCategory cafe;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder().email("a@test.local").password("x").nickname("테스터").name("테스트").build());
        Region region = regionRepository.save(Region.builder().name("경상도/부산").build());
        city = cityRepository.save(City.builder().region(region).name("부산").build());
        Category category = categoryRepository.save(Category.builder().name("맛집가자").type("FOOD").build());
        food = subCategoryRepository.save(SubCategory.builder().category(category).name("한식").build());
        cafe = subCategoryRepository.save(SubCategory.builder().category(category).name("카페").build());
    }

    @Test
    void createKeepsPlaceOrderAndFields() throws Exception {
        PostDetailResponse created = postService.createPost(user.getId(), createRequest(List.of(
                place(null, "감천문화마을", null, "10:00", "포토존은 줄이 있어요"),
                place(null, "돼지국밥집", food.getId(), "12:30", null),
                place(null, "영도 카페", cafe.getId(), null, null))));

        List<PlaceResponse> places = created.places();
        assertThat(places).extracting(PlaceResponse::name).containsExactly("감천문화마을", "돼지국밥집", "영도 카페");
        assertThat(places).extracting(PlaceResponse::seq).containsExactly(1, 2, 3);
        assertThat(places.get(0).visitedAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(places.get(0).memo()).isEqualTo("포토존은 줄이 있어요");
        assertThat(places.get(1).subCategoryName()).isEqualTo("한식");
        assertThat(places.get(2).visitedAt()).isNull();
    }

    @Test
    void createWithoutPlacesFails() throws Exception {
        assertThatThrownBy(() -> postService.createPost(user.getId(), createRequest(List.of())))
                .isInstanceOf(PlaceException.class);
    }

    @Test
    void updateReordersAddsAndRemovesPlaces() throws Exception {
        PostDetailResponse created = postService.createPost(user.getId(), createRequest(List.of(
                place(null, "A", null, null, null),
                place(null, "B", null, null, null),
                place(null, "C", null, null, null))));
        Long postId = created.id();
        Long idA = created.places().get(0).id();
        Long idC = created.places().get(2).id();

        // A 장소에 사진이 붙어 있는 상태에서 A 를 빼면 사진은 게시글 사진으로 남아야 함
        Post post = postRepository.findById(postId).orElseThrow();
        PostImage photo = postImageRepository.save(PostImage.builder()
                .post(post).place(postPlaceRepository.findById(idA).orElseThrow()).url("/uploads/a.jpg").seq(1).build());

        // C 를 맨 앞으로, B 는 빼고, 새 장소 D 추가
        postService.updatePost(user.getId(), postId, updateRequest(List.of(
                place(idC, "C", null, null, "먼저 들름"),
                place(null, "D", cafe.getId(), "15:00", null))));
        em.flush();
        em.clear();

        List<PlaceResponse> places = postService.getPost(postId).places();
        assertThat(places).extracting(PlaceResponse::name).containsExactly("C", "D");
        assertThat(places).extracting(PlaceResponse::seq).containsExactly(1, 2);
        assertThat(places.get(0).id()).isEqualTo(idC);
        assertThat(places.get(0).memo()).isEqualTo("먼저 들름");
        assertThat(postPlaceRepository.findById(idA)).isEmpty();

        PostImage kept = postImageRepository.findById(photo.getId()).orElseThrow();
        assertThat(kept.getPlace()).isNull();
    }

    @Test
    void updateWithPlaceOfAnotherPostFails() throws Exception {
        PostDetailResponse mine = postService.createPost(user.getId(), createRequest(List.of(place(null, "A", null, null, null))));
        PostDetailResponse other = postService.createPost(user.getId(), createRequest(List.of(place(null, "B", null, null, null))));

        assertThatThrownBy(() -> postService.updatePost(user.getId(), mine.id(),
                updateRequest(List.of(place(other.places().get(0).id(), "B", null, null, null)))))
                .isInstanceOf(PlaceException.class);
    }

    @Test
    void userPostsIncludePlacePoints() throws Exception {
        postService.createPost(user.getId(), createRequest(List.of(
                place(null, "A", null, null, null),
                place(null, "B", null, null, null))));
        postRepository.save(Post.builder().user(user).city(city).subCategory(food).title("임시").content("x").build());

        List<PostResponse> posts = postService.getUserPosts(user.getId(), PageRequest.of(0, 10)).getContent();
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).places()).extracting(p -> p.name()).containsExactly("A", "B");
    }

    private Map<String, Object> place(Long id, String name, Long subCategoryId, String visitedAt, String memo) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("latitude", 35.0975);
        m.put("longitude", 129.0106);
        m.put("subCategoryId", subCategoryId);
        m.put("visitedAt", visitedAt);
        m.put("memo", memo);
        return m;
    }

    private CreatePostRequest createRequest(List<Map<String, Object>> places) throws Exception {
        Map<String, Object> body = Map.of(
                "cityId", city.getId(),
                "subCategoryId", food.getId(),
                "title", "부산 하루",
                "content", "본문",
                "places", places);
        return objectMapper.convertValue(body, CreatePostRequest.class);
    }

    private UpdatePostRequest updateRequest(List<Map<String, Object>> places) {
        return objectMapper.convertValue(Map.of("places", places), UpdatePostRequest.class);
    }
}
