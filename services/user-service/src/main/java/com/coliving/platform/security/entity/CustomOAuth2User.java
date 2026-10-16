package com.coliving.platform.security.entity;

import com.coliving.platform.user.entity.UserEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@NullMarked
public class CustomOAuth2User implements OAuth2User {

    private final String sub;
    private final List<GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(Map<String, Object> attributes, UserEntity user) {
        this.sub = String.valueOf(user.getId());
        this.attributes = Map.copyOf(attributes);
        this.authorities = CustomUserDetails.toAuthorities(user.getRoles());
    }

    @Override
    public String getName() {
        return sub;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
