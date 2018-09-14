package cc.fedtech.elasticsearch.dao;

import cc.fedtech.elasticsearch.entity.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author: lollipop
 * @date: 17/11/2
 */
public interface ArticleRepository extends ElasticsearchRepository<Article, Long> {
}
