package com.luochen.chenoj.service.impl;

import static com.luochen.chenoj.constant.UserConstant.USER_LOGIN_STATE;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luochen.chenoj.common.ErrorCode;
import com.luochen.chenoj.constant.CommonConstant;
import com.luochen.chenoj.exception.BusinessException;
import com.luochen.chenoj.mapper.UserMapper;
import com.luochen.chenoj.model.dto.user.UserQueryRequest;
import com.luochen.chenoj.model.entity.User;
import com.luochen.chenoj.model.entity.UserAuthBind;
import com.luochen.chenoj.model.enums.UserRoleEnum;
import com.luochen.chenoj.model.vo.LoginUserVO;
import com.luochen.chenoj.model.vo.UserVO;
import com.luochen.chenoj.service.CaptchaService;
import com.luochen.chenoj.service.UserAuthBindService;
import com.luochen.chenoj.service.UserService;
import com.luochen.chenoj.utils.SqlUtils;
import com.luochen.chenoj.utils.UserNameUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import javax.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现
 *
 * @author <a href="https://github.com/luochen">程序员啊琛</a>
 * @from <a href="https://chenduoduo.icu">琛多多的oj网站</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "luochen";

    @Resource
    private UserAuthBindService userAuthBindService;

    @Resource
    private CaptchaService captchaService;

    @Override
    public long userRegister(String userAccount, String userName, String userPassword, String checkPassword,
                             String captchaKey, String captchaCode) {
        captchaService.validateCaptcha(captchaKey, captchaCode);
        userAccount = StringUtils.trim(userAccount);
        userName = StringUtils.trimToEmpty(userName);
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if (StringUtils.isBlank(userName)) {
            userName = userAccount;
        }
        UserNameUtils.validateUserName(userName);
        final String resolvedUserName = userName;
        synchronized (userAccount.intern()) {
            QueryWrapper<User> accountQuery = new QueryWrapper<>();
            accountQuery.eq("userAccount", userAccount);
            if (this.baseMapper.selectCount(accountQuery) > 0) {
                throw new BusinessException(ErrorCode.USER_ACCOUNT_EXIST);
            }
            QueryWrapper<User> nameQuery = new QueryWrapper<>();
            nameQuery.eq("userName", resolvedUserName);
            if (this.baseMapper.selectCount(nameQuery) > 0) {
                throw new BusinessException(ErrorCode.USER_NAME_DUPLICATE);
            }
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserName(resolvedUserName);
            user.setUserPassword(encryptPassword);
            user.setUserRole(UserRoleEnum.USER.getValue());
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword,
                                 String captchaKey, String captchaCode, HttpServletRequest request) {
        captchaService.validateCaptcha(captchaKey, captchaCode);
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密，加密后的哈希值是一样的
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        //mybatis-plus自动拼接sql并查询出对象
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            //抛出异常后，后面代码不执行了，异常往上走，被globalexceptionhanlder拦截
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        //把user对象存进session里，tomcat会自动写cookie给浏览器
        //session本质存储的是键值对
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        // 浏览器发送请求的时候自动带cookie，用cookie中的sessionid找到session
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 这是getsession不是拿session，而是从前端的cookie中拿取sessionid
        // 然后拿着sessionid去取session信息
        // user_login_state是一个key
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMyAccount(User loginUser, String password, HttpServletRequest request) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (StringUtils.isBlank(password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入密码确认注销");
        }
        String encryptedPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        if (!encryptedPassword.equals(loginUser.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        Long userId = loginUser.getId();
        QueryWrapper<UserAuthBind> bindQuery = new QueryWrapper<>();
        bindQuery.eq("userId", userId);
        userAuthBindService.remove(bindQuery);

        boolean removed = this.removeById(userId);
        if (!removed) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "注销失败");
        }
        userLogout(request);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}
