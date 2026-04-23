package com.luochen.chenoj.judge.codesandbox.impl;

import com.luochen.chenoj.judge.codesandbox.CodeSandbox;
import com.luochen.chenoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.luochen.chenoj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 第三方代码沙箱
 */
public class ThirdPartyCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        System.out.println("第三方代码沙箱");
        return null;
    }
}
