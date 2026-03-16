package com.luochen.chenoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luochen.chenoj.annotation.AuthCheck;
import com.luochen.chenoj.common.BaseResponse;
import com.luochen.chenoj.common.ErrorCode;
import com.luochen.chenoj.common.ResultUtils;
import com.luochen.chenoj.constant.UserConstant;
import com.luochen.chenoj.exception.BusinessException;
import com.luochen.chenoj.model.dto.question.QuestionQueryRequest;
import com.luochen.chenoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.luochen.chenoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.luochen.chenoj.model.entity.Question;
import com.luochen.chenoj.model.entity.QuestionSubmit;
import com.luochen.chenoj.model.entity.User;
import com.luochen.chenoj.model.vo.QuestionSubmitVO;
import com.luochen.chenoj.service.QuestionSubmitService;
import com.luochen.chenoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 *
 * @author <a href="https://github.com/luochen">程序员啊琛</a>
 * @from <a href="https://chenduoduo.icu">琛多多的oj网站</a>
 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交记录的id
     */
    @PostMapping("/")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest, HttpServletRequest request) {
        // 题目id大于1才能提交
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才提交题目
        final User loginUser = userService.getLoginUser(request);
        long result = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 分页获取题目列表（除了管理员外，普通用户只能看到非答案提交代码等公开信息）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest  request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        //1.查询出未脱敏的分页信息
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        final User loginUser = userService.getLoginUser(request);//获取当前登录用户信息
        //2.将分页信息脱敏后返回
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
    }
}
