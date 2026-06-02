package com.luochen.chenoj.model.dto.question;

import lombok.Data;


/**
 * 题目判题配置（Java 方法题：methodName + paramTypes + JSON 用例）
 */
@Data
public class JudgeConfig {
    /**
     * 待测方法名，如 mergeTwoLists
     */
    private String methodName;

    /**
     * 参数类型列表，如 ListNode,int
     */
    private java.util.List<String> paramTypes;

    /**
     * 返回值类型，如 ListNode、int、int[]
     */
    private String returnType;

    /**
     * 时间限制（毫秒，存库与沙箱/判题统一；前端展示为秒）
     */
    private Long timeLimit;

    /**
     * 内存限制（KB，存库与沙箱/判题统一；前端展示为 MB）
     */
    private Long memoryLimit;

    /**
     * 堆栈限制（KB，已废弃未使用）
     */
    private Long stackLimit;
}
