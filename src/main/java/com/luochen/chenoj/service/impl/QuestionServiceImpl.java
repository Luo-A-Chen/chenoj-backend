package com.luochen.chenoj.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.luochen.chenoj.model.dto.question.JudgeCase;
import com.luochen.chenoj.model.dto.question.JudgeConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luochen.chenoj.common.ErrorCode;
import com.luochen.chenoj.constant.CommonConstant;
import com.luochen.chenoj.exception.BusinessException;
import com.luochen.chenoj.exception.ThrowUtils;
import com.luochen.chenoj.model.dto.question.QuestionQueryRequest;
import com.luochen.chenoj.model.entity.*;
import com.luochen.chenoj.model.vo.QuestionVO;
import com.luochen.chenoj.model.vo.UserVO;
import com.luochen.chenoj.service.QuestionService;
import com.luochen.chenoj.mapper.QuestionMapper;
import com.luochen.chenoj.service.UserService;
import com.luochen.chenoj.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author luochen
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2026-03-04 20:06:10
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

    @Resource
    private UserService userService;

    /**
     * 校验题目是否合法
     * @param question
     * @param add
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();
        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
            if (StringUtils.isBlank(judgeCase)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "请至少配置一组测试用例");
            }
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
        if(StringUtils.isNotBlank(answer)&& answer.length()>8192){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"答案过长");
        }
        if (StringUtils.isNotBlank(judgeCase) && judgeCase.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题用例过长");
        }
        if (StringUtils.isNotBlank(judgeConfig) && judgeConfig.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题配置过长");
        }
        if (StringUtils.isNotBlank(judgeCase)) {
            validateJudgeCasesNotEmpty(judgeCase);
        }
        if (StringUtils.isNotBlank(judgeConfig)) {
            JudgeConfig config = JSONUtil.toBean(judgeConfig, JudgeConfig.class);
            if (config != null && config.getMemoryLimit() != null
                    && config.getMemoryLimit() > 64 * 1024) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "内存限制不能超过 64 MB");
            }
            if (config != null) {
                if (StringUtils.isBlank(config.getMethodName())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "须填写方法名");
                }
                if (config.getParamTypes() == null || config.getParamTypes().isEmpty()) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "须填写参数类型");
                }
                if (StringUtils.isBlank(config.getReturnType())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "须填写返回类型");
                }
                if (StringUtils.isNotBlank(judgeCase)) {
                    validateMethodJudgeCaseJson(judgeCase, config);
                }
            }
        }
    }

    private void validateJudgeCasesNotEmpty(String judgeCase) {
        List<JudgeCase> cases = JSONUtil.toList(judgeCase, JudgeCase.class);
        if (CollUtil.isEmpty(cases)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请至少配置一组测试用例");
        }
        for (int i = 0; i < cases.size(); i++) {
            JudgeCase item = cases.get(i);
            if (item == null || StringUtils.isAnyBlank(item.getInput(), item.getOutput())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,
                        "第 " + (i + 1) + " 组测试用例的输入与输出均不能为空");
            }
        }
    }

    private void validateMethodJudgeCaseJson(String judgeCase, JudgeConfig config) {
        List<JudgeCase> cases = JSONUtil.toList(judgeCase, JudgeCase.class);
        if (CollUtil.isEmpty(cases)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题用例不能为空");
        }
        int expectedArgs = config.getParamTypes().size();
        for (int i = 0; i < cases.size(); i++) {
            JudgeCase item = cases.get(i);
            if (item == null || StringUtils.isBlank(item.getInput())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,
                        "第 " + (i + 1) + " 组用例 input 须为 JSON 数组");
            }
            try {
                JSONArray inputArr = JSONUtil.parseArray(item.getInput().trim());
                if (inputArr == null) {
                    throw new IllegalArgumentException("input");
                }
                if (inputArr.size() != expectedArgs) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,
                            "第 " + (i + 1) + " 组用例参数个数应为 " + expectedArgs);
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,
                        "第 " + (i + 1) + " 组用例 input 须为合法 JSON 数组");
            }
            if (StringUtils.isBlank(item.getOutput())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,
                        "第 " + (i + 1) + " 组用例 output 不能为空");
            }
        }
    }

    /**
     * 获取查询包装类(用户根据哪些字段查询，根据前端传来的请求对象，得到mybatis框架支持的查询QueryWrapper类）
     * 动态构建sql查询的where条件，最终转换成对应的sql查询语句
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        String answer = questionQueryRequest.getAnswer();
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        List<String> tagList = questionQueryRequest.getTags();
        Long userId = questionQueryRequest.getUserId();
        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取帖子封装脱敏类
     *
     * @param question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request){
        QuestionVO questionVO = QuestionVO.objToVo(question);
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        //得到脱敏的用户信息
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUserVO(userVO);
        return questionVO;
    }

    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            Long userId = question.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            return questionVO;
        }).collect(Collectors.toList());
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

}




