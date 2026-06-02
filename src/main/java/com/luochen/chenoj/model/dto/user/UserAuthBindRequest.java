package com.luochen.chenoj.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 保存账号绑定（仅记录数据，后续可接 OAuth）
 */
@Data
public class UserAuthBindRequest implements Serializable {

    /**
     * phone / wechat / weibo / github
     */
    private String authType;

    /**
     * 平台唯一标识或手机号
     */
    private String authId;

    /**
     * 展示名（可选）
     */
    private String authName;

    private static final long serialVersionUID = 1L;
}
