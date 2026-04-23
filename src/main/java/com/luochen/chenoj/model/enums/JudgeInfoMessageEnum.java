package com.luochen.chenoj.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件上传业务类型枚举
 *
 * @author <a href="https://github.com/luochen">程序员啊琛</a>
 * @from <a href="https://chenduoduo.icu">琛多多的oj网站</a>
 */
public enum JudgeInfoMessageEnum {
    ACCEPTED("成功", "Accepted"),
    WRONG_ANSWER("答案错误", "WrongAnswer"),
    COMPILE_ERROR("编译错误", "CompileError"),
    RUNTIME_ERROR("运行错误", "RuntimeError"),
    SYSTEM_ERROR("系统错误", "SystemError"),
    DANGEROUS_OPERATION("危险操作", "DangrousOperation"),
    OUTPUT_LIMIT_EXCEEDED("输出溢出", "OutputLimitExceeded"),
    WAITING("等待中", "Waiting"),
    TIME_LIMIT_EXCEEDED("超时", "TimeLimitExceeded"),
    MEMORY_LIMIT_EXCEEDED("内存溢出", "MemoryLimitExceeded"),
    PRESENTATION_ERROR("展示错误", "PresentationError"),
    UNKNOWN("未知错误", "Unknown");


    private final String text;

    private final String value;

    JudgeInfoMessageEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static JudgeInfoMessageEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (JudgeInfoMessageEnum anEnum : JudgeInfoMessageEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
