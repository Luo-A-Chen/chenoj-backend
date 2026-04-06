package com.luochen.chenoj.judge.codesandbox;

import com.luochen.chenoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.luochen.chenoj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口
 */
public interface CodeSandbox {
    /**
     * 执行代码
     * 后续的代码沙箱实现都得继承这个接口，然后只接受ExecuteCodeRequest参数
     * @param request
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest request);
}
