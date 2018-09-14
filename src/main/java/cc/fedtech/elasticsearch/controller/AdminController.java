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

    /**
     * 跳转后台首页
     * @return
     */
    @GetMapping(value = {"", "index"})
    public String index() {
        return "admin/index";
    }

    /**
     * 分页获取文章列表数据
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("getByPage")
    @ResponseBody
    public PageResponse<Article> getByPage(@RequestParam(defaultValue = "0") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return articleService.searchArticle(pageNum, pageSize, null);
    }

    /**
     * 跳转文章新增／编辑页面
     * @param id
     * @param model
     * @return
     */
    @GetMapping("add")
    public String add(Long id, Model model) {
        if (id != null) {
            model.addAttribute("article", articleService.getById(id));
        }
        return "admin/article_add";
    }

    /**
     * 新增文章
     * @param article
     * @return
     */
    @PostMapping("add")
    @ResponseBody
    public JsonResult add(Article article) {
        JsonResult jsonResult = new JsonResult();
        if (articleService.addArticle(article)) {
            jsonResult.markSuccess("新增成功", null);
        } else {
            jsonResult.markError("新增失败");
        }
        return jsonResult;
    }

    /**
     * 编辑文章
     * @param article
     * @return
     */
    @PostMapping("edit")
    @ResponseBody
    public JsonResult edit(Article article) {
        JsonResult jsonResult = new JsonResult();
        if (articleService.updateArticle(article)) {
            jsonResult.markSuccess("修改成功", null);
        } else {
            jsonResult.markError("修改失败");
        }
        return jsonResult;
    }

    /**
     * 删除文章
     * @param id
     * @return
     */
    @PostMapping("delete")
    @ResponseBody
    public boolean delete(Long id) {
        articleService.deleteById(id);
        return true;
    }

}
