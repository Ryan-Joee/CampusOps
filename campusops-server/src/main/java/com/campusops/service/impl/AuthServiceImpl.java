package com.campusops.service.impl;

import com.campusops.service.AuthService;
import com.campusops.vo.auth.CaptchaResponse;
import com.campusops.vo.auth.CurrentUserVO;
import com.campusops.vo.auth.LoginResponse;
import com.campusops.vo.auth.RegisterResponse;
import com.campusops.dto.auth.RegisterRequest;
import com.campusops.common.exception.BusinessException;
import com.campusops.common.result.ResultCode;
import com.campusops.security.jwt.JwtTokenProvider;
import com.campusops.entity.SysUserEntity;
import com.campusops.entity.SysRoleEntity;
import com.campusops.mapper.SysRoleMapper;
import com.campusops.mapper.SysUserMapper;
import com.campusops.service.CaptchaService;
import com.campusops.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserQueryService userQueryService;
    private final CaptchaService captchaService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public CaptchaResponse getCaptcha() {
        return captchaService.generate();
    }

    @Override
    public LoginResponse login(String account, String rawPassword, String captchaId, String captchaCode) {
        // 1. 校验验证码
        if (!captchaService.validateAndRemove(captchaId, captchaCode)) {
            log.warn("登录失败: 原因=验证码错误或已过期, captchaId={}", captchaId);
            throw new BusinessException(ResultCode.CAPTCHA_INVALID, "验证码错误或已过期");
        }

        // 2. 按 account 匹配用户名/手机号/邮箱
        SysUserEntity user = userQueryService.getByAccount(account);
        if (user == null) {
            log.warn("登录失败: 原因=账号不存在, account={}", account);
            throw new BusinessException(ResultCode.AUTH_FAILED, "用户名或密码错误");
        }
        if (!"enabled".equals(user.getStatus())) {
            log.warn("登录失败: 原因=账号禁用, userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED, "账号已禁用");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("登录失败: 原因=密码错误, userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException(ResultCode.AUTH_FAILED, "用户名或密码错误");
        }

        List<String> roles = userQueryService.getRoleCodesByUserId(user.getId());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roles);

        // 更新最近登录时间
        user.setLastLoginAt(LocalDateTime.now());
        sysUserMapper.updateById(user);

        log.info("登录成功: userId={}, username={}, roleCount={}", user.getId(), user.getUsername(), roles.size());

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
            log.warn("获取当前用户失败: 原因=用户不存在或已禁用, userId={}, code={}", userId, ResultCode.TOKEN_INVALID);
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

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 1. 校验验证码
        if (!captchaService.validateAndRemove(request.getCaptchaId(), request.getCaptchaCode())) {
            log.warn("注册失败: 原因=验证码错误或已过期, captchaId={}", request.getCaptchaId());
            throw new BusinessException(ResultCode.CAPTCHA_INVALID, "验证码错误或已过期");
        }

        // 2. 校验两次密码一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("注册失败: 原因=两次密码不一致, username={}", request.getUsername());
            throw new BusinessException(ResultCode.PASSWORD_CONFIRM_NOT_MATCH, "两次密码不一致");
        }

        // 3. 用户名唯一
        if (userQueryService.getByUsername(request.getUsername()) != null) {
            log.warn("注册失败: 原因=用户名已存在, username={}", request.getUsername());
            throw new BusinessException(ResultCode.USERNAME_EXISTS, "用户名已存在");
        }

        // 4. 邮箱唯一
        if (sysUserMapper.selectByEmail(request.getEmail().trim()) != null) {
            log.warn("注册失败: 原因=邮箱已被使用, username={}", request.getUsername());
            throw new BusinessException(ResultCode.EMAIL_EXISTS, "邮箱已被使用");
        }

        // 5. 手机号唯一
        if (sysUserMapper.selectByPhone(request.getPhone().trim()) != null) {
            log.warn("注册失败: 原因=手机号已被使用, username={}", request.getUsername());
            throw new BusinessException(ResultCode.PHONE_EXISTS, "手机号已被使用");
        }

        // 6. 确认 normal_user 角色存在
        SysRoleEntity normalRole = sysRoleMapper.selectByRoleCode("normal_user");
        if (normalRole == null) {
            log.warn("注册失败: 原因=系统角色配置异常, roleCode=normal_user");
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND, "系统角色配置异常");
        }

        // 7. 创建用户
        SysUserEntity user = new SysUserEntity();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail().trim());
        user.setPhone(request.getPhone().trim());
        user.setDepartment(request.getDepartment());
        user.setStatus("enabled");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.insert(user);

        // 8. 绑定角色
        sysRoleMapper.insertUserRole(user.getId(), normalRole.getId());

        log.info("注册成功: userId={}, username={}, phone={}, email={}",
                user.getId(), user.getUsername(), user.getPhone(), user.getEmail());

        return RegisterResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .build();
    }
}
