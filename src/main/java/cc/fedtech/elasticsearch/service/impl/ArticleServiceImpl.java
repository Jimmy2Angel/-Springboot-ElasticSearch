package cc.fedtech.elasticsearch.service.impl;

import cc.fedtech.elasticsearch.dao.ArticleRepository;
import cc.fedtech.elasticsearch.data.PageResponse;
import cc.fedtech.elasticsearch.entity.Article;
import cc.fedtech.elasticsearch.service.ArticleService;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    private static final String INDEX_NAME = "elasticsearch";
    private static final String TYPE_NAME = "article";

    private static final String PRE_TAG = "<font color='#dd4b39'>";
    private static final String POST_TAG = "</font>";

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    @Override
    public PageResponse<Article> searchArticle(Integer pageNum, Integer pageSize, String searchContent) {
        List<Article> articleList = null;
        int total = 0;
        if ("".equals(searchContent) || searchContent == null) {
            total = (int) articleRepository.count();
            articleList = articleRepository.findAll(new PageRequest(pageNum, pageSize)).getContent();
        } else {
            // 分页参数
            Pageable pageable = new PageRequest(pageNum, pageSize);

            // Function Score Query
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery()
                    .add(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("title", searchContent)),
                            ScoreFunctionBuilders.weightFactorFunction(100))
                    .add(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("abstracts", searchContent)),
                            ScoreFunctionBuilders.weightFactorFunction(50))
                    .add(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("content", searchContent)),
                            ScoreFunctionBuilders.weightFactorFunction(10))
                    .scoreMode("sum").setMinScore(20);

            // 创建搜索 DSL 查询
            SearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withPageable(pageable)
                    .withHighlightFields(new HighlightBuilder.Field("title").preTags(PRE_TAG).postTags(POST_TAG),
                            new HighlightBuilder.Field("abstracts").preTags(PRE_TAG).postTags(POST_TAG))
                    .withQuery(functionScoreQueryBuilder)
                    .build();
            LOGGER.info("\n searchArticle(): searchContent [" + searchContent + "] \n DSL  = \n " + searchQuery.getQuery().toString());

            Page<Article> page = articleRepository.search(searchQuery);
            articleList = page.getContent();
            total = (int) elasticTemplate.count(searchQuery);;
        }
        PageResponse<Article> pageResponse = new PageResponse<>();
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

    @Override
    public Article findOne(Long id) {
        Article article =  articleRepository.findOne(id);
        if (article != null) {
            article.setClickCount(article.getClickCount()+1);
            articleRepository.save(article);
        }
        return article;
    }
}
