package com.coliving.platform.user.entity;

import com.coliving.platform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@ToString
@Entity
@Table(name = "user_social_accounts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSocialAccountEntity extends BaseTimeEntity {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProviderType providerType;

    @Column(nullable = false)
    private String providerId;

    private String email;
}
