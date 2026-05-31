package com.luochen.chenoj.model.dto.questioncomment;

import com.luochen.chenoj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 题目评论查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionCommentQueryRequest extends PageRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 父评论 id，默认查一级评论（0）
     */
    private Long parentId;

    /**
     * 评论用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
