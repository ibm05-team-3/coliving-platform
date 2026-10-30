package com.coliving.platform.user.entity;

import com.coliving.platform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Builder
@ToString
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연락처. 비어 있을 수 있고 로그인 식별자로 쓰지 않는다.
    private String email;

    @Column(length = 100)
    private String name;

    // 커뮤니티와 헤더에 보여주는 이름. 비어 있을 수 있다.
    @Column(length = 100)
    private String nickname;

    private String phone;

    @Column(length = 511)
    private String address;

    @Column(length = 10)
    private String contractLanguage;

    // 활성화 여부 (비활성화 / 회원탈퇴)
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    // 계정 잠금 여부
    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    // 계정 역할. GUEST는 비로그인 방문자라 저장하지 않는다.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
