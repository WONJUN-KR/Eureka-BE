package com.eureka.domain.auth.dto;

import com.eureka.domain.user.entity.Gender;
import com.eureka.domain.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SignupRequest(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8)
        String password,

        @NotBlank @Size(min = 2, max = 20)
        String nickname,

        @NotBlank @Size(min = 2, max = 20)
        String name,

        LocalDate birth,

        Gender gender
) {
    public User toEntity(String encodedPassword) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .name(name)
                .birth(birth)
                .gender(gender)
                .build();
    }
}
