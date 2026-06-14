package com.luochen.chenoj.utils;

import com.luochen.chenoj.common.ErrorCode;
import com.luochen.chenoj.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 用户名校验：仅允许中文、字母、数字与下划线。
 */
public final class UserNameUtils {

    private static final int MAX_LENGTH = 20;

    private static final Pattern USER_NAME_PATTERN =
            Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$");

    private UserNameUtils() {
    }

    public static boolean isValidFormat(String userName) {
        if (StringUtils.isBlank(userName)) {
            return false;
        }
        String trimmed = userName.trim();
        return trimmed.length() <= MAX_LENGTH && USER_NAME_PATTERN.matcher(trimmed).matches();
    }

    /**
     * 校验用户名格式，非法时抛出业务异常。
     */
    public static void validateUserName(String userName) {
        if (StringUtils.isBlank(userName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名不能为空");
        }
        String trimmed = userName.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名过长");
        }
        if (!USER_NAME_PATTERN.matcher(trimmed).matches()) {
            throw new BusinessException(ErrorCode.INVALID_USER_NAME);
        }
    }
}
