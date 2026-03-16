package com.luochen.chenoj.judge;

import com.luochen.chenoj.judge.strategy.DefaultJudgeStrategy;
import com.luochen.chenoj.judge.strategy.JavaLanguageJudgeStrategy;
import com.luochen.chenoj.judge.strategy.JudgeContext;
import com.luochen.chenoj.judge.strategy.JudgeStrategy;
import com.luochen.chenoj.model.dto.questionsubmit.JudgeInfo;
import com.luochen.chenoj.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 * 职责分离
 */
@Service
public class JudgeManager {
    /**
     * 执行判题任务
     * 根据编程语言选择合适的判题策略
     */
    JudgeInfo doJudge(JudgeContext judgeContext){
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        //默认使用通用判题逻辑
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if(language.equals("java")){
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}
