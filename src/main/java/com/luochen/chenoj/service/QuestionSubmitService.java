package com.luochen.chenoj.service;

import com.luochen.chenoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.luochen.chenoj.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luochen.chenoj.model.entity.User;

/**
* @author luochen
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2026-03-04 20:07:44
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest 题目提交信息
     * @param loginUser
     * @return
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);
}
