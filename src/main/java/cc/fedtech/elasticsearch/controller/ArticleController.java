package cc.fedtech.elasticsearch.controller;

import cc.fedtech.elasticsearch.data.PageResponse;
import cc.fedtech.elasticsearch.entity.Article;
import cc.fedtech.elasticsearch.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author: lollipop
 * @date: 17/11/10
 */
@Controller
@RequestMapping("article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @GetMapping(value = {"","index"})
    public String index (Model model) {
        Iterable<Article> articles =  articleService.findAll();
        model.addAttribute("articles", articles);
        return "index";
    }

    @GetMapping("getByPage")
    @ResponseBody
    public List<Object> getByPage(@RequestParam(defaultValue = "0") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  String searchContent) {
        List<Object> list = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        PageResponse<Article> pageResponse = articleService.searchArticleWithHighlight(pageNum==0?pageNum:pageNum-1, pageSize, searchContent);
        long endTime = System.currentTimeMillis();
        list.add(pageResponse);
        list.add(endTime-startTime);
        return list;
    }

    @GetMapping("query/{keyWords}")
    @ResponseBody
    public String testSearch(@PathVariable String keyWords) {
        StringBuilder sb = new StringBuilder();
        Iterable<Article> searchResult = articleService.search(keyWords);
        Iterator<Article> iterator = searchResult.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next().toString()+"\n ");
        }
        return sb.toString().length()==0?"No related articles found!!":sb.toString();
    }

    @GetMapping("{id}")
    public String findById(@PathVariable Long id, Model model) {
        Article searchResult = articleService.findOne(id);
        if (searchResult != null) {
            model.addAttribute("article", searchResult);
        }
        return "article_detail";
    }

    @GetMapping("findAll")
    @ResponseBody
    public String findAll() {
        StringBuilder sb = new StringBuilder();
        Iterable<Article> searchResult = articleService.findAll();
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
        Iterable<Article> searchResult = articleService.findByTitleOrContent(searchMsg, searchMsg);
        Iterator<Article> iterator = searchResult.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next().toString()+"\n ");
        }
        return sb.toString().length()==0?"No related articles found":sb.toString();
    }

    @GetMapping("delete/{id}")
    @ResponseBody
    public void deleteById(@PathVariable Long id) {
        articleService.delete(id);
    }

}
