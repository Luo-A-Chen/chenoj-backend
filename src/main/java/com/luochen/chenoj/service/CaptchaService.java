package com.luochen.chenoj.service;

import com.luochen.chenoj.model.vo.CaptchaVO;

/**
 * 图形验证码服务
 */
public interface CaptchaService {

    /**
     * 生成图形验证码
     */
    CaptchaVO generateCaptcha();

    /**
     * 校验验证码（一次性，校验后删除）
     *
     * @param captchaKey  验证码标识
     * @param captchaCode 用户输入
     */
    void validateCaptcha(String captchaKey, String captchaCode);
}
