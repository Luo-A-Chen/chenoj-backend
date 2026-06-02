package com.luochen.chenoj.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 解除账号绑定
 */
@Data
public class UserAuthUnbindRequest implements Serializable {

    private String authType;

    private static final long serialVersionUID = 1L;
}
