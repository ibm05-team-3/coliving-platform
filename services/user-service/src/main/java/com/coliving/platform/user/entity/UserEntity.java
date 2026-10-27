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

    @Column(nullable = false, unique = true)
    private String email;

    // 소셜만 있으면 null
    @ToString.Exclude
    @Column
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

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
