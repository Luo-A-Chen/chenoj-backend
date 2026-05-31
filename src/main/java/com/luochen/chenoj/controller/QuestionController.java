package com.luochen.chenoj.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luochen.chenoj.annotation.AuthCheck;
import com.luochen.chenoj.common.BaseResponse;
import com.luochen.chenoj.common.DeleteRequest;
import com.luochen.chenoj.common.ErrorCode;
import com.luochen.chenoj.common.ResultUtils;
import com.luochen.chenoj.constant.UserConstant;
import com.luochen.chenoj.exception.BusinessException;
import com.luochen.chenoj.exception.ThrowUtils;
import com.luochen.chenoj.model.dto.question.*;
import com.luochen.chenoj.model.dto.questioncomment.QuestionCommentAddRequest;
import com.luochen.chenoj.model.dto.questioncomment.QuestionCommentQueryRequest;
import com.luochen.chenoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.luochen.chenoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.luochen.chenoj.model.entity.QuestionComment;
import com.luochen.chenoj.model.entity.Question;
import com.luochen.chenoj.model.entity.QuestionSubmit;
import com.luochen.chenoj.model.entity.User;
import com.luochen.chenoj.model.vo.DailyPracticeVO;
import com.luochen.chenoj.model.vo.QuestionCommentVO;
import com.luochen.chenoj.model.vo.WeeklyBoardVO;
import com.luochen.chenoj.model.vo.stat.QuestionPlatformStatsVO;
import com.luochen.chenoj.model.vo.QuestionSubmitVO;
import com.luochen.chenoj.model.vo.QuestionVO;
import com.luochen.chenoj.service.QuestionCommentService;
import com.luochen.chenoj.service.DailyPracticeService;
import com.luochen.chenoj.service.QuestionService;
import com.luochen.chenoj.service.QuestionSubmitService;
import com.luochen.chenoj.service.UserService;
import com.luochen.chenoj.service.QuestionStatsService;
import com.luochen.chenoj.service.WeeklyStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题目接口
 *
 * @author <a href="https://github.com/luochen">程序员啊琛</a>
 * @from <a href="https://chenduoduo.icu">琛多多的网站</a>
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private QuestionCommentService questionCommentService;

    @Resource
    private DailyPracticeService dailyPracticeService;

    @Resource
    private WeeklyStatsService weeklyStatsService;

    @Resource
    private QuestionStatsService questionStatsService;

    /**
     * 创建一道题目
     *
     * @param questionAddRequest 要求前端传来请求体
     * @param request
     * BaseResponse<Long>表示data里面装的是Long类型
     * @requestbody是把请求体的json自动反序列化成questionaddrequest对象
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        //请求体为空报错
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        //使用beanUtils的copyProperties把同名字段自动复制过去
        BeanUtils.copyProperties(questionAddRequest, question);
        //因为数据库存储的【标签，判题用例，判题配置】不是同一类型的，需要进行手动转换
        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        List<JudgeCase> judgeCases = questionAddRequest.getJudgeCase();
        if (judgeCases != null) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCases));
        }
        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        //提取并校验完请求体的信息后，调用业务层对提交的题目信息进行处理
        questionService.validQuestion(question, true);
        //从session里面拿当前登录用户，是什么意思？这不是从请求里面拿吗？
        //你说http请求对象里有session，session我的理解不是在后端生成的吗
        User loginUser = userService.getLoginUser(request);
        //额外补充题目信息
        question.setUserId(loginUser.getId());
        question.setFavourNum(0);
        question.setThumbNum(0);
        //quesionsercvice继承了mybatis的通用类所以调用的父类普通方法
        boolean result = questionService.save(question);
        //工具方法，先取反，判断有题目有无保存成功
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        //数据库会自动生成主键id，mybatis-plus会自动set进question里
        long newQuestionId = question.getId();
        // resultutils工具，帮忙new响应对象，然后静态化，可以直接调用方法
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        if (oldQuestion == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        List<JudgeCase> judgeCases = questionUpdateRequest.getJudgeCase();
        if (judgeCases != null) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCases));
        }
        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        long id = questionUpdateRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 分页获取题目列表（仅管理员）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 编辑（用户）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<String> tags = questionEditRequest.getTags();
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        List<JudgeCase> judgeCases = questionEditRequest.getJudgeCase();
        if (judgeCases != null) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCases));
        }
        JudgeConfig judgeConfig = questionEditRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        User loginUser = userService.getLoginUser(request);
        long id = questionEditRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        if (oldQuestion == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }

    /**
     * 每日一题 + 连续打卡看板（日历与换题以 Asia/Shanghai 为准；同一天全站同一道随机题）。
     * 登录态下返回是否完成今日每日题、连续完成每日题的天数；未登录仍可看到今日题目。
     */
    @GetMapping("/daily/board")
    public BaseResponse<DailyPracticeVO> getDailyPracticeBoard(HttpServletRequest request) {
        User loginUser = userService.getLoginUserPermitNull(request);
        return ResultUtils.success(dailyPracticeService.getDailyPracticeBoard(loginUser));
    }

    /**
     * 周报：本周（东八区周一至周日）AC 排行榜、热门题目（按提交次数）TOP3；新一周开始后统计窗口自然前移，不包含上周提交。
     */
    @GetMapping("/stats/weekly/board")
    public BaseResponse<WeeklyBoardVO> getWeeklyBoard() {
        return ResultUtils.success(weeklyStatsService.getWeeklyBoard());
    }

    /**
     * 平台题目数据统计（题目量、通过率、标签分布、语言分布、近 7 日提交趋势、热门题目等）
     */
    @GetMapping("/stats/platform")
    public BaseResponse<QuestionPlatformStatsVO> getPlatformStats() {
        return ResultUtils.success(questionStatsService.getPlatformStats());
    }

    /**
     * 发表评论（支持一级评论和二级回复）
     */
    @PostMapping("/comment/add")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Long> addQuestionComment(@RequestBody QuestionCommentAddRequest questionCommentAddRequest,
                                                 HttpServletRequest request) {
        if (questionCommentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long commentId = questionCommentService.addQuestionComment(questionCommentAddRequest, loginUser);
        return ResultUtils.success(commentId);
    }

    /**
     * 分页查询题目评论（默认查一级评论，并携带对应二级回复）
     */
    @PostMapping("/comment/list/page")
    public BaseResponse<Page<QuestionCommentVO>> listQuestionCommentByPage(@RequestBody QuestionCommentQueryRequest questionCommentQueryRequest,
                                                                           HttpServletRequest request) {
        if (questionCommentQueryRequest == null
                || questionCommentQueryRequest.getQuestionId() == null
                || questionCommentQueryRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = questionCommentQueryRequest.getCurrent();
        long size = questionCommentQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUserPermitNull(request);
        Page<QuestionComment> questionCommentPage = questionCommentService.page(new Page<>(current, size),
                questionCommentService.getQueryWrapper(questionCommentQueryRequest));
        return ResultUtils.success(questionCommentService.getQuestionCommentVOPage(questionCommentPage, loginUser));
    }

    /**
     * 删除评论（仅本人或管理员）
     */
    @PostMapping("/comment/delete")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> deleteQuestionComment(@RequestBody DeleteRequest deleteRequest,
                                                       HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QuestionComment oldQuestionComment = questionCommentService.getById(deleteRequest.getId());
        if (oldQuestionComment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        boolean canDelete = oldQuestionComment.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser);
        ThrowUtils.throwIf(!canDelete, ErrorCode.NO_AUTH_ERROR);
        boolean removed = questionCommentService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!removed, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 提交题目
     * 这里可能出现一个高并发的问题，也就是多个用户同时提交代码，同时调用代码沙箱执行
     * 资源竞争可能出问题
     * 为什么会出现数据库连接耗尽、内存撑爆、数据出现错误是数据一致性问题吗？
     * 1.数据库的连接是有限资源。
     * 2.每个请求都要创建对象，创建对象占内存，都在堆里，gc触发频繁性能下降
     * 一般怎么解决：消息队列、缓存、限流、数据库连接池、分布式锁
     * 1.使用synchronized单机锁，可以让同一时间只有一个线程能进来
     * 2.分布式（多机锁）,所有服务器都能看到的公共锁，也就是放在redis上
     *   大家连接的是同一个redis，都来redis上抢这把锁
     *   redis是单线程只能有一个成功，抢到了执行业务逻辑，做完删除锁
     *   原理是：redis里面的key--value，设置后，key存在了，相当于有锁
     *          用完删除key，下一个进入的线程又可以重新创建key
     *   所以分布式锁解决的是1.单个用户重复提交，2.多个用户抢同一把锁（防止高并发）
     *   看门狗机制就是自动续期，每隔一段时间检查A线程还在执行吗，如果还在执行，就自动续期
     *   redission自动带有这个看门狗机制
     * 判题平台规模不大，所以使用消息队列是比较好的选择
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交记录的id
     */
    @PostMapping("/question_submit/do")
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
     * 分页查询题目提交记录（任意已登录用户均可浏览；他人提交的代码对非管理员脱敏）。
     *
     * @param questionSubmitQueryRequest 筛选条件（题号、语言、状态、用户 id 等，均为可选）
     * @param request
     * @return
     */
    @PostMapping("/question_submit/list/page")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest  request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        final User loginUser = userService.getLoginUser(request);
        //1.查询出分页信息（按请求条件筛选，可看全站或某题、某用户的提交概要）
        // new Page<>(2,10)第二页，每页十条，mybatis-plus的分页对象
        // .page的需要两个参数，一个是分页对象，一个是查询条件对象
        // 数据量大的时候需要用“游标分页”优化，来避免全表扫描
        // 前端传递过来questionSubmitQueryRequest，里面包含了很多搜索过滤信息
        // 也就是说分页就是执行一段sql过滤后返回给的结果而已
        // 关键是.page的这个方面
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
    }

    /**
     * 删除题目提交记录（仅管理员）
     */
    @PostMapping("/question_submit/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestionSubmit(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        QuestionSubmit old = questionSubmitService.getById(id);
        ThrowUtils.throwIf(old == null, ErrorCode.NOT_FOUND_ERROR);
        boolean removed = questionSubmitService.removeById(id);
        ThrowUtils.throwIf(!removed, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
