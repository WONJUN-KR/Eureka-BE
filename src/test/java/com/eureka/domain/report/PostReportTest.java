package com.eureka.domain.report;

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
import com.eureka.domain.report.repository.PostReportRepository;
import com.eureka.domain.user.entity.User;
import com.eureka.domain.user.repository.UserRepository;
import com.eureka.global.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 게시글 신고 - 한 사람당 한 번, 본인 글 불가, 3건 쌓이면 숨김
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostReportTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager em;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private PostReportRepository postReportRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SubCategoryRepository subCategoryRepository;

    private User writer;
    private City city;
    private SubCategory sub;
    private Post post;

    @BeforeEach
    void setUp() {
        writer = newUser("writer");
        Region region = regionRepository.save(Region.builder().name("강원도").build());
        city = cityRepository.save(City.builder().region(region).name("속초").build());
        Category category = categoryRepository.save(Category.builder().name("여행가자").type("TRAVEL").build());
        sub = subCategoryRepository.save(SubCategory.builder().category(category).name("바다로").build());

        post = Post.builder().user(writer).city(city).subCategory(sub).title("속초 코스").content("x").build();
        post.publish();
        postRepository.save(post);
    }

    private User newUser(String name) {
        return userRepository.save(User.builder().email(name + "@test.local").password("x").nickname(name).name("테스트").build());
    }

    private ResultActions report(User reporter, Long postId, String reason) throws Exception {
        return mockMvc.perform(post("/api/posts/{id}/reports", postId)
                .with(user(new CustomUserDetails(reporter.getId(), reporter.getEmail(), "USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"" + reason + "\"}"));
    }

    private String statusOf(Post p) {
        em.flush();
        em.clear();
        return postRepository.findById(p.getId()).orElseThrow().getStatus();
    }

    @Test
    void reportIsSaved() throws Exception {
        report(newUser("reporter"), post.getId(), "SPAM").andExpect(status().isOk());

        assertThat(postReportRepository.countByPostIdAndStatus(post.getId(), "PENDING")).isEqualTo(1);
        assertThat(statusOf(post)).isEqualTo("PUBLISHED");
    }

    @Test
    void sameUserCannotReportTwice() throws Exception {
        User reporter = newUser("reporter");
        report(reporter, post.getId(), "SPAM").andExpect(status().isOk());

        report(reporter, post.getId(), "ETC")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 신고한 게시글입니다"));
        assertThat(postReportRepository.countByPostIdAndStatus(post.getId(), "PENDING")).isEqualTo(1);
    }

    @Test
    void cannotReportOwnPost() throws Exception {
        report(writer, post.getId(), "SPAM")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("본인의 게시글은 신고할 수 없습니다"));
    }

    @Test
    void thirdReportHidesPostFromList() throws Exception {
        report(newUser("r1"), post.getId(), "SPAM").andExpect(status().isOk());
        report(newUser("r2"), post.getId(), "FALSE_INFO").andExpect(status().isOk());
        assertThat(statusOf(post)).isEqualTo("PUBLISHED");

        report(newUser("r3"), post.getId(), "INAPPROPRIATE").andExpect(status().isOk());
        assertThat(statusOf(post)).isEqualTo("HIDDEN");

        mockMvc.perform(get("/api/posts").param("keyword", "속초"))
                .andExpect(jsonPath("$.data.totalElements").value(0));
        // 상세도 다른 사람에게는 404, 글쓴이는 계속 볼 수 있음
        mockMvc.perform(get("/api/posts/{id}", post.getId())).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/posts/{id}", post.getId())
                        .with(user(new CustomUserDetails(writer.getId(), writer.getEmail(), "USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void onlyPublishedPostsCanBeReported() throws Exception {
        Post draft = postRepository.save(Post.builder().user(writer).city(city).subCategory(sub).title("임시").content("x").build());

        report(newUser("reporter"), draft.getId(), "SPAM").andExpect(status().isNotFound());
        report(newUser("reporter2"), 999_999L, "SPAM").andExpect(status().isNotFound());
    }

    @Test
    void unknownReasonIsBadRequest() throws Exception {
        report(newUser("reporter"), post.getId(), "WHATEVER").andExpect(status().isBadRequest());
        User other = newUser("other");
        mockMvc.perform(post("/api/posts/{id}/reports", post.getId())
                        .with(user(new CustomUserDetails(other.getId(), other.getEmail(), "USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void needsLogin() throws Exception {
        mockMvc.perform(post("/api/posts/{id}/reports", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"SPAM\"}"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(401, 403));
    }
}
