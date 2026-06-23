package com.campusops.auth.controller;

import com.campusops.auth.dto.LoginRequest;
import com.campusops.auth.service.AuthService;
import com.campusops.auth.vo.CurrentUserVO;
import com.campusops.auth.vo.LoginResponse;
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

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getUsername(), request.getPassword());
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
