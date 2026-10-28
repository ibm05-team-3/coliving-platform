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
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(@NonNull OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes;
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (SocialProviderType.NAVER.name().equalsIgnoreCase(registrationId)) {
            attributes = responseAttributes(oAuth2User);
        } else if (SocialProviderType.GOOGLE.name().equalsIgnoreCase(registrationId)) {
            attributes = oAuth2User.getAttributes();
        } else {
            throw new OAuth2AuthenticationException("지원하지 않은 소셜 로그인입니다.");
        }
        return loadLinkedUser(registrationId, attributes);
    }

    @Transactional
    public CustomOAuth2User loadLinkedUser(String registrationId, Map<String, Object> attributes) {
        SocialProviderType registration;
        String providerId;
        String registrationName = registrationId.toUpperCase();
        if (SocialProviderType.NAVER.name().equals(registrationName)) {
            registration = SocialProviderType.NAVER;
            providerId = requiredText(attributes, "id");
        } else if (SocialProviderType.GOOGLE.name().equals(registrationName)) {
            registration = SocialProviderType.GOOGLE;
            providerId = requiredText(attributes, "sub");
        } else {
            throw new OAuth2AuthenticationException("지원하지 않은 소셜 로그인입니다.");
        }

        String socialId = registrationName + "_" + providerId;
        return socialAccountRepository.findById(socialId)
                .map(account -> new CustomOAuth2User(attributes, account.getUser()))
                .orElseGet(() -> createSocialUser(socialId, registration, providerId, attributes));
    }

    private CustomOAuth2User createSocialUser(
            String socialId,
            SocialProviderType registration,
            String providerId,
            Map<String, Object> attributes
    ) {
        UserEntity.UserEntityBuilder userBuilder = UserEntity.builder()
                .roles(Set.of(Role.USER));
        optionalText(attributes, "name").ifPresent(userBuilder::name);
        optionalText(attributes, "email").ifPresent(userBuilder::email);
        optionalText(attributes, "mobile").ifPresent(userBuilder::phone);
        UserEntity user = userRepository.save(userBuilder.build());
        socialAccountRepository.save(UserSocialAccountEntity.builder()
                .id(socialId)
                .user(user)
                .providerType(registration)
                .providerId(providerId)
                .build());
        return new CustomOAuth2User(attributes, user);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> responseAttributes(OAuth2User oAuth2User) {
        Object response = oAuth2User.getAttributes().get("response");
        if (!(response instanceof Map<?, ?> attributes)) {
            throw new OAuth2AuthenticationException("네이버 프로필이 없습니다.");
        }
        return (Map<String, Object>) attributes;
    }

    private static Optional<String> optionalText(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value instanceof String text && !text.isBlank()) {
            return Optional.of(text);
        }
        return Optional.empty();
    }

    private static String requiredText(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new OAuth2AuthenticationException("소셜 프로필 식별자가 없습니다.");
        }
        return text;
    }
}