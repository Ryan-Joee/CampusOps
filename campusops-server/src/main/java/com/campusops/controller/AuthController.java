package com.campusops.controller;

import com.campusops.dto.auth.LoginRequest;
import com.campusops.dto.auth.RegisterRequest;
import com.campusops.service.AuthService;
import com.campusops.vo.auth.*;
import com.campusops.common.result.ApiResponse;
import com.campusops.common.result.ResultCode;
import com.campusops.security.jwt.LoginUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> captcha() {
        CaptchaResponse response = authService.getCaptcha();
        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(
                request.getAccount(), request.getPassword(),
                request.getCaptchaId(), request.getCaptchaCode());
        return ApiResponse.success(response);
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserVO> currentUser(@AuthenticationPrincipal LoginUserPrincipal principal) {
        if (principal == null) {
            return ApiResponse.error(ResultCode.UNAUTHORIZED);
        }
        CurrentUserVO vo = authService.getCurrentUser(principal.getUserId());
        return ApiResponse.success(vo);
    }
}
