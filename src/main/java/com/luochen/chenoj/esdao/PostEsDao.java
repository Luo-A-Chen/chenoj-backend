package com.luochen.chenoj.esdao;

import com.luochen.chenoj.model.dto.post.PostEsDTO;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 帖子 ES 操作
 *
 * @author <a href="https://github.com/luochen">程序员啊琛</a>
 * @from <a href="https://chenduoduo.icu">琛多多的oj网站</a>
 */
public interface PostEsDao extends ElasticsearchRepository<PostEsDTO, Long> {

    List<PostEsDTO> findByUserId(Long userId);
}