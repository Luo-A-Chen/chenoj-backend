package com.luochen.chenoj.model.dto.questioncomment;

import lombok.Data;

import java.io.Serializable;

/**
 * 题目评论创建请求
 */
@Data
public class QuestionCommentAddRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论 id，0 为一级评论，>0 为二级回复
     */
    private Long parentId;

    /**
     * 被回复用户 id（仅二级回复时传递）
     */
    private Long replyToUserId;

    private static final long serialVersionUID = 1L;
}
