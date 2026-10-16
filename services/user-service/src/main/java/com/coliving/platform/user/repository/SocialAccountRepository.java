package com.coliving.platform.user.repository;

import com.coliving.platform.user.entity.UserSocialAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialAccountRepository extends JpaRepository<UserSocialAccountEntity, String> {
}
