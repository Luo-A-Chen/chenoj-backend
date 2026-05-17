package com.luochen.chenoj.judge.strategy;

import com.luochen.chenoj.judge.codesandbox.model.JudgeInfo;

/**
 * Java 判题策略（与默认策略逻辑一致，便于后续扩展 Java 特判）
 */
public class JavaLanguageJudgeStrategy extends AbstractJudgeStrategy {

    @Override
    protected JudgeInfo judgeOutputsAndLimits(JudgeContext judgeContext, JudgeInfo judgeInfoResponse,
                                              Long time, Long memory) {
        return super.judgeOutputsAndLimits(judgeContext, judgeInfoResponse, time, memory);
    }
}
