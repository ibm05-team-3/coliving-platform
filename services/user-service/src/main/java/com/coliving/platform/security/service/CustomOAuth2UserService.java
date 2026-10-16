package com.coliving.platform.security.service;

import com.coliving.platform.user.entity.Role;
import com.coliving.platform.user.entity.SocialProviderType;
import com.coliving.platform.user.entity.UserEntity;
import com.coliving.platform.user.entity.UserSocialAccountEntity;
import com.coliving.platform.user.repository.SocialAccountRepository;
import com.coliving.platform.user.repository.UserRepository;
import com.coliving.platform.security.entity.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(@NonNull OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        SocialProviderType registration;
        Map<String, Object> attributes;
        String providerId;
        String email;
        String nickname;

        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        if (SocialProviderType.NAVER.name().equals(registrationId)) {
            registration = SocialProviderType.NAVER;
            attributes = (Map<String, Object>) oAuth2User.getAttributes().get("response");
            providerId = attributes.get("id").toString();
            email = attributes.get("email").toString();
            nickname = attributes.get("nickname").toString();
        } else if (SocialProviderType.GOOGLE.name().equals(registrationId)) {
            registration = SocialProviderType.GOOGLE;
            attributes = oAuth2User.getAttributes();
            providerId = attributes.get("sub").toString();
            email = attributes.get("email").toString();
            nickname = attributes.get("name").toString();
        } else {
            throw new OAuth2AuthenticationException("지원하지 않은 소셜 로그인입니다.");
        }

        UserEntity user = findOrCreateMember(email, nickname);
        UserSocialAccountEntity socialAccount = UserSocialAccountEntity.builder()
                .id(registrationId + "_" + providerId)
                .user(user)
                .providerType(registration)
                .providerId(providerId)
                .email(email)
                .build();
        socialAccountRepository.save(socialAccount);
        return new CustomOAuth2User(attributes, user);
    }

    private UserEntity findOrCreateMember(String email, String nickname) {
        Optional<UserEntity> optionalMember = userRepository.findByEmail(email);
        if (optionalMember.isPresent()) {
            // 기존 유저
            return optionalMember.get();
        } else {
            // 신규 유저 추가
            UserEntity newMember = UserEntity
                    .builder()
                    .email(email)
                    .password(null)
                    .nickname(nickname)
                    .roles(Set.of(Role.USER))
                    .build();
            return userRepository.save(newMember);
        }
    }
}