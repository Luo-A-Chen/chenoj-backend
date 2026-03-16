package com.luochen.chenoj.judge;

import cn.hutool.json.JSONUtil;
import com.luochen.chenoj.common.ErrorCode;
import com.luochen.chenoj.exception.BusinessException;
import com.luochen.chenoj.judge.codesandbox.CodeSandbox;
import com.luochen.chenoj.judge.codesandbox.CodeSandboxFactory;
import com.luochen.chenoj.judge.codesandbox.CodeSandboxProxy;
import com.luochen.chenoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.luochen.chenoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.luochen.chenoj.judge.strategy.DefaultJudgeStrategy;
import com.luochen.chenoj.judge.strategy.JudgeContext;
import com.luochen.chenoj.judge.strategy.JudgeStrategy;
import com.luochen.chenoj.model.dto.question.JudgeCase;
import com.luochen.chenoj.model.dto.questionsubmit.JudgeInfo;
import com.luochen.chenoj.model.entity.Question;
import com.luochen.chenoj.model.entity.QuestionSubmit;
import com.luochen.chenoj.model.enums.QuestionSubmitStatusEnum;
import com.luochen.chenoj.service.QuestionService;
import com.luochen.chenoj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {
    @Value("{codesandbox.type:example}")
    private String value;
    @Resource
    private QuestionSubmitService questionSubmitService;//获取用户提交信息
    @Resource
    private QuestionService questionService;//根据id获取题目
    @Resource
    private JudgeManager judgeManager;//获取判题管理器


    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        //1.用户提交代码，根据用户提交的题目记录id，获取提交信息和题目
        //拿到用户的提交信息
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        //拿到具体题目
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2.判断题目如果不为等待状态，则不执行，防止重复判题
        //todo丰富抛出的异常状态
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }

        // 3.更改题目的判题状态为“判题中”，防止并发判题
        // todo 这里防止了其他操作影响当前流程，是怎么防止的？
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }

        // 4.调用代码沙箱代理类，获取执行结果
        // 这里走的是spring配置设置+工厂模式+静态代理模式
        // 从工厂拿到配置的代码沙箱类型
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(value);
        // 创建沙箱代理类
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();

        // 5.获取判题用例,输入用例
        String judgeCaseStr = question.getJudgeCase();//获取题目判题用例
        List<JudgeCase> judgeCaselist = JSONUtil.toList(judgeCaseStr, JudgeCase.class);//使用工具转换为json列表，传入代码沙箱
        List<String> inputList = judgeCaselist.stream()
                .map(JudgeCase::getInput)
                .collect(Collectors.toList());//拿到每一个用例，汇聚成输入列表
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        // 6.代码沙箱执行获得输出结果用例
        // 执行代码
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        // 返回输出列表
        List<String> outputList = executeCodeResponse.getOutputList();

        // 7.根据沙箱执行结果，设置题目的判题状态和信息
        // 构建判题上下文，封装所有的判题数据（也就是上下文）,
        // 判题策略不需要知道数据来源，判题依据从context中获取
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaselist(judgeCaselist);
        judgeContext.setQuestion(question);

        // 8.调用判题策略
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);

        // 9.更新数据库
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }

        // 10.返回结果
        questionSubmit.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        return questionSubmit;
    }
}
