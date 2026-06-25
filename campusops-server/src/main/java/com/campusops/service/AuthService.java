package com.campusops.service;

import com.campusops.vo.auth.CaptchaResponse;
import com.campusops.vo.auth.CurrentUserVO;
import com.campusops.vo.auth.LoginResponse;
import com.campusops.vo.auth.RegisterResponse;
import com.campusops.dto.auth.RegisterRequest;

public interface AuthService {

    LoginResponse login(String account, String rawPassword, String captchaId, String captchaCode);

    CurrentUserVO getCurrentUser(Long userId);

    CaptchaResponse getCaptcha();

    RegisterResponse register(RegisterRequest request);
}
