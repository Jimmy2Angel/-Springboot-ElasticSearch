package cc.fedtech.elasticsearch.service.impl;

import cc.fedtech.elasticsearch.dao.ArticleRepository;
import cc.fedtech.elasticsearch.data.PageResponse;
import cc.fedtech.elasticsearch.entity.Article;
import cc.fedtech.elasticsearch.service.ArticleService;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author: lollipop
 * @date: 17/11/13
 */
@Service
public class ArticleServiceImpl implements ArticleService{

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleServiceImpl.class);

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    @Override
    public PageResponse<Article> searchArticle(Integer pageNum, Integer pageSize, String searchContent) {
        List<Article> articleList = null;
        if (searchContent == null) {
            articleList = articleRepository.findAll(new PageRequest(pageNum, pageSize)).getContent();
        } else {
            // 分页参数
            Pageable pageable = new PageRequest(pageNum, pageSize);

            // Function Score Query
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery()
                    .add(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("title", searchContent)),
                            ScoreFunctionBuilders.weightFactorFunction(1000))
                    .add(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("abstracts", searchContent)),
                            ScoreFunctionBuilders.weightFactorFunction(100))
                    .add(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("content", searchContent)),
                            ScoreFunctionBuilders.weightFactorFunction(10));

            // 创建搜索 DSL 查询
            SearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withPageable(pageable)
                    .withQuery(functionScoreQueryBuilder).build();
            articleList = articleRepository.search(searchQuery).getContent();
        }

        PageResponse<Article> pageResponse = new PageResponse<>();
        int total = (int) articleRepository.count();
        pageResponse.setRecordsFiltered(total);
        pageResponse.setRecordsTotal(total);
        pageResponse.setPageNum(pageNum+1);
        pageResponse.setPageSize(pageSize);
        pageResponse.setTotal(total);
        pageResponse.setData(articleList.size()==0?null:articleList);
        return pageResponse;
    }

    @Override
    public boolean addArticle(Article article) {
        article.setId(new Random().nextLong());
        article.setClickCount(0L);
        article.setPostTime(new Date());
        try {
            articleRepository.save(article);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void deleteIndex() {
        elasticTemplate.deleteIndex("elasticsearch");
    }

    @Override
    public void deleteById(Long id) {
        articleRepository.delete(id);
    }

    @Override
    public void deleteAll() {
        articleRepository.deleteAll();
    }
}
