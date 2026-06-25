package com.campusops.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 用户注册请求 DTO。
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 64, message = "用户名长度4-64")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只允许字母、数字、下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度6-32")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "姓名不能为空")
    @Size(min = 1, max = 64, message = "姓名长度1-64")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 32, message = "手机号长度不超过32")
    private String phone;

    @Size(max = 128, message = "部门长度不超过128")
    private String department;

    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;

    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}