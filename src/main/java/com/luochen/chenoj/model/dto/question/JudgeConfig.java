package com.luochen.chenoj.model.dto.question;

import lombok.Data;


/**
 * 题目信息
 */
@Data
public class JudgeConfig {
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
