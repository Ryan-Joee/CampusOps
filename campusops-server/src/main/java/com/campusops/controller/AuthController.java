package com.campusops.controller;

import com.campusops.dto.auth.LoginRequest;
import com.campusops.service.AuthService;
import com.campusops.vo.auth.CurrentUserVO;
import com.campusops.vo.auth.LoginResponse;
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
