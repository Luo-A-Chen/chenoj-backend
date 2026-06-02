package com.luochen.chenoj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luochen.chenoj.model.dto.user.UserAuthBindRequest;
import com.luochen.chenoj.model.entity.UserAuthBind;
import com.luochen.chenoj.model.vo.UserAuthBindVO;
import java.util.List;

/**
 * 用户账号绑定
 */
public interface UserAuthBindService extends IService<UserAuthBind> {

    List<UserAuthBindVO> listMyBinds(Long userId);

    boolean saveMyBind(Long userId, UserAuthBindRequest request);

    boolean removeMyBind(Long userId, String authType);
}
