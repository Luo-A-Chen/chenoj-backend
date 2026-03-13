package com.luochen.chenoj.judge.impl;

import com.luochen.chenoj.judge.CodeSandbox;
import com.luochen.chenoj.judge.model.ExecuteCodeRequest;
import com.luochen.chenoj.judge.model.ExecuteCodeResponse;

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
