# Springboot-ElasticSearch

## 版本

由于存在版本不兼容的问题，此 demo 采用版本如下

1. springboot ： 1.5.8.RELEASE
2. elasticsearch：2.4.0
3. ik 分词器：1.9.5（启动时报错，可修改 ik 插件下配置文件中 es 的版本为 2.4.0）

## springboot相关

### 热部署

添加依赖

```java
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-devtools</artifactId>
	<optional>true</optional> <!-- 这个需要为 true 热部署才有效 -->
</dependency>
```

这样就可以不用手动重启应用了，监测到代码有变化会自动重启应用，并且这个重启比手动重启要快很多。

配合 chrome 浏览器 liveReload 插件（监测页面有变化自动刷新页面）可以实现边写代码边看效果。

### Thymeleaf 模版

thymeleaf 作为官方推荐的模板引擎，自有它的优点，这里不做多说。springboot 无需其它配置 controller 返回视图名默认查找 templates 路径下的 html。

## ElasticSearch相关

### 存储路径

在 application.yml 文件中配置如下

```java
spring:
   data:
      elasticsearch:
        #cluster-name: elasticsearch
        #cluster-nodes: 127.0.0.1:9300 #配置es节点信息，逗号分隔，如果没有指定，则启动ClientNode
        properties:
          path:
            logs: ./elasticsearch/log #elasticsearch日志存储目录
            data: ./elasticsearch/data #elasticsearch数据存储目录
```

若配置集群后，则默认在 es 根路径下，此处存储路径配置失效。可在 es 路径下 config 文件夹下 elasticsearch.yml 中配置数据及日志存储路径。

### IK分词器

在 es 路径下 plugins 文件夹下新建 ik 文件夹，将下载好的 ik 压缩包解压放在此处即可。

### 关键词搜索高亮

spring-data-es 尝试了一番，但是好像不支持，最终还是用 原生的 es 实现了改功能。代码如下：

```java
private static final String INDEX_NAME = "elasticsearch";
private static final String TYPE_NAME = "article";
private static final String PRE_TAG = "<span style=\"color:red\">";
private static final String POST_TAG = "</span>";

public PageResponse<Article> searchArticleWithHighlight(Integer pageNum, Integer pageSize, String searchContent) {
        PageResponse<Article> pageResponse = new PageResponse<>();
        List<Article> articleList = new ArrayList<>();
        int total = 0;
        pageNum = pageNum == 0 ? pageNum : pageNum - 1;
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
                searchRequestBuilder.setFrom(pageNum * pageSize + 1).setSize(pageSize);

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
                total = (int) searchHits.getTotalHits();
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
                    if (titleField != null) {
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
                    if (contentField != null) {
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
            pageResponse.setRecordsFiltered(total);
            pageResponse.setRecordsTotal(total);
            pageResponse.setPageNum(pageNum + 1);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotal(total);
            pageResponse.setData(articleList.size() == 0 ? null : articleList);
            return pageResponse;
        }
    }
```



## 其它问题

1. long 类型传到页面丢失精度问题，解决如下：

   ```java
   public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

           MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
           ObjectMapper objectMapper = new ObjectMapper();
           //将Null指改为空字符串
           objectMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
               @Override
               public void serialize(Object value, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {
                   jg.writeString("");
               }
           });
           //格式化Date类型的数据
           objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
           SimpleModule simpleModule = new SimpleModule();
           //序列换成json时,将所有的long变成string 因为js中得数字类型不能包含所有的java long值
           simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
           simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
           simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
           objectMapper.registerModule(simpleModule);
           jackson2HttpMessageConverter.setObjectMapper(objectMapper);
           converters.add(jackson2HttpMessageConverter);
       }
   ```

2. 关键词搜索时有时不如人意，可能是分词器的原因，暂未处理。
