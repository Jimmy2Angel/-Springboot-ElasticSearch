package com.lollipop.elasticsearch;

import cn.hutool.json.JSONUtil;
import com.lollipop.elasticsearch.dao.ArticleRepository;
import com.lollipop.elasticsearch.entity.Article;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: lollipop
 * @date: 17/11/14
 */
public class ArticleTest extends ElasticsearchApplicationTests{

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    ArticleRepository articleRepository;

    @Test
    public void testGetIndex() {
        System.out.println(elasticsearchTemplate.getSetting("elasticsearch"));
        System.out.println(elasticsearchTemplate.getMapping("elasticsearch", "article"));
    }

    @Test
    public void testShouldReturnHighlightedFieldsForGivenQueryAndFields() {

        List<Article> newsInfos= new ArrayList<Article>();
        try {
            // 创建查询索引,参数productindex表示要查询的索引库为productindex
            SearchRequestBuilder searchRequestBuilder = elasticsearchTemplate.getClient()
                    .prepareSearch("elasticsearch");

            // 设置查询索引类型,setTypes("productType1", "productType2","productType3");
            // 用来设定在多个类型中搜索
            searchRequestBuilder.setTypes("article");
            // 设置查询类型 1.SearchType.DFS_QUERY_THEN_FETCH = 精确查询 2.SearchType.SCAN
            // = 扫描查询,无序
            searchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
            // 设置查询关键词
//          searchRequestBuilder
//                  .setQuery(QueryBuilders.boolQuery().should(QueryBuilders.termQuery("title", key))
//                          .should(QueryBuilders.termQuery("content", key)));
            QueryStringQueryBuilder queryBuilder = new QueryStringQueryBuilder("spring");
//            queryBuilder.analyzer("ik_smart");
            queryBuilder.field("title").field("content");
            searchRequestBuilder.setQuery(queryBuilder);
            // 分页应用
            searchRequestBuilder.setFrom(1).setSize(3000);

            // 设置是否按查询匹配度排序
            searchRequestBuilder.setExplain(true);
            // 按照字段排序
            searchRequestBuilder.addSort("postTime", SortOrder.DESC);
            // 设置高亮显示
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title").field("content").preTags("<span style=\"color:red\">").postTags("</span>");
            searchRequestBuilder.highlighter(highlightBuilder);
//          searchRequestBuilder.setHighlighterPreTags("<em>");
//          searchRequestBuilder.setHighlighterPostTags("<em>");
            // 执行搜索,返回搜索响应信息
            SearchResponse response = searchRequestBuilder.execute()
                    .actionGet();

            // 获取搜索的文档结果
            SearchHits searchHits = response.getHits();
            SearchHit[] hits = searchHits.getHits();
            // ObjectMapper mapper = new ObjectMapper();
            for (int i = 0; i < hits.length; i++) {
                SearchHit hit = hits[i];
                // 将文档中的每一个对象转换json串值
                String json = hit.getSourceAsString();
                // 将json串值转换成对应的实体对象
                Article newsInfo = JSONUtil.toBean(json, Article.class);
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
                HighlightField contentField = result.get("content");
                if (contentField !=null) {
                    // 取得定义的高亮标签
                    Text[] contentTexts = contentField.fragments();
                    // 为title串值增加自定义的高亮标签
                    String content = "";
                    for (Text text : contentTexts) {
                        content += text;
                    }
                    // 将追加了高亮标签的串值重新填充到对应的对象
                    newsInfo.setContent(content);
                }
                newsInfos.add(newsInfo);
//              System.out.println(newsInfo.toString());
                // 打印高亮标签追加完成后的实体对象
            }
            // 防止出现：远程主机强迫关闭了一个现有的连接
//          Thread.sleep(10000);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        newsInfos.forEach(System.out::println);
    }
}
