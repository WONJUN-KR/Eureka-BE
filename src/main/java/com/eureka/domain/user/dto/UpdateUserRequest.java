package com.eureka.domain.user.dto;

import com.eureka.domain.user.entity.Gender;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UpdateUserRequest {

    @Size(min = 2, max = 20, message = "닉네임은 2-20자여야 합니다")
    private String nickname;

    @Size(min = 2, max = 20, message = "이름은 2-20자여야 합니다")
    private String name;

    private LocalDate birth;

    private Gender gender;
}
