package com.campusops.service;

import com.campusops.dto.auth.RegisterRequest;
import com.campusops.service.impl.AuthServiceImpl;
import com.campusops.vo.auth.*;
import com.campusops.common.exception.BusinessException;
import com.campusops.security.jwt.JwtTokenProvider;
import com.campusops.entity.SysUserEntity;
import com.campusops.entity.SysRoleEntity;
import com.campusops.mapper.SysRoleMapper;
import com.campusops.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    // ---- 登录 ----

    @Test
    void login_validCredentials_shouldReturnToken() {
        when(captchaService.validateAndRemove("c1", "A7K2")).thenReturn(true);
        SysUserEntity user = userEntity(5L, "student001", "enabled", "{noop}123456");
        when(userQueryService.getByAccount("student001")).thenReturn(user);
        when(passwordEncoder.matches("123456", "{noop}123456")).thenReturn(true);
        when(userQueryService.getRoleCodesByUserId(5L)).thenReturn(List.of("normal_user"));
        when(jwtTokenProvider.generateToken(5L, "student001", List.of("normal_user"))).thenReturn("test-jwt");
        when(jwtTokenProvider.getExpirationSeconds()).thenReturn(7200L);
        when(sysUserMapper.updateById(any(SysUserEntity.class))).thenReturn(1);

        LoginResponse response = authService.login("student001", "123456", "c1", "A7K2");

        assertThat(response.getAccessToken()).isEqualTo("test-jwt");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void login_byEmail_shouldReturnToken() {
        when(captchaService.validateAndRemove("c1", "A7K2")).thenReturn(true);
        SysUserEntity user = userEntity(5L, "student001", "enabled", "{noop}123456");
        user.setEmail("student001@campusops.local");
        when(userQueryService.getByAccount("student001@campusops.local")).thenReturn(user);
        when(passwordEncoder.matches("123456", "{noop}123456")).thenReturn(true);
        when(userQueryService.getRoleCodesByUserId(5L)).thenReturn(List.of("normal_user"));
        when(jwtTokenProvider.generateToken(5L, "student001", List.of("normal_user"))).thenReturn("test-jwt");
        when(jwtTokenProvider.getExpirationSeconds()).thenReturn(7200L);
        when(sysUserMapper.updateById(any(SysUserEntity.class))).thenReturn(1);

        LoginResponse response = authService.login("student001@campusops.local", "123456", "c1", "A7K2");

        assertThat(response.getAccessToken()).isEqualTo("test-jwt");
    }

    @Test
    void login_byPhone_shouldReturnToken() {
        when(captchaService.validateAndRemove("c1", "A7K2")).thenReturn(true);
        SysUserEntity user = userEntity(5L, "student001", "enabled", "{noop}123456");
        user.setPhone("13800000005");
        when(userQueryService.getByAccount("13800000005")).thenReturn(user);
        when(passwordEncoder.matches("123456", "{noop}123456")).thenReturn(true);
        when(userQueryService.getRoleCodesByUserId(5L)).thenReturn(List.of("normal_user"));
        when(jwtTokenProvider.generateToken(5L, "student001", List.of("normal_user"))).thenReturn("test-jwt");
        when(jwtTokenProvider.getExpirationSeconds()).thenReturn(7200L);
        when(sysUserMapper.updateById(any(SysUserEntity.class))).thenReturn(1);

        LoginResponse response = authService.login("13800000005", "123456", "c1", "A7K2");

        assertThat(response.getAccessToken()).isEqualTo("test-jwt");
    }

    @Test
    void login_captchaInvalid_shouldThrow() {
        when(captchaService.validateAndRemove("bad", "XXXX")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("student001", "123456", "bad", "XXXX"))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("CAPTCHA_INVALID");
    }

    @Test
    void login_wrongPassword_shouldThrow() {
        when(captchaService.validateAndRemove("c1", "A7K2")).thenReturn(true);
        SysUserEntity user = userEntity(5L, "student001", "enabled", "{noop}123456");
        when(userQueryService.getByAccount("student001")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "{noop}123456")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("student001", "wrong", "c1", "A7K2"))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("AUTH_FAILED");
    }

    @Test
    void login_disabledUser_shouldThrow() {
        when(captchaService.validateAndRemove("c1", "A7K2")).thenReturn(true);
        SysUserEntity user = userEntity(5L, "student001", "disabled", "{noop}123456");
        when(userQueryService.getByAccount("student001")).thenReturn(user);

        assertThatThrownBy(() -> authService.login("student001", "123456", "c1", "A7K2"))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("ACCOUNT_DISABLED");
    }

    // ---- 注册 ----

    @Test
    void register_shouldCreateUser() {
        when(captchaService.validateAndRemove("c1", "A7K2")).thenReturn(true);
        when(userQueryService.getByUsername("student002")).thenReturn(null);
        when(sysUserMapper.selectByEmail("student002@campusops.local")).thenReturn(null);
        when(sysUserMapper.selectByPhone("13800000099")).thenReturn(null);
        when(sysRoleMapper.selectByRoleCode("normal_user")).thenReturn(roleEntity(1L, "normal_user"));
        when(passwordEncoder.encode("123456")).thenReturn("{bcrypt}encoded");
        when(sysUserMapper.insert(any(SysUserEntity.class))).thenAnswer(inv -> {
            SysUserEntity u = inv.getArgument(0);
            u.setId(7L);
            return 1;
        });
        when(sysRoleMapper.insertUserRole(7L, 1L)).thenReturn(1);

        RegisterRequest req = buildRegisterRequest("student002", "123456", "123456");
        RegisterResponse resp = authService.register(req);

        assertThat(resp.getId()).isEqualTo(7L);
        assertThat(resp.getUsername()).isEqualTo("student002");
        assertThat(resp.getRealName()).isEqualTo("王同学");

        // 验证密码被加密
        verify(passwordEncoder).encode("123456");
    }

    @Test
    void register_passwordMismatch_shouldThrow() {
        when(captchaService.validateAndRemove("c1", "A7K2")).thenReturn(true);

        RegisterRequest req = buildRegisterRequest("student002", "123456", "654321");
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("PASSWORD_CONFIRM_NOT_MATCH");
    }

    @Test
    void register_duplicateUsername_shouldThrow() {
        when(captchaService.validateAndRemove("c1", "A7K2")).thenReturn(true);
        when(userQueryService.getByUsername("student001")).thenReturn(new SysUserEntity());

        RegisterRequest req = buildRegisterRequest("student001", "123456", "123456");
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("USERNAME_EXISTS");
    }

    @Test
    void register_duplicateEmail_shouldThrow() {
        when(captchaService.validateAndRemove("c1", "A7K2")).thenReturn(true);
        when(userQueryService.getByUsername("student003")).thenReturn(null);
        when(sysUserMapper.selectByEmail("exist@test.local")).thenReturn(new SysUserEntity());

        RegisterRequest req = buildRegisterRequest("student003", "123456", "123456");
        req.setEmail("exist@test.local");
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("EMAIL_EXISTS");
    }

    @Test
    void register_duplicatePhone_shouldThrow() {
        when(captchaService.validateAndRemove("c1", "A7K2")).thenReturn(true);
        when(userQueryService.getByUsername("student003")).thenReturn(null);
        when(sysUserMapper.selectByEmail("student003@campusops.local")).thenReturn(null);
        when(sysUserMapper.selectByPhone("13800000001")).thenReturn(new SysUserEntity());

        RegisterRequest req = buildRegisterRequest("student003", "123456", "123456");
        req.setPhone("13800000001");
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("PHONE_EXISTS");
    }

    @Test
    void register_captchaInvalid_shouldNotWriteUser() {
        when(captchaService.validateAndRemove("bad", "XXXX")).thenReturn(false);

        RegisterRequest req = buildRegisterRequest("student003", "123456", "123456");
        req.setCaptchaId("bad");
        req.setCaptchaCode("XXXX");
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("CAPTCHA_INVALID");

        // 验证码失败不应写用户
        verify(sysUserMapper, org.mockito.Mockito.never()).insert(any(SysUserEntity.class));
    }

    @Test
    void register_roleNotFound_shouldThrow() {
        when(captchaService.validateAndRemove("c1", "A7K2")).thenReturn(true);
        when(userQueryService.getByUsername("student003")).thenReturn(null);
        when(sysUserMapper.selectByEmail("student003@campusops.local")).thenReturn(null);
        when(sysUserMapper.selectByPhone("13800000099")).thenReturn(null);
        when(sysRoleMapper.selectByRoleCode("normal_user")).thenReturn(null);

        RegisterRequest req = buildRegisterRequest("student003", "123456", "123456");
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("ROLE_NOT_FOUND");
    }

    // ---- 当前用户 ----

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

    // ---- helper ----

    private SysUserEntity userEntity(Long id, String username, String status, String passwordHash) {
        SysUserEntity user = new SysUserEntity();
        user.setId(id);
        user.setUsername(username);
        user.setStatus(status);
        user.setPasswordHash(passwordHash);
        return user;
    }

    private SysRoleEntity roleEntity(Long id, String roleCode) {
        SysRoleEntity role = new SysRoleEntity();
        role.setId(id);
        role.setRoleCode(roleCode);
        return role;
    }

    private RegisterRequest buildRegisterRequest(String username, String password, String confirmPassword) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setPassword(password);
        req.setConfirmPassword(confirmPassword);
        req.setRealName("王同学");
        req.setEmail(username + "@campusops.local");
        req.setPhone("13800000099");
        req.setCaptchaId("c1");
        req.setCaptchaCode("A7K2");
        return req;
    }
}