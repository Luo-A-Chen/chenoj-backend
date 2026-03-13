package com.luochen.chenoj.judge.impl;

import com.luochen.chenoj.judge.CodeSandbox;
import com.luochen.chenoj.judge.model.ExecuteCodeRequest;
import com.luochen.chenoj.judge.model.ExecuteCodeResponse;

/**
 * 远程代码沙箱
 */
public class RemoteCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        System.out.println("远程代码沙箱");
        return null;
    }
}
