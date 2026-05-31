package com.luochen.chenoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luochen.chenoj.model.dto.questioncomment.QuestionCommentAddRequest;
import com.luochen.chenoj.model.dto.questioncomment.QuestionCommentQueryRequest;
import com.luochen.chenoj.model.entity.QuestionComment;
import com.luochen.chenoj.model.entity.User;
import com.luochen.chenoj.model.vo.QuestionCommentVO;

/**
 * @author luochen
 * @description 针对表【question_comment(题目评论)】的数据库操作Service
 * @createDate 2026-05-31
 */
public interface QuestionCommentService extends IService<QuestionComment> {

    /**
     * 创建题目评论
     *
     * @param questionCommentAddRequest 评论信息
     * @param loginUser                 当前登录用户
     * @return 评论 id
     */
    long addQuestionComment(QuestionCommentAddRequest questionCommentAddRequest, User loginUser);

    /**
     * 获取评论查询条件
     *
     * @param questionCommentQueryRequest 查询参数
     * @return QueryWrapper
     */
    QueryWrapper<QuestionComment> getQueryWrapper(QuestionCommentQueryRequest questionCommentQueryRequest);

    /**
     * 分页获取评论封装
     *
     * @param questionCommentPage 评论分页
     * @param loginUser           当前登录用户（允许为空）
     * @return 评论封装分页
     */
    Page<QuestionCommentVO> getQuestionCommentVOPage(Page<QuestionComment> questionCommentPage, User loginUser);
}
