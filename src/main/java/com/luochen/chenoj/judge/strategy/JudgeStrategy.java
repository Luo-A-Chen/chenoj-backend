package com.luochen.chenoj.judge.strategy;

import com.luochen.chenoj.judge.codesandbox.model.JudgeInfo;

/**
 * 判题策略接口
 */
public interface JudgeStrategy {

    JudgeInfo doJudge(JudgeContext judgeContext);
}
