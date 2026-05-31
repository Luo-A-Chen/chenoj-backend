package com.luochen.chenoj.model.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luochen.chenoj.model.entity.QuestionComment;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目评论封装类
 *
 * @TableName question_comment
 */
@TableName(value = "question_comment")
@Data
public class QuestionCommentVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 评论用户 id
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论 id，一级评论固定为 0
     */
    private Long parentId;

    /**
     * 被回复用户 id（仅二级回复）
     */
    private Long replyToUserId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 评论用户信息
     */
    private UserVO userVO;

    /**
     * 被回复用户信息
     */
    private UserVO replyToUserVO;

    /**
     * 当前登录用户是否可删除该评论
     */
    private Boolean canDelete;

    /**
     * 二级回复列表（仅一级评论会携带）
     */
    private List<QuestionCommentVO> replyList;

    /**
     * 是否删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;

    public static QuestionComment voToObj(QuestionCommentVO questionCommentVO) {
        if (questionCommentVO == null) {
            return null;
        }
        QuestionComment questionComment = new QuestionComment();
        BeanUtils.copyProperties(questionCommentVO, questionComment);
        return questionComment;
    }

    public static QuestionCommentVO objToVo(QuestionComment questionComment) {
        if (questionComment == null) {
            return null;
        }
        QuestionCommentVO questionCommentVO = new QuestionCommentVO();
        BeanUtils.copyProperties(questionComment, questionCommentVO);
        return questionCommentVO;
    }
}
