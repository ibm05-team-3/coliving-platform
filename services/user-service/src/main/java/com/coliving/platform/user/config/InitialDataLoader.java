package com.coliving.platform.user.config;

import com.coliving.platform.user.entity.Role;
import com.coliving.platform.user.entity.UserEmailAccountEntity;
import com.coliving.platform.user.entity.UserEntity;
import com.coliving.platform.user.repository.EmailAccountRepository;
import com.coliving.platform.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class InitialDataLoader implements CommandLineRunner {
    private final UserRepository memberRepository;
    private final EmailAccountRepository emailAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoader(UserRepository memberRepository,
                              EmailAccountRepository emailAccountRepository,
                              PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.emailAccountRepository = emailAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (emailAccountRepository.findByEmail("admin@naver.com").isPresent()) {
            return;
        }
        UserEntity member = memberRepository.save(UserEntity.builder()
                .email("admin@naver.com")
                .roles(Set.of(Role.ADMIN))
                .build());
        emailAccountRepository.save(UserEmailAccountEntity.builder()
                .user(member)
                .email("admin@naver.com")
                .password(passwordEncoder.encode("12341234"))
                .build());
    }
}
