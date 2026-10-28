package com.coliving.platform.security.service;

import com.coliving.platform.user.entity.UserEmailAccountEntity;
import com.coliving.platform.user.entity.UserEntity;
import com.coliving.platform.user.repository.EmailAccountRepository;
import com.coliving.platform.user.repository.UserRepository;
import com.coliving.platform.security.entity.CustomUserDetails;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService  implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmailAccountRepository emailAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public @NonNull UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        UserEmailAccountEntity account = emailAccountRepository.findByEmail(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("이메일 또는 비밀번호가 올바르지 않습니다."));

        return new CustomUserDetails(account.getUser(), account.getPassword());
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(String accessSub) throws UsernameNotFoundException {
        long userId;
        try {
            userId = Long.parseLong(accessSub);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("아이디 혹은 비밀번호가 틀렸습니다.");
        }
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("아이디 혹은 비밀번호가 틀렸습니다."));
        String password = emailAccountRepository.findById(userId)
                .map(UserEmailAccountEntity::getPassword)
                .orElse(null);

        return new CustomUserDetails(user, password);
    }
}
