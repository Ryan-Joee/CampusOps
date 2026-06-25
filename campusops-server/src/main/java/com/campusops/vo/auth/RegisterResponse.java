package com.campusops.vo.auth;

import lombok.Builder;
import lombok.Data;

/**
 * 注册响应 VO。
 */
@Data
@Builder
public class RegisterResponse {

    private Long id;
    private String username;
    private String realName;
}