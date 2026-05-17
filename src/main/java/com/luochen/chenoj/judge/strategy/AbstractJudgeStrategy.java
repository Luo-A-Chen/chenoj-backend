package com.luochen.chenoj.judge.strategy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.luochen.chenoj.judge.codesandbox.model.JudgeInfo;
import com.luochen.chenoj.model.dto.question.JudgeCase;
import com.luochen.chenoj.model.dto.question.JudgeConfig;
import com.luochen.chenoj.model.entity.Question;
import com.luochen.chenoj.model.enums.JudgeInfoMessageEnum;

import java.util.List;

/**
 * 判题策略模板：先处理沙箱状态，再比对输出与时间/内存。
 */
public abstract class AbstractJudgeStrategy implements JudgeStrategy {

    protected static String normalizeSandboxOutputForCompare(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceFirst("\\s+$", "");
    }

    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        JudgeInfo sandboxJudgeInfo = judgeContext.getJudgeInfo();
        Long memory = sandboxJudgeInfo != null ? sandboxJudgeInfo.getMemory() : null;
        Long time = sandboxJudgeInfo != null ? sandboxJudgeInfo.getTime() : null;
        String sandboxDetail = sandboxJudgeInfo != null ? sandboxJudgeInfo.getErrorMessage() : null;
        if (StrUtil.isBlank(sandboxDetail)) {
            sandboxDetail = judgeContext.getSandboxMessage();
        }

        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setTime(time);
        judgeInfoResponse.setMemory(memory);

        Integer sandboxStatus = judgeContext.getSandboxStatus();
        if (sandboxStatus != null && sandboxStatus == 2) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.COMPILE_ERROR.getValue());
            judgeInfoResponse.setErrorMessage(sandboxDetail);
            return judgeInfoResponse;
        }
        if (sandboxStatus != null && sandboxStatus == 4) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue());
            judgeInfoResponse.setErrorMessage(sandboxDetail);
            return judgeInfoResponse;
        }
        if (sandboxStatus != null && sandboxStatus == 3) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.RUNTIME_ERROR.getValue());
            judgeInfoResponse.setErrorMessage(sandboxDetail);
            return judgeInfoResponse;
        }
        if (sandboxStatus != null && sandboxStatus != 1) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.SYSTEM_ERROR.getValue());
            judgeInfoResponse.setErrorMessage(sandboxDetail);
            return judgeInfoResponse;
        }

        return judgeOutputsAndLimits(judgeContext, judgeInfoResponse, time, memory);
    }

    protected JudgeInfo judgeOutputsAndLimits(JudgeContext judgeContext, JudgeInfo judgeInfoResponse,
                                              Long time, Long memory) {
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> judgeCaselist = judgeContext.getJudgeCaselist();

        if (outputList == null || inputList == null || outputList.size() != inputList.size()) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
            return judgeInfoResponse;
        }

        for (int i = 0; i < judgeCaselist.size(); i++) {
            JudgeCase judgeCase = judgeCaselist.get(i);
            if (!normalizeSandboxOutputForCompare(judgeCase.getOutput())
                    .equals(normalizeSandboxOutputForCompare(outputList.get(i)))) {
                judgeInfoResponse.setMessage(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
                return judgeInfoResponse;
            }
        }

        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long needMemoryLimit = judgeConfig != null ? judgeConfig.getMemoryLimit() : null;
        Long needTimeLimit = judgeConfig != null ? judgeConfig.getTimeLimit() : null;
        if (needMemoryLimit != null && memory != null && memory > needMemoryLimit) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getValue());
            return judgeInfoResponse;
        }
        if (needTimeLimit != null && time != null && time > needTimeLimit) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue());
            return judgeInfoResponse;
        }

        judgeInfoResponse.setMessage(JudgeInfoMessageEnum.ACCEPTED.getValue());
        return judgeInfoResponse;
    }
}
