package com.luochen.chenoj.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户注册请求体
 *
 * @author <a href="https://github.com/luochen">程序员啊琛</a>
 * @from <a href="https://chenduoduo.icu">琛多多的oj网站</a>
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    /**
     * 用户昵称；为空时默认与账号相同
     */
    private String userName;

    private String userPassword;

    private String checkPassword;

    /**
     * 图形验证码标识
     */
    private String captchaKey;

    /**
     * 用户输入的图形验证码
     */
    private String captchaCode;
}
