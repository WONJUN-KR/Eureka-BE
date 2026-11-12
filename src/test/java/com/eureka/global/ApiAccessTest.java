package com.eureka.global;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 비로그인 공개 범위와 잘못된 요청 응답 코드
@SpringBootTest
@AutoConfigureMockMvc
class ApiAccessTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void postReadIsPublic() throws Exception {
        mockMvc.perform(get("/api/posts")).andExpect(status().isOk());
        mockMvc.perform(get("/api/posts/{id}", 999_999)).andExpect(status().isNotFound());
    }

    @Test
    void postWriteNeedsLogin() throws Exception {
        mockMvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(401, 403));
        mockMvc.perform(patch("/api/posts/{id}", 1).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(401, 403));
        mockMvc.perform(delete("/api/posts/{id}", 1))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(401, 403));
    }

    @Test
    void malformedJsonIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/posts").with(user("tester"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cityId\": \"abc\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
