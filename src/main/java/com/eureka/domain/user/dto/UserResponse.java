package com.eureka.domain.user.dto;

import com.eureka.domain.user.entity.Gender;
import com.eureka.domain.user.entity.User;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String name,
        LocalDate birth,
        Gender gender
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                user.getBirth(),
                user.getGender()
        );
    }
}
