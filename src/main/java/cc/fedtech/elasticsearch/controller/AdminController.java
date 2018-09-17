package cc.fedtech.elasticsearch.controller;

import cc.fedtech.elasticsearch.data.JsonResult;
import cc.fedtech.elasticsearch.data.PageResponse;
import cc.fedtech.elasticsearch.entity.Article;
import cc.fedtech.elasticsearch.service.ArticleService;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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

    /**
     * 删除全部文章
     * @return
     */
    @PostMapping("deleteAll")
    @ResponseBody
    public boolean deleteAll() {
        articleService.deleteAll();
        return true;
    }

    /**
     * 批量获取倔强上后端文章保存到 es
     * @return
     */
    @GetMapping("batchSave")
    @ResponseBody
    public boolean batchSave() {
        String url = "https://timeline-merger-ms.juejin.im/v1/get_entry_by_rank?src=web&limit=20&category=5562b419e4b00c57d9b94ae2";
        if (!"".equals(lastRankIndex)) {
            url = url + "&before=" + lastRankIndex;
        }
        return test(url);
    }

    private int index = 1;
    private String lastRankIndex = "";

    private boolean test (String url) {
        String content = HttpUtil.get(url);
        System.out.println(index++ + " : " + content);
        JSONObject resultObject = JSONObject.parseObject(content);
        JSONArray entryArray;
        try {
            entryArray = resultObject.getJSONObject("d").getJSONArray("entrylist");
            assert entryArray != null;
            for (int i = 0; i < entryArray.size(); i++) {
                JSONObject jsonObject = entryArray.getJSONObject(i);
                Article article = new Article();
                article.setAuthor(jsonObject.getJSONObject("user").getString("username"));
                article.setTitle(jsonObject.getString("title"));
                article.setAbstracts(jsonObject.getString("summaryInfo"));
                article.setContent(jsonObject.getString("content"));
                article.setUrl(jsonObject.getString("originalUrl"));
                articleService.addArticle(article);
                lastRankIndex = jsonObject.getString("rankIndex");
                if (index == 50000) {
                    break;
                }
                if (i == entryArray.size() - 1) {
                    test(url + "&before=" + lastRankIndex);
                }
            }
        } catch (Exception e) {
            System.out.println("lastRankIndex : " + lastRankIndex);
            return false;
        }
        return true;
    }

}
