package com.eureka.domain.user.service;

import com.eureka.domain.user.dto.UpdateUserRequest;
import com.eureka.domain.user.dto.UserResponse;
import com.eureka.domain.user.entity.User;
import com.eureka.domain.user.exception.UserException;
import com.eureka.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException::notFound);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMyProfile(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException::notFound);

        // 닉네임 변경 시 중복 체크 (값이 있고, 본인 닉네임과 다른 경우만)
        if (request.getNickname() != null
                && !request.getNickname().equals(user.getNickname())
                && userRepository.existsByNickname(request.getNickname())) {
            throw UserException.duplicateNickname();
        }

        // null인 필드는 기존 값 유지
        user.updateProfile(
                request.getNickname() != null ? request.getNickname() : user.getNickname(),
                request.getName() != null ? request.getName() : user.getName(),
                request.getBirth() != null ? request.getBirth() : user.getBirth(),
                request.getGender() != null ? request.getGender() : user.getGender()
        );

        return UserResponse.from(user);
    }

    @Transactional
    public void deleteMyAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException::notFound);
        user.delete();
    }
}
