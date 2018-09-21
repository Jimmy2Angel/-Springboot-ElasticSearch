package com.lollipop.elasticsearch.service;

import cn.hutool.json.JSONArray;
import com.lollipop.elasticsearch.data.PageResponse;
import com.lollipop.elasticsearch.entity.Article;

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
     * 根据id获取一篇文章
     * @param id 文章id
     * @param addOneClickCount 是否增加1点击量
     * @return
     */
    Article findById(Long id, boolean addOneClickCount);

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
