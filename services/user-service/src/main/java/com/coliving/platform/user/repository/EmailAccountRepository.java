package com.coliving.platform.user.repository;

import com.coliving.platform.user.entity.UserEmailAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailAccountRepository extends JpaRepository<UserEmailAccountEntity, Long> {
    Optional<UserEmailAccountEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
