package com.luochen.chenoj.judge.codesandbox;

import com.luochen.chenoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.luochen.chenoj.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 代码沙箱代理类
 * 对代码沙箱进行日志增强
 * 客户端不需要知道真实的代码沙箱实现
 * 代理模式还可以在不修改原代码的基础上增强功能（比如：日志）
 */
@Slf4j
public class CodeSandboxProxy implements CodeSandbox {

    //设置为静态属性，静止更改
    private final CodeSandbox codeSandbox;

    //明确代理关系，稳定代理的对象，防止代理对象被修改
    public CodeSandboxProxy(CodeSandbox codeSandbox) {
        this.codeSandbox = codeSandbox;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        log.info("代码沙箱请求信息：{}", request.toString());
        //调用被代理的代码沙箱
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(request);
        log.info("代码沙箱响应信息：{}", executeCodeResponse.toString());
        return executeCodeResponse;
    }
}
