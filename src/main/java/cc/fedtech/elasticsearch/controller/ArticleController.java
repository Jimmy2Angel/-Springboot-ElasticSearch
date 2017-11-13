package cc.fedtech.elasticsearch.controller;

import cc.fedtech.elasticsearch.dao.ArticleRepository;
import cc.fedtech.elasticsearch.data.PageResponse;
import cc.fedtech.elasticsearch.entity.Article;
import cc.fedtech.elasticsearch.service.ArticleService;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;

/**
 * @author: lollipop
 * @date: 17/11/10
 */
@Controller
@RequestMapping("article")
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleService articleService;

    @GetMapping("")
    public String index (Model model) {
        Iterable<Article> articles =  articleRepository.findAll();
        model.addAttribute("articles", articles);
        return "index";
    }

    @GetMapping("getByPage")
    @ResponseBody
    public PageResponse<Article> getByPage(@RequestParam(defaultValue = "0") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                            String searchContent) {
        return articleService.searchArticle(pageNum==0?pageNum:pageNum-1, pageSize, searchContent);
    }

    @GetMapping("query/{keyWords}")
    @ResponseBody
    public String testSearch(@PathVariable String keyWords) {
        StringBuilder sb = new StringBuilder();
        QueryStringQueryBuilder builder = new QueryStringQueryBuilder(keyWords);
        Iterable<Article> searchResult = articleRepository.search(builder);
        Iterator<Article> iterator = searchResult.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next().toString()+"\n ");
        }
        return sb.toString().length()==0?"No related articles found":sb.toString();
    }

    @GetMapping("{id}")
    @ResponseBody
    public String findById(@PathVariable Long id) {
        Article searchResult = articleRepository.findOne(id);;
        if (searchResult != null) {
            return searchResult.toString();
        }
        return "No related articles found";
    }

    @GetMapping("findAll")
    @ResponseBody
    public String findAll() {
        StringBuilder sb = new StringBuilder();
        Iterable<Article> searchResult = articleRepository.findAll();
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
        Iterable<Article> searchResult = articleRepository.findByTitleOrContent(searchMsg, searchMsg);
        Iterator<Article> iterator = searchResult.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next().toString()+"\n ");
        }
        return sb.toString().length()==0?"No related articles found":sb.toString();
    }

    @GetMapping("delete/{id}")
    @ResponseBody
    public void deleteById(@PathVariable Long id) {
        articleRepository.delete(id);
    }

}
