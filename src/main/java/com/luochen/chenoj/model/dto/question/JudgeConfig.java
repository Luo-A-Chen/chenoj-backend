package com.luochen.chenoj.model.dto.question;

import lombok.Data;


/**
 * 题目信息
 */
@Data
public class JudgeConfig {
    /**
     * 判题模式：stdio（只写 Solution.run 读写 stdin/stdout）| function_java（只写 Solution 方法，JSON 用例）
     */
    private String judgeMode;

    /**
     * function_java：待测方法名，如 mergeTwoLists
     */
    private String methodName;

    /**
     * function_java：参数类型列表，如 ListNode,int
     */
    private java.util.List<String> paramTypes;

    /**
     * function_java：返回值类型，如 ListNode、int、int[]
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
