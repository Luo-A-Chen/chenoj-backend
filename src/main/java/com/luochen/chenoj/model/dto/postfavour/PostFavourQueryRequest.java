package com.luochen.chenoj.model.dto.postfavour;

import com.luochen.chenoj.common.PageRequest;
import com.luochen.chenoj.model.dto.post.PostQueryRequest;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子收藏查询请求
 *
 * @author <a href="https://github.com/luochen">程序员啊琛</a>
 * @from <a href="https://chenduoduo.icu">琛多多的oj网站</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostFavourQueryRequest extends PageRequest implements Serializable {

    /**
     * 帖子查询请求
     */
    private PostQueryRequest postQueryRequest;

    /**
     * 用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}