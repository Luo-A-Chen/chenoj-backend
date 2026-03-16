package com.luochen.chenoj.judge.strategy;

import com.luochen.chenoj.model.dto.question.JudgeCase;
import com.luochen.chenoj.model.dto.questionsubmit.JudgeInfo;
import com.luochen.chenoj.model.entity.Question;
import com.luochen.chenoj.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 */
@Data
public class JudgeContext {

    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaselist;

    private Question question;

    private QuestionSubmit questionSubmit;
}
