package com.luochen.chenoj.service.impl;

import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.luochen.chenoj.common.ErrorCode;
import com.luochen.chenoj.constant.CaptchaConstant;
import com.luochen.chenoj.exception.BusinessException;
import com.luochen.chenoj.model.vo.CaptchaVO;
import com.luochen.chenoj.service.CaptchaService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 图形验证码服务实现（Redis 存储，一次性校验）
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final int WIDTH = 130;
    private static final int HEIGHT = 48;
    private static final int CODE_LENGTH = 4;
    private static final int LINE_COUNT = 80;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public CaptchaVO generateCaptcha() {
        LineCaptcha captcha = new LineCaptcha(WIDTH, HEIGHT, CODE_LENGTH, LINE_COUNT);
        captcha.setGenerator(new RandomGenerator("0123456789", CODE_LENGTH));
        captcha.createCode();

        String captchaKey = IdUtil.simpleUUID();
        String redisKey = CaptchaConstant.REDIS_KEY_PREFIX + captchaKey;
        stringRedisTemplate.opsForValue().set(
                redisKey,
                captcha.getCode(),
                CaptchaConstant.EXPIRE_MINUTES,
                TimeUnit.MINUTES);

        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaKey(captchaKey);
        captchaVO.setCaptchaImage(captcha.getImageBase64Data());
        return captchaVO;
    }

    @Override
    public void validateCaptcha(String captchaKey, String captchaCode) {
        if (StringUtils.isAnyBlank(captchaKey, captchaCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入验证码");
        }
        String redisKey = CaptchaConstant.REDIS_KEY_PREFIX + captchaKey;
        String expected = stringRedisTemplate.opsForValue().get(redisKey);
        stringRedisTemplate.delete(redisKey);
        if (StrUtil.isBlank(expected)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码已过期，请刷新后重试");
        }
        if (!expected.equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
    }
}
