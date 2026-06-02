package com.luochen.chenoj.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户账号绑定视图
 */
@Data
public class UserAuthBindVO implements Serializable {

    private String authType;

    private String authId;

    private String authName;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
