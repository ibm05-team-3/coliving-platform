package com.coliving.platform.security.service;

import com.coliving.platform.security.entity.CustomOidcUser;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();
    private final CustomOAuth2UserService customOAuth2UserService;

    public CustomOidcUserService(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Override
    public OidcUser loadUser(@NonNull OidcUserRequest userRequest) {
        OidcUser oidcUser = delegate.loadUser(userRequest);
        Map<String, Object> attributes = new HashMap<>(oidcUser.getClaims());
        OidcUserInfo userInfo = oidcUser.getUserInfo();
        if (userInfo != null) {
            attributes.putAll(userInfo.getClaims());
        }
        return new CustomOidcUser(
                customOAuth2UserService.loadLinkedUser(
                        userRequest.getClientRegistration().getRegistrationId(),
                        attributes),
                oidcUser);
    }
}
