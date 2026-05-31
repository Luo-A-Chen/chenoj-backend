package com.luochen.chenoj.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户重置密码请求
 */
@Data
public class UserResetPasswordRequest implements Serializable {

    /**
     * 原密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认新密码
     */
    private String checkPassword;

    private static final long serialVersionUID = 1L;
}
