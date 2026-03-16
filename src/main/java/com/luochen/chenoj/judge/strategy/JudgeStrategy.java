package com.luochen.chenoj.judge.strategy;

import com.luochen.chenoj.model.dto.questionsubmit.JudgeInfo;

/**
 * 判题策略接口
 */
public interface JudgeStrategy {

    JudgeInfo doJudge(JudgeContext judgeContext);
}
