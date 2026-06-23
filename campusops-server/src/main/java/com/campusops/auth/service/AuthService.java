package com.campusops.auth.service;

import com.campusops.auth.vo.CurrentUserVO;
import com.campusops.auth.vo.LoginResponse;

public interface AuthService {

    LoginResponse login(String username, String rawPassword);

    CurrentUserVO getCurrentUser(Long userId);
}
