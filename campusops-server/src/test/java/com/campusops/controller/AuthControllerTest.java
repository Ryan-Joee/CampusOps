package com.campusops.controller;

import com.campusops.service.AuthService;
import com.campusops.vo.auth.CurrentUserVO;
import com.campusops.vo.auth.LoginResponse;
import com.campusops.security.jwt.JwtTokenProvider;
import com.campusops.entity.SysUserEntity;
import com.campusops.mapper.SysRoleMapper;
import com.campusops.mapper.SysUserMapper;
import com.campusops.service.UserQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private SysUserMapper sysUserMapper;

    @MockBean
    private SysRoleMapper sysRoleMapper;

    @Test
    void login_emptyUsername_shouldReturn400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"123456\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_emptyPassword_shouldReturn400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"student001\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_shouldReturn200() throws Exception {
        LoginResponse mockResponse = LoginResponse.builder()
                .accessToken("test-jwt")
                .tokenType("Bearer")
                .expiresIn(7200L)
                .build();
        when(authService.login("student001", "123456")).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"student001\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("test-jwt"));
    }

    @Test
    void me_noToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_validToken_shouldReturn200() throws Exception {
        // Use AuthService mock directly — the JWT filter will try to validate,
        // so we also mock userQueryService through the real bean chain
        SysUserEntity mockUser = new SysUserEntity();
        mockUser.setId(5L);
        mockUser.setUsername("student001");
        mockUser.setStatus("enabled");
        when(sysUserMapper.selectEnabledById(5L)).thenReturn(mockUser);
        when(sysRoleMapper.selectRoleCodesByUserId(5L)).thenReturn(List.of("normal_user"));

        CurrentUserVO mockVo = CurrentUserVO.builder()
                .id(5L).username("student001").realName("张三")
                .roles(List.of("normal_user")).build();
        when(authService.getCurrentUser(5L)).thenReturn(mockVo);

        // Generate a real JWT using the real JwtTokenProvider
        JwtTokenProvider realProvider = jwtTokenProvider();
        String token = realProvider.generateToken(5L, "student001", List.of("normal_user"));

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.username").value("student001"))
                .andExpect(jsonPath("$.data.roles[0]").value("normal_user"));
    }

    /**
     * Create a real JwtTokenProvider using the test application.yml config.
     */
    private JwtTokenProvider jwtTokenProvider() {
        com.campusops.security.jwt.JwtProperties props = new com.campusops.security.jwt.JwtProperties();
        props.setSecret("change-this-dev-secret-change-this-dev-secret");
        props.setExpirationSeconds(7200);
        return new JwtTokenProvider(props);
    }
}
