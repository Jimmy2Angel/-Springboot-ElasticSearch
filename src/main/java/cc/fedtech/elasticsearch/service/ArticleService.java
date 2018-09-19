package cc.fedtech.elasticsearch.service;

import cc.fedtech.elasticsearch.data.PageResponse;
import cc.fedtech.elasticsearch.entity.Article;
import com.alibaba.fastjson.JSONArray;

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
     * 前台分页获取，关键词高亮
     * @param pageNum
     * @param pageSize
     * @param searchContent
     * @return
     */
    PageResponse<Article>  searchArticleWithHighlight(Integer pageNum, Integer pageSize, String searchContent);

    /**
     * 新增文章
     * @param article
     * @return
     */
    boolean addArticle(Article article);

    /**
     * 批量新增文章
     * @param jsonArray
     * @return
     */
    void addArticles(JSONArray jsonArray);

    /**
     * 后台删除文章
     * @param id
     */
    void deleteById(Long id);

    /**
     * 根据id获取一篇文章, 点击量加1
     * @param id
     * @return
     */
    Article findOne(Long id);

    /**
     * 根据id获取一篇文章, 点击量不变
     * @param id
     * @return
     */
    Article getById(Long id);

    /**
     * 后台修改文章
     * @param article
     * @return
     */
    boolean updateArticle(Article article);

    /**
     * 删除全部文章
     */
    void deleteAll();

}
