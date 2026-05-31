package com.luochen.chenoj.model.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 判题模式：标准 IO 或 Java 函数题（平台 Driver）。
 */
public enum JudgeModeEnum {

    STDIO("stdio"),
    FUNCTION_JAVA("function_java");

    private final String value;

    JudgeModeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isFunctionJava(String judgeMode) {
        return FUNCTION_JAVA.value.equals(StringUtils.trimToEmpty(judgeMode));
    }

    public static String normalize(String judgeMode) {
        if (isFunctionJava(judgeMode)) {
            return FUNCTION_JAVA.value;
        }
        return STDIO.value;
    }
}
