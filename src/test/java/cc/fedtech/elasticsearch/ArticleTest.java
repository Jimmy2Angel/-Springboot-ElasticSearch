package cc.fedtech.elasticsearch;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

/**
 * @author: lollipop
 * @date: 17/11/14
 */
public class ArticleTest extends ElasticsearchApplicationTests{

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void testGetIndex() {
        System.out.println(elasticsearchTemplate.getSetting("elasticsearch"));
        System.out.println(elasticsearchTemplate.getMapping("elasticsearch", "article"));
    }
}
