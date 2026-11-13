package com.eureka.domain.user.entity;

import com.eureka.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends BaseEntity {

    // 카카오로 가입한 계정의 내부용 이메일 도메인 - 일반 회원가입에서는 못 씀
    public static final String KAKAO_EMAIL_DOMAIN = "@kakao.eureka.local";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(nullable = false)
    private String name;

    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    private LocalDateTime deletedAt;

    // 가입 경로 - 이메일 가입은 LOCAL, 카카오 로그인은 KAKAO + 카카오 회원번호
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @ColumnDefault("'LOCAL'")
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(length = 64)
    private String providerId;

    public void updateProfile(String nickname, String name, LocalDate birth, Gender gender) {
        this.nickname = nickname;
        this.name = name;
        this.birth = birth;
        this.gender = gender;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.status = "INACTIVE";
        // 카카오 계정은 같은 카카오 회원이 다시 가입할 수 있게 회원번호 · 내부 이메일을 비워 둠
        if (this.provider == AuthProvider.KAKAO) {
            this.providerId = null;
            this.email = "deleted_" + this.id + KAKAO_EMAIL_DOMAIN;
        }
    }
}
