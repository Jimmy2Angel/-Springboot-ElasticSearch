package cc.fedtech.elasticsearch.controller;

import cc.fedtech.elasticsearch.dao.ArticleSearchRepository;
import cc.fedtech.elasticsearch.entity.Article;
import cc.fedtech.elasticsearch.entity.Author;
import cc.fedtech.elasticsearch.entity.Tutorial;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Iterator;

/**
 * @author: lollipop
 * @date: 17/11/10
 */
@Controller
@RequestMapping("article")
public class ArticleController {

    @Autowired
    private ArticleSearchRepository articleSearchRepository;

    @GetMapping("")
    public String index (Model model) {
        Iterable<Article> articles =  articleSearchRepository.findAll();
        model.addAttribute("articles", articles);
        return "index";
    }


    @GetMapping("add")
    public void testSaveArticleIndex() {
        Author author = new Author();
        author.setId(1L);
        author.setName("baojie");
        author.setRemark("javaer");

        Tutorial tutorial = new Tutorial();
        tutorial.setId(1L);
        tutorial.setName("elastic search");

        Article article = new Article();
        article.setId(2L);
        article.setTitle("曾经沧海难为水，除却巫山不是云");
        article.setAbstracts("经历过无比深广的沧海的人,别处的水再难以吸引他;除了云蒸霞蔚的巫山之云,别处的云都黯然失色");
        article.setTutorial(tutorial);
        article.setAuthor(author);
        article.setContent("这两句诗出自唐元鸲离思》,诗为悼念亡妻韦丛而作,全诗如下: 曾经沧海难为水,除却巫山不是云。 取次花丛懒回顾,半缘修道半缘君");
        article.setPostTime(new Date());
        article.setClickCount(3L);

        articleSearchRepository.save(article);
    }

    @GetMapping("query/{keyWords}")
    @ResponseBody
    public String testSearch(@PathVariable String keyWords) {
        StringBuilder sb = new StringBuilder();
        QueryStringQueryBuilder builder = new QueryStringQueryBuilder(keyWords);
        Iterable<Article> searchResult = articleSearchRepository.search(builder);
        Iterator<Article> iterator = searchResult.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next().toString()+"\n ");
        }
        return sb.toString().length()==0?"No related articles found":sb.toString();
    }

    @GetMapping("findById/{id}")
    @ResponseBody
    public String findById(@PathVariable Long id) {
        Article searchResult = articleSearchRepository.findOne(id);;
        if (searchResult != null) {
            return searchResult.toString();
        }
        return "No related articles found";
    }

    @GetMapping("findByTitle/{title}")
    @ResponseBody
    public String findByTitle(@PathVariable String title) {
        StringBuilder sb = new StringBuilder();
        Iterable<Article> searchResult = articleSearchRepository.findByTitle(title);
        Iterator<Article> iterator = searchResult.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next().toString()+"\n ");
        }
        return sb.toString().length()==0?"No related articles found":sb.toString();
    }

    @GetMapping("findByTitleOrContent/{searchMsg}")
    @ResponseBody
    public String findByTitleOrContent(@PathVariable String searchMsg) {
        StringBuilder sb = new StringBuilder();
        Iterable<Article> searchResult = articleSearchRepository.findByTitleOrContent(searchMsg, searchMsg);
        Iterator<Article> iterator = searchResult.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next().toString()+"\n ");
        }
        return sb.toString().length()==0?"No related articles found":sb.toString();
    }

}
