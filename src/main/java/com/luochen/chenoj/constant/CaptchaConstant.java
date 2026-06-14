package com.luochen.chenoj.constant;

/**
 * 图形验证码常量
 */
public interface CaptchaConstant {

    String REDIS_KEY_PREFIX = "captcha:";

    /** 验证码有效期（分钟） */
    long EXPIRE_MINUTES = 5L;
}
