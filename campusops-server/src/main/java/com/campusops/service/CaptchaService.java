package com.campusops.service;

import com.campusops.vo.auth.CaptchaResponse;

/**
 * 图形验证码服务。当前阶段使用 JVM 内存存储，不引入 Redis。
 */
public interface CaptchaService {

    CaptchaResponse generate();

    boolean validateAndRemove(String captchaId, String code);
}