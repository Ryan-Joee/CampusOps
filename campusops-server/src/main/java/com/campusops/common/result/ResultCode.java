package com.campusops.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS("SUCCESS", "OK"),

    BAD_REQUEST("BAD_REQUEST", "请求参数错误"),
    UNAUTHORIZED("UNAUTHORIZED", "未认证或登录已失效"),
    FORBIDDEN("FORBIDDEN", "无权访问"),
    NOT_FOUND("NOT_FOUND", "资源不存在"),

    AUTH_FAILED("AUTH_FAILED", "用户名或密码错误"),
    ACCOUNT_DISABLED("ACCOUNT_DISABLED", "账号已禁用"),
    TOKEN_INVALID("TOKEN_INVALID", "登录状态无效或已过期"),

    BUSINESS_ERROR("BUSINESS_ERROR", "业务处理失败"),
    INTERNAL_ERROR("INTERNAL_ERROR", "系统内部错误"),

    TICKET_NOT_FOUND("TICKET_NOT_FOUND", "工单不存在或无权访问"),
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "工单分类不存在"),
    INVALID_TICKET_PRIORITY("INVALID_TICKET_PRIORITY", "无效的工单优先级"),

    USERNAME_EXISTS("USERNAME_EXISTS", "用户名已存在"),
    EMAIL_EXISTS("EMAIL_EXISTS", "邮箱已被使用"),
    PHONE_EXISTS("PHONE_EXISTS", "手机号已被使用"),
    PASSWORD_CONFIRM_NOT_MATCH("PASSWORD_CONFIRM_NOT_MATCH", "两次密码不一致"),
    CAPTCHA_INVALID("CAPTCHA_INVALID", "验证码错误"),
    CAPTCHA_EXPIRED("CAPTCHA_EXPIRED", "验证码已过期"),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "系统角色配置异常");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
