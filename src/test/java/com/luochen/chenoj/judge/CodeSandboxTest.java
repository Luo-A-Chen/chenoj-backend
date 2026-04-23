package com.luochen.chenoj.judge;

import com.luochen.chenoj.judge.codesandbox.CodeSandbox;
import com.luochen.chenoj.judge.codesandbox.CodeSandboxFactory;
import com.luochen.chenoj.judge.codesandbox.CodeSandboxProxy;
import com.luochen.chenoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.luochen.chenoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.luochen.chenoj.model.enums.QuestionSubmitLanguageEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@SpringBootTest
class CodeSandboxTest {

    @Value("{codesandbox.type:example}")
    private String value;

    @Test
    void executeCode() {
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){//测试不同输入切换沙箱
            String type = scanner.next();
            CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
            String code="int main(){}";
            String language= QuestionSubmitLanguageEnum.JAVA.getValue();
            List<String> inputList = Arrays.asList("1 2","3 4");
            ExecuteCodeRequest  executeCodeRequest = ExecuteCodeRequest.builder()
                    .code(code)
                    .language(language)
                    .inputList(inputList)
                    .build();
            ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
            Assertions.assertNotNull(executeCodeResponse);
        }
    }

    @Test
    void executeCodeByValue() {//测试配置修改沙箱选择
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(value);
        String code="int main(){}";
        String language= QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2","3 4");
        ExecuteCodeRequest  executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }

    @Test
    void executeCodeByProxy() {//测试静态代理类
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(value);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String code="int main(){}";
        String language= QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2","3 4");
        ExecuteCodeRequest  executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }

    public static void main(String[] args) {//测试不同输入切换沙箱
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String type = scanner.next();
            CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
            String code="int main(){}";
            String language= QuestionSubmitLanguageEnum.JAVA.getValue();
            List<String> inputList = Arrays.asList("1 2","3 4");
            ExecuteCodeRequest  executeCodeRequest = ExecuteCodeRequest.builder()
                    .code(code)
                    .language(language)
                    .inputList(inputList)
                    .build();
            ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
            Assertions.assertNotNull(executeCodeResponse);
        }
    }
}