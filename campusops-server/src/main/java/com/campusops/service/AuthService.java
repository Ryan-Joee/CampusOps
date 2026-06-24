package com.campusops.service;

import com.campusops.vo.auth.CurrentUserVO;
import com.campusops.vo.auth.LoginResponse;

public interface AuthService {

    LoginResponse login(String username, String rawPassword);

    CurrentUserVO getCurrentUser(Long userId);
}
