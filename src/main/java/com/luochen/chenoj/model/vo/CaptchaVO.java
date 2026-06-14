package com.luochen.chenoj.model.vo;

import java.io.Serializable;
import lombok.Data;

/**
 * 图形验证码
 */
@Data
public class CaptchaVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 验证码唯一标识，提交登录/注册时回传
     */
    private String captchaKey;

    /**
     * Base64 图片（含 data:image/png;base64, 前缀）
     */
    private String captchaImage;
}
