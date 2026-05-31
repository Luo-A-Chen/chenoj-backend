package com.luochen.chenoj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luochen.chenoj.common.ErrorCode;
import com.luochen.chenoj.constant.CommonConstant;
import com.luochen.chenoj.exception.BusinessException;
import com.luochen.chenoj.mapper.QuestionCommentMapper;
import com.luochen.chenoj.model.dto.questioncomment.QuestionCommentAddRequest;
import com.luochen.chenoj.model.dto.questioncomment.QuestionCommentQueryRequest;
import com.luochen.chenoj.model.entity.Question;
import com.luochen.chenoj.model.entity.QuestionComment;
import com.luochen.chenoj.model.entity.User;
import com.luochen.chenoj.model.vo.QuestionCommentVO;
import com.luochen.chenoj.service.QuestionCommentService;
import com.luochen.chenoj.service.QuestionService;
import com.luochen.chenoj.service.UserService;
import com.luochen.chenoj.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author luochen
 * @description 针对表【question_comment(题目评论)】的数据库操作Service实现
 * @createDate 2026-05-31
 */
@Service
public class QuestionCommentServiceImpl extends ServiceImpl<QuestionCommentMapper, QuestionComment>
        implements QuestionCommentService {

    private static final int MAX_CONTENT_LENGTH = 1000;

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Override
    public long addQuestionComment(QuestionCommentAddRequest questionCommentAddRequest, User loginUser) {
        if (questionCommentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long questionId = questionCommentAddRequest.getQuestionId();
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目 id 非法");
        }
        String content = StringUtils.trimToEmpty(questionCommentAddRequest.getContent());
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容过长");
        }
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        Long parentId = questionCommentAddRequest.getParentId() == null ? 0L : questionCommentAddRequest.getParentId();
        if (parentId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "父评论 id 非法");
        }
        if (parentId > 0) {
            QuestionComment parentComment = this.getById(parentId);
            if (parentComment == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "父评论不存在");
            }
            if (!Objects.equals(parentComment.getQuestionId(), questionId)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "父评论与题目不匹配");
            }
            if (parentComment.getParentId() != null && parentComment.getParentId() > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前仅支持二级回复");
            }
        }

        Long replyToUserId = null;
        if (parentId > 0) {
            QuestionComment parentComment = this.getById(parentId);
            Long requestReplyToUserId = questionCommentAddRequest.getReplyToUserId();
            replyToUserId = requestReplyToUserId == null ? parentComment.getUserId() : requestReplyToUserId;
            boolean validReplyTarget = lambdaQuery()
                    .eq(QuestionComment::getQuestionId, questionId)
                    .eq(QuestionComment::getUserId, replyToUserId)
                    .and(wrapper -> wrapper.eq(QuestionComment::getId, parentId)
                            .or()
                            .eq(QuestionComment::getParentId, parentId))
                    .eq(QuestionComment::getIsDelete, 0)
                    .count() > 0;
            if (!validReplyTarget) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "回复目标无效");
            }
        }

        QuestionComment questionComment = new QuestionComment();
        questionComment.setQuestionId(questionId);
        questionComment.setUserId(loginUser.getId());
        questionComment.setContent(content);
        questionComment.setParentId(parentId);
        questionComment.setReplyToUserId(replyToUserId);
        boolean saved = this.save(questionComment);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "发表评论失败");
        }
        return questionComment.getId();
    }

    @Override
    public QueryWrapper<QuestionComment> getQueryWrapper(QuestionCommentQueryRequest questionCommentQueryRequest) {
        QueryWrapper<QuestionComment> queryWrapper = new QueryWrapper<>();
        if (questionCommentQueryRequest == null) {
            return queryWrapper;
        }
        Long questionId = questionCommentQueryRequest.getQuestionId();
        Long parentId = questionCommentQueryRequest.getParentId();
        Long userId = questionCommentQueryRequest.getUserId();
        String sortField = questionCommentQueryRequest.getSortField();
        String sortOrder = questionCommentQueryRequest.getSortOrder();

        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("parentId", parentId == null ? 0 : parentId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField);
        queryWrapper.orderByDesc(!SqlUtils.validSortField(sortField), "createTime");
        return queryWrapper;
    }

    @Override
    public Page<QuestionCommentVO> getQuestionCommentVOPage(Page<QuestionComment> questionCommentPage, User loginUser) {
        List<QuestionComment> questionCommentList = questionCommentPage.getRecords();
        Page<QuestionCommentVO> questionCommentVOPage =
                new Page<>(questionCommentPage.getCurrent(), questionCommentPage.getSize(), questionCommentPage.getTotal());
        if (CollUtil.isEmpty(questionCommentList)) {
            return questionCommentVOPage;
        }

        boolean allTopLevel = questionCommentList.stream()
                .allMatch(comment -> comment.getParentId() == null || comment.getParentId() == 0);
        List<QuestionComment> replyCommentList = Collections.emptyList();
        if (allTopLevel) {
            Set<Long> parentIdSet = questionCommentList.stream()
                    .map(QuestionComment::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(parentIdSet)) {
                replyCommentList = lambdaQuery()
                        .in(QuestionComment::getParentId, parentIdSet)
                        .eq(QuestionComment::getIsDelete, 0)
                        .orderByAsc(QuestionComment::getCreateTime)
                        .list();
            }
        }

        boolean admin = loginUser != null && userService.isAdmin(loginUser);
        Long loginUserId = loginUser == null ? null : loginUser.getId();
        Set<Long> userIdSet = questionCommentList.stream()
                .map(QuestionComment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        replyCommentList.stream()
                .map(QuestionComment::getUserId)
                .filter(Objects::nonNull)
                .forEach(userIdSet::add);
        replyCommentList.stream()
                .map(QuestionComment::getReplyToUserId)
                .filter(Objects::nonNull)
                .forEach(userIdSet::add);
        Map<Long, User> userMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.toMap(User::getId, user -> user, (a, b) -> a));

        Map<Long, List<QuestionCommentVO>> parentReplyMap = replyCommentList.stream()
                .map(comment -> toCommentVO(comment, userMap, loginUserId, admin))
                .collect(Collectors.groupingBy(QuestionCommentVO::getParentId));

        List<QuestionCommentVO> questionCommentVOList = questionCommentList.stream()
                .map(comment -> {
                    QuestionCommentVO questionCommentVO = toCommentVO(comment, userMap, loginUserId, admin);
                    if (allTopLevel) {
                        questionCommentVO.setReplyList(parentReplyMap.getOrDefault(comment.getId(), Collections.emptyList()));
                    }
                    return questionCommentVO;
                })
                .collect(Collectors.toList());
        questionCommentVOPage.setRecords(questionCommentVOList);
        return questionCommentVOPage;
    }

    private QuestionCommentVO toCommentVO(QuestionComment questionComment,
                                          Map<Long, User> userMap,
                                          Long loginUserId,
                                          boolean admin) {
        QuestionCommentVO questionCommentVO = QuestionCommentVO.objToVo(questionComment);
        User commentUser = userMap.get(questionComment.getUserId());
        questionCommentVO.setUserVO(userService.getUserVO(commentUser));
        User replyToUser = userMap.get(questionComment.getReplyToUserId());
        questionCommentVO.setReplyToUserVO(userService.getUserVO(replyToUser));
        boolean canDelete = admin || (loginUserId != null && Objects.equals(loginUserId, questionComment.getUserId()));
        questionCommentVO.setCanDelete(canDelete);
        return questionCommentVO;
    }
}
