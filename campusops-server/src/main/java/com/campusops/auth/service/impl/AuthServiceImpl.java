package com.campusops.auth.service.impl;

import com.campusops.auth.service.AuthService;
import com.campusops.auth.vo.CurrentUserVO;
import com.campusops.auth.vo.LoginResponse;
import com.campusops.common.exception.BusinessException;
import com.campusops.common.result.ResultCode;
import com.campusops.security.jwt.JwtTokenProvider;
import com.campusops.user.entity.SysUserEntity;
import com.campusops.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserQueryService userQueryService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(String username, String rawPassword) {
        SysUserEntity user = userQueryService.getByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.AUTH_FAILED, "用户名或密码错误");
        }
        if (!"enabled".equals(user.getStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED, "账号已禁用");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BusinessException(ResultCode.AUTH_FAILED, "用户名或密码错误");
        }

        List<String> roles = userQueryService.getRoleCodesByUserId(user.getId());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roles);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationSeconds())
                .build();
    }

    @Override
    public CurrentUserVO getCurrentUser(Long userId) {
        SysUserEntity user = userQueryService.getEnabledUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "登录状态无效或已过期");
        }
        List<String> roles = userQueryService.getRoleCodesByUserId(userId);

        return CurrentUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .roles(roles)
                .build();
    }
}
