package com.campusops.controller;

import com.campusops.dto.auth.RegisterRequest;
import com.campusops.service.AuthService;
import com.campusops.service.CaptchaService;
import com.campusops.vo.auth.*;
import com.campusops.common.exception.BusinessException;
import com.campusops.common.result.ResultCode;
import com.campusops.security.jwt.LoginUserPrincipal;
import com.campusops.dto.auth.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CaptchaService captchaService;

    @Test
    void captcha_shouldReturn200() throws Exception {
        CaptchaResponse resp = CaptchaResponse.builder()
                .captchaId("test-uuid").imageBase64("data:image/png;base64,xxx").expiresIn(120).build();
        when(authService.getCaptcha()).thenReturn(resp);

        mockMvc.perform(get("/auth/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.captchaId").value("test-uuid"));
    }

    @Test
    void login_emptyAccount_shouldReturn400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\":\"\",\"password\":\"123456\",\"captchaId\":\"c1\",\"captchaCode\":\"A7K2\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_emptyPassword_shouldReturn400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\":\"student001\",\"password\":\"\",\"captchaId\":\"c1\",\"captchaCode\":\"A7K2\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_shouldReturn200() throws Exception {
        LoginResponse mockResponse = LoginResponse.builder()
                .accessToken("test-jwt").tokenType("Bearer").expiresIn(7200L).build();
        when(authService.login(eq("student001"), eq("123456"), anyString(), anyString()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\":\"student001\",\"password\":\"123456\",\"captchaId\":\"c1\",\"captchaCode\":\"A7K2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("test-jwt"));
    }

    @Test
    void login_withEmail_shouldReturn200() throws Exception {
        LoginResponse mockResponse = LoginResponse.builder()
                .accessToken("test-jwt").tokenType("Bearer").expiresIn(7200L).build();
        when(authService.login(eq("student001@campusops.local"), eq("123456"), anyString(), anyString()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\":\"student001@campusops.local\",\"password\":\"123456\",\"captchaId\":\"c1\",\"captchaCode\":\"A7K2\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void login_captchaInvalid_shouldReturn200WithError() throws Exception {
        when(authService.login(eq("student001"), eq("123456"), anyString(), anyString()))
                .thenThrow(new BusinessException(ResultCode.CAPTCHA_INVALID, "验证码错误或已过期"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\":\"student001\",\"password\":\"123456\",\"captchaId\":\"bad\",\"captchaCode\":\"XXXX\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CAPTCHA_INVALID"));
    }

    @Test
    void register_valid_shouldReturn200() throws Exception {
        RegisterResponse resp = RegisterResponse.builder().id(7L).username("student002").realName("王同学").build();
        when(authService.register(any())).thenReturn(resp);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("student002");
        req.setPassword("123456");
        req.setConfirmPassword("123456");
        req.setRealName("王同学");
        req.setEmail("student002@campusops.local");
        req.setPhone("13800000007");
        req.setCaptchaId("c1");
        req.setCaptchaCode("A7K2");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("student002"));
    }

    @Test
    void register_emptyUsername_shouldReturn400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"123456\",\"confirmPassword\":\"123456\",\"realName\":\"王同学\",\"captchaId\":\"c1\",\"captchaCode\":\"A7K2\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordMismatch_shouldReturnError() throws Exception {
        when(authService.register(any()))
                .thenThrow(new BusinessException(ResultCode.PASSWORD_CONFIRM_NOT_MATCH, "两次密码不一致"));

        RegisterRequest req = new RegisterRequest();
        req.setUsername("student002");
        req.setPassword("123456");
        req.setConfirmPassword("654321");
        req.setRealName("王同学");
        req.setEmail("student002@campusops.local");
        req.setPhone("13800000007");
        req.setCaptchaId("c1");
        req.setCaptchaCode("A7K2");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PASSWORD_CONFIRM_NOT_MATCH"));
    }

    @Test
    void me_noToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}