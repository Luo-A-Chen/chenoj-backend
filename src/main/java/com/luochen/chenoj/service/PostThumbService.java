package com.luochen.chenoj.service;

import com.luochen.chenoj.model.entity.PostThumb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luochen.chenoj.model.entity.User;

/**
 * 帖子点赞服务
 *
 * @author <a href="https://github.com/luochen">程序员啊琛</a>
 * @from <a href="https://chenduoduo.icu">琛多多的oj网站</a>
 */
public interface PostThumbService extends IService<PostThumb> {

    /**
     * 点赞
     *
     * @param postId
     * @param loginUser
     * @return
     */
    int doPostThumb(long postId, User loginUser);

    /**
     * 帖子点赞（内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doPostThumbInner(long userId, long postId);
}
