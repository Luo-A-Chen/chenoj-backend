package com.luochen.chenoj.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.luochen.chenoj.common.ErrorCode;
import com.luochen.chenoj.exception.BusinessException;
import com.luochen.chenoj.judge.codesandbox.CodeSandbox;
import com.luochen.chenoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.luochen.chenoj.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 远程代码沙箱（地址由 {@link com.luochen.chenoj.judge.codesandbox.CodeSandboxFactory} 传入，来自 codesandbox.remote.url）
 */
@Slf4j
public class RemoteCodeSandbox implements CodeSandbox {
    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_ReQUEST_SECRET = "secretKey";

    private final String executeCodeUrl;

    public RemoteCodeSandbox(String executeCodeUrl) {
        if (StringUtils.isBlank(executeCodeUrl)) {
            throw new IllegalArgumentException("codesandbox.remote.url 不能为空");
        }
        this.executeCodeUrl = executeCodeUrl.trim();
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        log.info("远程代码沙箱 POST url={}", executeCodeUrl);
        String json = JSONUtil.toJsonStr(request);
        String responseStr = HttpUtil.createPost(executeCodeUrl)
                .header(AUTH_REQUEST_HEADER, AUTH_ReQUEST_SECRET)
                .body(json)
                .execute()
                .body();
        if (StringUtils.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "executeCode error: response blank");
        }
        JSONObject root = JSONUtil.parseObj(responseStr);
        if (root.containsKey("code")) {
            int code = root.getInt("code", -1);
            if (code != 0) {
                throw new BusinessException(ErrorCode.API_REQUEST_ERROR, root.getStr("message", "沙箱返回失败"));
            }
            if (root.get("data") != null) {
                return JSONUtil.toBean(root.getJSONObject("data"), ExecuteCodeResponse.class);
            }
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "沙箱返回成功但 data 为空");
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}
