package com.luochen.chenoj.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 当前用户注销账号请求
 */
@Data
public class UserDeleteMyRequest implements Serializable {

    /**
     * 当前登录密码（用于确认身份）
     */
    private String password;

    private static final long serialVersionUID = 1L;
}
