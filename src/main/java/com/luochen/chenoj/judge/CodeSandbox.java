package com.luochen.chenoj.judge;

import com.luochen.chenoj.judge.model.ExecuteCodeRequest;
import com.luochen.chenoj.judge.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口
 */
public interface CodeSandbox {
    /**
     * 执行代码
     *
     * TODO 这里需要考虑，如果代码沙箱服务挂了，应该如何查看并处理
     * @param request
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest request);
}
