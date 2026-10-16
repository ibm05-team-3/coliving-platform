package com.coliving.platform.security.entity;

import com.coliving.platform.user.entity.Role;
import com.coliving.platform.user.entity.UserEntity;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final String sub;
    private final List<GrantedAuthority> authorities;
    private final String password;
    private final boolean enabled;
    private final boolean accountNonLocked;

    public CustomUserDetails(UserEntity entity) {
        this.sub = String.valueOf(entity.getId());
        this.password = entity.getPassword();
        this.enabled = entity.isEnabled();
        this.accountNonLocked = entity.isAccountNonLocked();
        this.authorities = toAuthorities(entity.getRoles());
    }

    public static final String ROLE = "ROLE_";

    public static String toAuthorityName(Role role) {
        return ROLE + role.name();
    }

    public static List<GrantedAuthority> toAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(toAuthorityName(role)))
                .toList();
    }

    @Override
    public @NonNull String getUsername() {
        return sub;
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // 소셜만 가입한 경우 null 가능
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}