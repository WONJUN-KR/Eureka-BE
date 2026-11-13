package com.eureka.domain.like;

import com.eureka.domain.category.entity.Category;
import com.eureka.domain.category.entity.SubCategory;
import com.eureka.domain.category.repository.CategoryRepository;
import com.eureka.domain.category.repository.SubCategoryRepository;
import com.eureka.domain.like.service.LikeService;
import com.eureka.domain.post.entity.Post;
import com.eureka.domain.post.repository.PostRepository;
import com.eureka.domain.region.entity.City;
import com.eureka.domain.region.entity.Region;
import com.eureka.domain.region.repository.CityRepository;
import com.eureka.domain.region.repository.RegionRepository;
import com.eureka.domain.user.entity.User;
import com.eureka.domain.user.repository.UserRepository;
import com.eureka.global.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 내가 좋아요한 글 목록 · 프로필의 받은 좋아요 수 - 공개 글만 셈
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MyLikesTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private LikeService likeService;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SubCategoryRepository subCategoryRepository;

    private User writer;
    private User liker;
    private Post sea;
    private Post cafe;

    @BeforeEach
    void setUp() {
        writer = userRepository.save(User.builder().email("w@test.local").password("x").nickname("글쓴이").name("테스트").build());
        liker = userRepository.save(User.builder().email("l@test.local").password("x").nickname("좋아요러").name("테스트").build());
        Region region = regionRepository.save(Region.builder().name("부산").build());
        City city = cityRepository.save(City.builder().region(region).name("해운대").build());
        Category category = categoryRepository.save(Category.builder().name("여행가자").type("TRAVEL").build());
        SubCategory sub = subCategoryRepository.save(SubCategory.builder().category(category).name("바다로").build());

        sea = publish(Post.builder().user(writer).city(city).subCategory(sub).title("바다 코스").content("x").build());
        cafe = publish(Post.builder().user(writer).city(city).subCategory(sub).title("카페 코스").content("x").build());

        likeService.toggleLike(liker.getId(), sea.getId());
        likeService.toggleLike(liker.getId(), cafe.getId());
    }

    private Post publish(Post post) {
        post.publish();
        return postRepository.save(post);
    }

    private CustomUserDetails login(User u) {
        return new CustomUserDetails(u.getId(), u.getEmail(), "USER");
    }

    @Test
    void listsPostsILiked() throws Exception {
        mockMvc.perform(get("/api/users/me/likes").with(user(login(liker))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[*].title", containsInAnyOrder("바다 코스", "카페 코스")));

        mockMvc.perform(get("/api/users/me/likes").with(user(login(writer))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void newestLikeComesFirstAndSortParamIsIgnored() throws Exception {
        // 바다 -> 카페 순으로 눌렀으니 카페가 먼저
        mockMvc.perform(get("/api/users/me/likes").param("sort", "likeCount,desc").with(user(login(liker))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("카페 코스"))
                .andExpect(jsonPath("$.data.content[1].title").value("바다 코스"));
    }

    @Test
    void deletedPostIsLeftOut() throws Exception {
        sea.delete();
        postRepository.flush();

        mockMvc.perform(get("/api/users/me/likes").with(user(login(liker))))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("카페 코스"));
    }

    @Test
    void profileShowsLikesReceived() throws Exception {
        mockMvc.perform(get("/api/users/{id}", writer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeReceivedCount").value(2));

        mockMvc.perform(get("/api/users/{id}", liker.getId()))
                .andExpect(jsonPath("$.data.likeReceivedCount").value(0));
    }

    @Test
    void hiddenPostIsLeftOut() throws Exception {
        cafe.hide();
        postRepository.flush();

        mockMvc.perform(get("/api/users/me/likes").with(user(login(liker))))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("바다 코스"));
        mockMvc.perform(get("/api/users/{id}", writer.getId()))
                .andExpect(jsonPath("$.data.likeReceivedCount").value(1));
    }

    @Test
    void needsLogin() throws Exception {
        mockMvc.perform(get("/api/users/me/likes"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(401, 403));
    }
}
