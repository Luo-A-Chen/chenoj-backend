package com.luochen.chenoj.judge.strategy;

import com.luochen.chenoj.judge.codesandbox.model.JudgeInfo;

/**
 * 默认判题策略
 */
public class DefaultJudgeStrategy extends AbstractJudgeStrategy {

    @Override
    protected JudgeInfo judgeOutputsAndLimits(JudgeContext judgeContext, JudgeInfo judgeInfoResponse,
                                              Long time, Long memory) {
        return super.judgeOutputsAndLimits(judgeContext, judgeInfoResponse, time, memory);
    }
}
