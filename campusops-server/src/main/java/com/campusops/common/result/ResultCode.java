package com.campusops.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS("SUCCESS", "OK"),

    BAD_REQUEST("BAD_REQUEST", "请求参数错误"),
    UNAUTHORIZED("UNAUTHORIZED", "未认证或登录已失效"),
    FORBIDDEN("FORBIDDEN", "无权访问"),
    NOT_FOUND("NOT_FOUND", "资源不存在"),

    BUSINESS_ERROR("BUSINESS_ERROR", "业务处理失败"),
    INTERNAL_ERROR("INTERNAL_ERROR", "系统内部错误");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
