package com.luochen.chenoj.controller;

import com.luochen.chenoj.common.BaseResponse;
import com.luochen.chenoj.common.ResultUtils;
import com.luochen.chenoj.model.vo.CaptchaVO;
import com.luochen.chenoj.service.CaptchaService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图形验证码接口
 */
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Resource
    private CaptchaService captchaService;

    /**
     * 获取图形验证码
     */
    @GetMapping("/get")
    public BaseResponse<CaptchaVO> getCaptcha() {
        return ResultUtils.success(captchaService.generateCaptcha());
    }
}
