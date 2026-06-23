package com.campusops.security.jwt;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 从 JWT 解析出的登录用户主体，注入 Spring Security 上下文。
 * 用户存活状态已由 JwtAuthenticationFilter 校验，此处不再重复查库。
 */
@Getter
public class LoginUserPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final List<String> roles;

    public LoginUserPrincipal(Long userId, String username, List<String> roles) {
        this.userId = userId;
        this.username = username;
        this.roles = roles != null ? roles : Collections.emptyList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
