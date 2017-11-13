package cc.fedtech.elasticsearch.service;

import cc.fedtech.elasticsearch.data.PageResponse;
import cc.fedtech.elasticsearch.entity.Article;

import java.util.List;

/**
 * @author: lollipop
 * @date: 17/11/13
 */
public interface ArticleService {

    /**
     * 后台分页查询
     * @param pageNum
     * @param pageSize
     * @param searchContent
     * @return
     */
    PageResponse<Article> searchArticle(Integer pageNum, Integer pageSize, String searchContent);

    /**
     * 新增文章
     * @param article
     * @return
     */
    boolean addArticle(Article article);

    /**
     * 删除索引
     */
    void deleteIndex();

    /**
     * 删除文章
     * @param id
     */
    void deleteById(Long id);

    /**
     * 删除全部文章
     */
    void deleteAll();
}
