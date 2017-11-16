package cc.fedtech.elasticsearch.service.impl;

import cc.fedtech.elasticsearch.dao.ArticleRepository;
import cc.fedtech.elasticsearch.data.PageResponse;
import cc.fedtech.elasticsearch.entity.Article;
import cc.fedtech.elasticsearch.service.ArticleService;
import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
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

import java.util.*;

/**
 * @author: lollipop
 * @date: 17/11/13
 */
@Service
public class ArticleServiceImpl implements ArticleService{

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleServiceImpl.class);

    private static final String INDEX_NAME = "elasticsearch";
    private static final String TYPE_NAME = "article";

    private static final String PRE_TAG = "<span style=\"color:red\">";
    private static final String POST_TAG = "</span>";

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public PageResponse<Article> searchArticle(Integer pageNum, Integer pageSize, String searchContent) {
        List<Article> articleList = null;
        // 分页参数
        Pageable pageable = new PageRequest(pageNum, pageSize);
        int total = 0;
        if ("".equals(searchContent) || searchContent == null) {
            return findAllByPaging(pageNum, pageSize);
        } else {
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
                    .withQuery(functionScoreQueryBuilder)
                    .build();
            LOGGER.info("\n searchArticle(): searchContent [" + searchContent + "] \n DSL  = \n " + searchQuery.getQuery().toString());

            Page<Article> page = articleRepository.search(searchQuery);
            articleList = page.getContent();
            total = (int) elasticsearchTemplate.count(searchQuery);;
            PageResponse<Article> pageResponse = new PageResponse<>();
            pageResponse.setRecordsFiltered(total);
            pageResponse.setRecordsTotal(total);
            pageResponse.setPageNum(pageNum+1);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotal(total);
            pageResponse.setData(articleList.size()==0?null:articleList);
            return pageResponse;
        }
    }

    @Override
    public PageResponse<Article> searchArticleWithHighlight(Integer pageNum, Integer pageSize, String searchContent) {
        PageResponse<Article> pageResponse = new PageResponse<>();
        List<Article> articleList= new ArrayList<Article>();
        if ("".equals(searchContent) || searchContent == null) {
            return findAllByPaging(pageNum, pageSize);
        } else {
            try {
                // 创建查询索引
                SearchRequestBuilder searchRequestBuilder = elasticsearchTemplate.getClient()
                        .prepareSearch(INDEX_NAME);

                // 设置查询索引类型,setTypes("productType1", "productType2","productType3");
                // 用来设定在多个类型中搜索
                searchRequestBuilder.setTypes(TYPE_NAME);

                // 设置查询类型 1.SearchType.DFS_QUERY_THEN_FETCH = 精确查询 2.SearchType.SCAN = 扫描查询,无序
                searchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);

                // 设置查询关键词
//                searchRequestBuilder
//                         .setQuery(QueryBuilders.boolQuery().should(QueryBuilders.termQuery("title", searchContent))
///                          .should(QueryBuilders.termQuery("abstracts", searchContent)));
                QueryStringQueryBuilder queryBuilder = new QueryStringQueryBuilder(searchContent);
                queryBuilder.field("title").field("abstracts");

                //分词器
                queryBuilder.analyzer("ik_smart");
                searchRequestBuilder.setQuery(queryBuilder);

                // 分页应用
                searchRequestBuilder.setFrom(pageNum*pageSize+1).setSize(pageSize);

                // 设置是否按查询匹配度排序
                searchRequestBuilder.setExplain(true);
                // 按照字段排序
                searchRequestBuilder.addSort("postTime", SortOrder.DESC);
                // 设置高亮显示
                searchRequestBuilder.addHighlightedField("title").addHighlightedField("abstracts");
                searchRequestBuilder.setHighlighterPreTags(PRE_TAG).setHighlighterPostTags(POST_TAG);
                // 执行搜索,返回搜索响应信息
                SearchResponse response = searchRequestBuilder.execute().actionGet();

                // 获取搜索的文档结果
                SearchHits searchHits = response.getHits();
                SearchHit[] hits = searchHits.getHits();
                // ObjectMapper mapper = new ObjectMapper();
                for (int i = 0; i < hits.length; i++) {
                    SearchHit hit = hits[i];
                    // 将文档中的每一个对象转换json串值
                    String json = hit.getSourceAsString();
                    // 将json串值转换成对应的实体对象
                    Article newsInfo = JSONObject
                            .parseObject(json, Article.class);
                    // 获取对应的高亮域
                    Map<String, HighlightField> result = hit.highlightFields();
                    // 从设定的高亮域中取得指定域
                    HighlightField titleField = result.get("title");
                    if (titleField !=null) {
                        // 取得定义的高亮标签
                        Text[] titleTexts = titleField.fragments();
                        // 为title串值增加自定义的高亮标签
                        String title = "";
                        for (Text text : titleTexts) {
                            title += text;
                        }
                        newsInfo.setTitle(title);
                    }
                    // 从设定的高亮域中取得指定域
                    HighlightField contentField = result.get("abstracts");
                    if (contentField !=null) {
                        // 取得定义的高亮标签
                        Text[] contentTexts = contentField.fragments();
                        // 为title串值增加自定义的高亮标签
                        String abstracts = "";
                        for (Text text : contentTexts) {
                            abstracts += text;
                        }
                        // 将追加了高亮标签的串值重新填充到对应的对象
                        newsInfo.setAbstracts(abstracts);
                    }
                    articleList.add(newsInfo);
                    // 打印高亮标签追加完成后的实体对象
                }
                // 防止出现：远程主机强迫关闭了一个现有的连接
///          Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int total = articleList.size();
            pageResponse.setRecordsFiltered(total);
            pageResponse.setRecordsTotal(total);
            pageResponse.setPageNum(pageNum+1);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotal(total);
            pageResponse.setData(articleList.size()==0?null:articleList);
            return pageResponse;
        }
    }

    public PageResponse<Article> findAllByPaging(Integer pageNum, Integer pageSize) {
        List<Article> articleList = articleRepository.findAll(new PageRequest(pageNum, pageSize)).getContent();
        int total = (int) articleRepository.count();
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
    public Iterable<Article> findAll() {
        return articleRepository.findAll();
    }

    @Override
    public Iterable<Article> findByTitleOrContent(String title, String content) {
        return articleRepository.findByTitleOrContent(title, content);
    }

    @Override
    public void delete(Long id) {
        articleRepository.delete(id);
    }

    @Override
    public Iterable<Article> search(String keyWords) {
        QueryStringQueryBuilder builder = new QueryStringQueryBuilder(keyWords);
        return articleRepository.search(builder);
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
        elasticsearchTemplate.deleteIndex(INDEX_NAME);
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

    @Override
    public Article getById(Long id) {
        return articleRepository.findOne(id);
    }

    @Override
    public boolean updateArticle(Article article) {
        Article selectArticle = articleRepository.findOne(article.getId());
        selectArticle.setAuthor(article.getAuthor());
        selectArticle.setAbstracts(article.getAbstracts());
        selectArticle.setContent(article.getContent());
        selectArticle.setTitle(article.getTitle());
        try {
            articleRepository.save(selectArticle);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
