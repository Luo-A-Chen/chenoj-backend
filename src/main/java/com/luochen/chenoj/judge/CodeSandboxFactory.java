package com.luochen.chenoj.judge;

import com.luochen.chenoj.judge.impl.ExampleCodeSandbox;
import com.luochen.chenoj.judge.impl.RemoteCodeSandbox;
import com.luochen.chenoj.judge.impl.ThirdPartyCodeSandbox;

/**
 * 代码沙箱工厂
 * 根据字符串参数创建选择指定的代码沙箱
 */
public class CodeSandboxFactory {
    /**
     * 创建代码沙箱实例
     *todo,拓展，可以确定代码沙箱不出现线程问题，可复用，升级为单例工厂模式
     * @param mode 沙箱模式
     */
    public static CodeSandbox newInstance(String mode) {
        if (mode == null) {
            mode = "default";
        }
        switch (mode) {
            case "remote":
                return new RemoteCodeSandbox();
            case "thirdParty":
                return new ThirdPartyCodeSandbox();
            case "example":
            default:
                return new ExampleCodeSandbox();
        }
    }
}
