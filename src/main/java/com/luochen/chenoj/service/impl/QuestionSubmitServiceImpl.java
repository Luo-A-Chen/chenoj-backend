package com.luochen.chenoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luochen.chenoj.common.ErrorCode;
import com.luochen.chenoj.exception.BusinessException;
import com.luochen.chenoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.luochen.chenoj.model.entity.Question;
import com.luochen.chenoj.model.entity.QuestionSubmit;
import com.luochen.chenoj.model.entity.User;
import com.luochen.chenoj.model.enums.QuestionSubmitLanguageEnum;
import com.luochen.chenoj.model.enums.QuestionSubmitStatusEnum;
import com.luochen.chenoj.service.QuestionService;
import com.luochen.chenoj.service.QuestionSubmitService;
import com.luochen.chenoj.mapper.QuestionSubmitMapper;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;

/**
* @author luochen
* @description 针对表【question_submit(题目提交)】的数据库操作Service实现
* @createDate 2026-03-04 20:07:44
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService{


    @Resource
    private QuestionService questionService;

    /**
     * 提交题目
     * 1.校验编程语言是否合法
     * 2.校验题目是否存在
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        //1.校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if(languageEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }

        long questionId = questionSubmitAddRequest.getQuestionId();
        // 2.判断题目是否存在，根据题目id获取题目
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //3.校验用户是否提交过该题目
        long userId = loginUser.getId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        // 设置题目的初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        if(!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目提交失败");
        }
        return questionSubmit.getId();
    }
}




