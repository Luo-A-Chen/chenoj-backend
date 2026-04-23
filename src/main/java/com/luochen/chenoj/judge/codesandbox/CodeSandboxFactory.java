package com.luochen.chenoj.judge.codesandbox;

import com.luochen.chenoj.judge.codesandbox.impl.ExampleCodeSandbox;
import com.luochen.chenoj.judge.codesandbox.impl.RemoteCodeSandbox;
import com.luochen.chenoj.judge.codesandbox.impl.ThirdPartyCodeSandbox;
import org.apache.commons.lang3.StringUtils;

/**
 * 代码沙箱工厂
 * 根据字符串参数创建选择指定的代码沙箱
 */
public class CodeSandboxFactory {

    /**
     * 创建代码沙箱实例（remote 模式使用 {@link #newInstance(String, String)} 传入 url）
     */
    public static CodeSandbox newInstance(String mode) {
        return newInstance(mode, null);
    }

    /**
     * @param mode             沙箱模式
     * @param remoteExecuteUrl 远程判题完整 URL，仅 remote 时有效，对应配置 codesandbox.remote.url
     */
    public static CodeSandbox newInstance(String mode, String remoteExecuteUrl) {
        if (mode == null) {
            mode = "default";
        }
        switch (mode) {
            case "remote":
                return new RemoteCodeSandbox(remoteExecuteUrl);
            case "thirdParty":
                return new ThirdPartyCodeSandbox();
            case "example":
            default:
                return new ExampleCodeSandbox();
        }
    }
}
