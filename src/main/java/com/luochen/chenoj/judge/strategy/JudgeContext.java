package com.luochen.chenoj.judge.strategy;

import com.luochen.chenoj.model.dto.question.JudgeCase;
import com.luochen.chenoj.judge.codesandbox.model.JudgeInfo;
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

    /**
     * 沙箱执行状态：1 成功 2 编译错误 3 运行错误 4 超时
     */
    private Integer sandboxStatus;

    /**
     * 沙箱返回的简要说明
     */
    private String sandboxMessage;
}
