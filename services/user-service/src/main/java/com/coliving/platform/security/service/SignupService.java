package com.coliving.platform.security.service;

import com.coliving.platform.user.entity.Role;
import com.coliving.platform.user.entity.UserEmailAccountEntity;
import com.coliving.platform.user.entity.UserEntity;
import com.coliving.platform.user.repository.EmailAccountRepository;
import com.coliving.platform.user.repository.UserRepository;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final UserRepository userRepository;
    private final EmailAccountRepository emailAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public void signup(String email, String password) {
        String normalized = normalizeEmail(email);
        if (!isEmail(normalized) || password == null || password.isBlank()) {
            throw new InvalidSignupException();
        }
        if (emailAccountRepository.existsByEmail(normalized)) {
            throw new EmailAlreadyUsedException();
        }

        UserEntity user = userRepository.save(UserEntity.builder()
                .email(normalized)
                .roles(Set.of(Role.defaultAccountRole()))
                .build());
        try {
            emailAccountRepository.saveAndFlush(UserEmailAccountEntity.builder()
                    .user(user)
                    .email(normalized)
                    .password(passwordEncoder.encode(password))
                    .build());
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyUsedException();
        }
    }

    private static boolean isEmail(String email) {
        int at = email.indexOf('@');
        return at > 0 && email.indexOf('.', at + 1) > at + 1 && !email.contains(" ");
    }

    public static class InvalidSignupException extends RuntimeException {
    }

    public static class EmailAlreadyUsedException extends RuntimeException {
    }
}
