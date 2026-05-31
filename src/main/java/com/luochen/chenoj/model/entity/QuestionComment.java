package com.luochen.chenoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 题目评论
 *
 * @TableName question_comment
 */
@TableName(value = "question_comment")
@Data
public class QuestionComment {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 父评论 id，0 为一级评论，>0 为二级回复
     */
    private Long parentId;

    /**
     * 被回复用户 id（仅二级回复使用）
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
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
