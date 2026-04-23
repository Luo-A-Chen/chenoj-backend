package com.luochen.chenoj.judge;

import com.luochen.chenoj.model.entity.QuestionSubmit;


/**
 * 判题服务
 */
public interface JudgeService {
    /**
     * 判题
     *
     * @param questionSubmitId 用户提交信息的题目记录id
     * @return
     */
    QuestionSubmit doJudge(long questionSubmitId);
}
