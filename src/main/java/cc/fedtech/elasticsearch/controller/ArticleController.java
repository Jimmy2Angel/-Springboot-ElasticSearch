package cc.fedtech.elasticsearch.controller;

import cc.fedtech.elasticsearch.data.PageResponse;
import cc.fedtech.elasticsearch.entity.Article;
import cc.fedtech.elasticsearch.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    /**
     * 跳转首页
     *
     * @return
     */
    @GetMapping(value = {"", "index"})
    public String index() {
        return "index";
    }

    /**
     * 分页获取文章列表
     *
     * @param pageNum
     * @param pageSize
     * @param searchContent 检索关键词
     * @return
     */
    @GetMapping("getByPage")
    @ResponseBody
    public List<Object> getByPage(@RequestParam(defaultValue = "0") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  String searchContent) {
        List<Object> list = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        PageResponse<Article> pageResponse = articleService.searchArticleWithHighlight(pageNum, pageSize, searchContent);
        long endTime = System.currentTimeMillis();
        list.add(pageResponse);
        list.add(endTime - startTime);
        return list;
    }

    /**
     * 根据id查看文章详情
     *
     * @param id
     * @param model
     * @return
     */
    @GetMapping("{id}")
    public String findById(@PathVariable Long id, Model model) {
        Article searchResult = articleService.findOne(id);
        if (searchResult != null) {
            model.addAttribute("article", searchResult);
        }
        return "article_detail";
    }

}
