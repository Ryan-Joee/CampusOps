package com.campusops.service;

import com.campusops.service.impl.AuthServiceImpl;
import com.campusops.vo.auth.CurrentUserVO;
import com.campusops.vo.auth.LoginResponse;
import com.campusops.common.exception.BusinessException;
import com.campusops.security.jwt.JwtTokenProvider;
import com.campusops.entity.SysUserEntity;
import com.campusops.service.UserQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_validCredentials_shouldReturnToken() {
        SysUserEntity user = userEntity(5L, "student001", "enabled", "{noop}123456");
        when(userQueryService.getByUsername("student001")).thenReturn(user);
        when(passwordEncoder.matches("123456", "{noop}123456")).thenReturn(true);
        when(userQueryService.getRoleCodesByUserId(5L)).thenReturn(List.of("normal_user"));
        when(jwtTokenProvider.generateToken(5L, "student001", List.of("normal_user")))
                .thenReturn("test-jwt-token");
        when(jwtTokenProvider.getExpirationSeconds()).thenReturn(7200L);

        LoginResponse response = authService.login("student001", "123456");

        assertThat(response.getAccessToken()).isEqualTo("test-jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(7200L);
    }

    @Test
    void login_wrongPassword_shouldThrow() {
        SysUserEntity user = userEntity(5L, "student001", "enabled", "{noop}123456");
        when(userQueryService.getByUsername("student001")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "{noop}123456")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("student001", "wrong"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名或密码错误")
                .extracting("code").isEqualTo("AUTH_FAILED");
    }

    @Test
    void login_disabledUser_shouldThrow() {
        SysUserEntity user = userEntity(5L, "student001", "disabled", "{noop}123456");
        when(userQueryService.getByUsername("student001")).thenReturn(user);

        assertThatThrownBy(() -> authService.login("student001", "123456"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账号已禁用")
                .extracting("code").isEqualTo("ACCOUNT_DISABLED");
    }

    @Test
    void login_userNotFound_shouldThrow() {
        when(userQueryService.getByUsername("unknown")).thenReturn(null);

        assertThatThrownBy(() -> authService.login("unknown", "any"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void getCurrentUser_shouldReturnRoles() {
        SysUserEntity user = userEntity(5L, "student001", "enabled", "{noop}123456");
        user.setRealName("张三");
        user.setEmail("student001@test.local");
        user.setPhone("13800000005");
        user.setDepartment("计算机学院");
        when(userQueryService.getEnabledUserById(5L)).thenReturn(user);
        when(userQueryService.getRoleCodesByUserId(5L)).thenReturn(List.of("normal_user"));

        CurrentUserVO vo = authService.getCurrentUser(5L);

        assertThat(vo.getId()).isEqualTo(5L);
        assertThat(vo.getUsername()).isEqualTo("student001");
        assertThat(vo.getRealName()).isEqualTo("张三");
        assertThat(vo.getRoles()).containsExactly("normal_user");
    }

    @Test
    void getCurrentUser_userNotFound_shouldThrow() {
        when(userQueryService.getEnabledUserById(anyLong())).thenReturn(null);

        assertThatThrownBy(() -> authService.getCurrentUser(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("TOKEN_INVALID");
    }

    private SysUserEntity userEntity(Long id, String username, String status, String passwordHash) {
        SysUserEntity user = new SysUserEntity();
        user.setId(id);
        user.setUsername(username);
        user.setStatus(status);
        user.setPasswordHash(passwordHash);
        return user;
    }
}
