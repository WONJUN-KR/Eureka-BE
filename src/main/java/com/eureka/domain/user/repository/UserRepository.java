package com.eureka.domain.user.repository;

import com.eureka.domain.user.entity.AuthProvider;
import com.eureka.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // 중복 확인은 탈퇴한 계정까지 봄 - DB 유니크 제약이 탈퇴 행에도 걸려 있어서
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM users WHERE email = :email", nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM users WHERE nickname = :nickname", nativeQuery = true)
    boolean existsByNickname(@Param("nickname") String nickname);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
