package com.luochen.chenoj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.luochen.chenoj.model.dto.question.JudgeCase;
import com.luochen.chenoj.model.dto.question.JudgeConfig;
import com.luochen.chenoj.judge.codesandbox.model.JudgeInfo;
import com.luochen.chenoj.model.entity.Question;
import com.luochen.chenoj.model.enums.JudgeInfoMessageEnum;

import java.util.List;

/**
 * JAVA判题策略
 */
public class JavaLanguageJudgeStrategy implements JudgeStrategy {
    /** 沙箱 TTY 输出末尾可能带换行，与题库期望对齐后再比。 */
    private static String normalizeSandboxOutputForCompare(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceFirst("\\s+$", "");
    }

    /**
     * 执行判题策略
     *
     * @param judgeContext
     * @return
     */
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        // 拿到各项参数
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();//沙箱执行结果
        Long memory = judgeInfo.getMemory();//沙箱执行结果的内存
        Long time = judgeInfo.getTime();//沙箱执行结果的时间
        List<String> inputList = judgeContext.getInputList();//题目判题用例的输入
        List<String> outputList = judgeContext.getOutputList();//沙箱执行结果的输出
        Question question = judgeContext.getQuestion();//题目信息
        List<JudgeCase> judgeCaselist = judgeContext.getJudgeCaselist();//题目判题用例
        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;//默认判题成功
        
        JudgeInfo judgeInfoResponse = new JudgeInfo();//构造判题结果对象
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());//判题结果的提示信息
        judgeInfoResponse.setMemory(memory);//判题结果的内存
        judgeInfoResponse.setTime(time);//判题结果的时间

        // 判断大小是否正确（这里判断沙箱执行结果的输出和题目判题用例的输入数量是否一致）
        if (outputList.size() != inputList.size()) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        // 判断每一项输出是否和预期相等（可能是问题所在）
        // 你写的 {1,2}、{3} 若原样写进 input/output，那期望就变成字面上的 {3}，
        // 程序若 println(1+2) 得到的是 3，"3".equals("{3}") 为 false，一定 WA。
        for (int i = 0; i < judgeCaselist.size(); i++) {
            JudgeCase judgeCase = judgeCaselist.get(i);
            if (!normalizeSandboxOutputForCompare(judgeCase.getOutput())
                    .equals(normalizeSandboxOutputForCompare(outputList.get(i)))) {
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                return judgeInfoResponse;
            }
        }
        // 检查时间和内存限制消耗
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long needMemoryLimit = judgeConfig.getMemoryLimit();
        Long needTimeLimit = judgeConfig.getTimeLimit();
        if (needMemoryLimit != null && memory != null && memory > needMemoryLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        if (needTimeLimit != null && time != null && time > needTimeLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        return judgeInfoResponse;
    }
}
