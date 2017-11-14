package cc.fedtech.elasticsearch.controller;

import cc.fedtech.elasticsearch.data.JsonResult;
import cc.fedtech.elasticsearch.data.PageResponse;
import cc.fedtech.elasticsearch.entity.Article;
import cc.fedtech.elasticsearch.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * @author: lollipop
 * @date: 17/11/12
 */
@Controller
@RequestMapping("a/article")
public class AdminController {

    @Autowired
    private ArticleService articleService;

    @GetMapping(value = {"", "index"})
    public String index() {
        return "admin/index";
    }

    @GetMapping("getByPage")
    @ResponseBody
    public PageResponse<Article> getByPage(@RequestParam(defaultValue = "0") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return articleService.searchArticle(pageNum==0?pageNum:pageNum-1, pageSize, null);
    }

    @GetMapping("add")
    public String add(Long id, Model model) {
        if (id != null) {
            model.addAttribute("article", articleService.getById(id));
        }
        return "admin/article_add";
    }

    @PostMapping("add")
    @ResponseBody
    public JsonResult add(Article article) {
        JsonResult jsonResult = new JsonResult();
        System.out.println("add");
        if(articleService.addArticle(article)) {
            jsonResult.markSuccess("新增成功", null);
        } else {
            jsonResult.markError("新增失败");
        }
        return jsonResult;
    }

    @PostMapping("edit")
    @ResponseBody
    public JsonResult edit(Article article) {
        JsonResult jsonResult = new JsonResult();
        if(articleService.updateArticle(article)) {
            jsonResult.markSuccess("修改成功", null);
        } else {
            jsonResult.markError("修改失败");
        }
        return jsonResult;
    }

    @PostMapping("delete")
    @ResponseBody
    public boolean delete(Long id) {
        articleService.deleteById(id);
        return true;
    }

    @GetMapping("deleteAll")
    @ResponseBody
    public String deleteAll() {
        articleService.deleteAll();
        return "all article has deleted!";
    }

    @GetMapping("deleteIndex")
    @ResponseBody
    public String deleteIndex() {
        articleService.deleteIndex();
        return "the elasticsearch index has deleted!";
    }
}
