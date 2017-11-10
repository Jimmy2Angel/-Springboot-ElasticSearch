package cc.fedtech.elasticsearch.dao;

import cc.fedtech.elasticsearch.entity.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Created by lollipop on 17/11/2
 */
public interface ArticleSearchRepository extends ElasticsearchRepository<Article, Long> {

    /**
     * 根据文章标题去查找
     * @param title 文章标题
     * @return 文章集合
     */
    List<Article> findByTitle(String title);

    /**
     * 根据文章标题或者内容去查找
     * @param title 文章标题
     * @param content 文章内容
     * @return
     */
    List<Article> findByTitleOrContent(String title, String content);
}
