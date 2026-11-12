package com.eureka.domain.user;

import com.eureka.domain.category.entity.Category;
import com.eureka.domain.category.entity.SubCategory;
import com.eureka.domain.category.repository.CategoryRepository;
import com.eureka.domain.category.repository.SubCategoryRepository;
import com.eureka.domain.post.entity.Post;
import com.eureka.domain.post.repository.PostRepository;
import com.eureka.domain.region.entity.City;
import com.eureka.domain.region.entity.Region;
import com.eureka.domain.region.repository.CityRepository;
import com.eureka.domain.region.repository.RegionRepository;
import com.eureka.domain.user.entity.User;
import com.eureka.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 다른 사람 프로필 · 글 목록 - 비로그인 공개, 공개 글만, 개인 정보 제외
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserPublicProfileTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SubCategoryRepository subCategoryRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder().email("p@test.local").password("x").nickname("공개테스터").name("테스트").build());
        Region region = regionRepository.save(Region.builder().name("강원도").build());
        City city = cityRepository.save(City.builder().region(region).name("강릉").build());
        Category category = categoryRepository.save(Category.builder().name("여행가자").type("TRAVEL").build());
        SubCategory sea = subCategoryRepository.save(SubCategory.builder().category(category).name("바다로").build());

        Post published = Post.builder().user(user).city(city).subCategory(sea).title("공개 글").content("x").build();
        published.publish();
        postRepository.save(published);
        postRepository.save(Post.builder().user(user).city(city).subCategory(sea).title("임시 글").content("x").build());
    }

    @Test
    void profileIsPublicWithoutPersonalInfo() throws Exception {
        mockMvc.perform(get("/api/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("공개테스터"))
                .andExpect(jsonPath("$.data.postCount").value(1))
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.name").doesNotExist());
    }

    @Test
    void userPostsShowOnlyPublished() throws Exception {
        mockMvc.perform(get("/api/users/{id}/posts", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("공개 글"));
    }

    @Test
    void unknownUserIsNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999_999)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/users/{id}/posts", 999_999)).andExpect(status().isNotFound());
    }

    @Test
    void meStillNeedsLogin() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(401, 403));
    }
}
