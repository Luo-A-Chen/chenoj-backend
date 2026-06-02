package com.luochen.chenoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luochen.chenoj.common.ErrorCode;
import com.luochen.chenoj.exception.BusinessException;
import com.luochen.chenoj.mapper.UserAuthBindMapper;
import com.luochen.chenoj.model.dto.user.UserAuthBindRequest;
import com.luochen.chenoj.model.entity.UserAuthBind;
import com.luochen.chenoj.model.vo.UserAuthBindVO;
import com.luochen.chenoj.service.UserAuthBindService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户账号绑定
 */
@Service
public class UserAuthBindServiceImpl extends ServiceImpl<UserAuthBindMapper, UserAuthBind>
        implements UserAuthBindService {

    private static final Set<String> ALLOWED_AUTH_TYPES = new HashSet<>(
            Arrays.asList("phone", "wechat", "weibo", "github"));

    @Override
    public List<UserAuthBindVO> listMyBinds(Long userId) {
        QueryWrapper<UserAuthBind> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.orderByAsc("authType");
        return this.list(queryWrapper).stream().map(this::toVo).collect(Collectors.toList());
    }

    @Override
    public boolean saveMyBind(Long userId, UserAuthBindRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String authType = StringUtils.trimToEmpty(request.getAuthType()).toLowerCase();
        String authId = StringUtils.trimToEmpty(request.getAuthId());
        String authName = StringUtils.trimToEmpty(request.getAuthName());
        if (StringUtils.isAnyBlank(authType, authId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "绑定类型或标识不能为空");
        }
        if (!ALLOWED_AUTH_TYPES.contains(authType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的绑定类型");
        }
        if ("phone".equals(authType) && !authId.matches("^1\\d{10}$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式不正确");
        }
        if (authId.length() > 256) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标识过长");
        }

        QueryWrapper<UserAuthBind> occupiedQuery = new QueryWrapper<>();
        occupiedQuery.eq("authType", authType);
        occupiedQuery.eq("authId", authId);
        occupiedQuery.ne("userId", userId);
        if (this.count(occupiedQuery) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账号已被其他用户绑定");
        }

        QueryWrapper<UserAuthBind> mineQuery = new QueryWrapper<>();
        mineQuery.eq("userId", userId);
        mineQuery.eq("authType", authType);
        UserAuthBind existing = this.getOne(mineQuery);

        if (existing != null) {
            existing.setAuthId(authId);
            existing.setAuthName(StringUtils.isNotBlank(authName) ? authName : authId);
            return this.updateById(existing);
        }

        UserAuthBind record = new UserAuthBind();
        record.setUserId(userId);
        record.setAuthType(authType);
        record.setAuthId(authId);
        record.setAuthName(StringUtils.isNotBlank(authName) ? authName : authId);
        return this.save(record);
    }

    @Override
    public boolean removeMyBind(Long userId, String authType) {
        String type = StringUtils.trimToEmpty(authType).toLowerCase();
        if (StringUtils.isBlank(type) || !ALLOWED_AUTH_TYPES.contains(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "绑定类型无效");
        }
        QueryWrapper<UserAuthBind> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("authType", type);
        return this.remove(queryWrapper);
    }

    private UserAuthBindVO toVo(UserAuthBind bind) {
        UserAuthBindVO vo = new UserAuthBindVO();
        BeanUtils.copyProperties(bind, vo);
        return vo;
    }
}
