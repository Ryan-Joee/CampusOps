package com.campusops.vo.auth;

import lombok.Builder;
import lombok.Data;

/**
 * 验证码响应 VO。
 */
@Data
@Builder
public class CaptchaResponse {

    private String captchaId;
    private String imageBase64;
    private int expiresIn;
}