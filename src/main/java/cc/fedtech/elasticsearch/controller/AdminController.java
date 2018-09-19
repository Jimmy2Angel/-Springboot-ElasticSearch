package cc.fedtech.elasticsearch.controller;

import cc.fedtech.elasticsearch.data.JsonResult;
import cc.fedtech.elasticsearch.data.PageResponse;
import cc.fedtech.elasticsearch.entity.Article;
import cc.fedtech.elasticsearch.service.ArticleService;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author: lollipop
 * @date: 17/11/12
 */
@Controller
@RequestMapping("a/article")
public class AdminController {

    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());

    /**
     * 已调用获取掘金文章的次数
     */
    private int index = 1;
    /**
     * 预计调用掘金接口次数
     */
    private static final int THE_CALLS_NUMBER = 100;
    /**
     * 获取掘金文章接口url
     */
    private static final String GET_ARTICLE_URL = "https://timeline-merger-ms.juejin.im/v1/get_entry_by_rank?src=web&limit=50&category=all&before=";
    /**
     * 标记上次获取到的最后一篇文章，用于下次接着此篇文章获取
     */
    private String lastRankIndex = "";
    /**
     * 线程池用于保存文章到es
     */
    private ExecutorService executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            ThreadUtil.newNamedThreadFactory("save-article-pool-", true));

    @Autowired
    private ArticleService articleService;

    /**
     * 跳转后台首页
     *
     * @return
     */
    @GetMapping(value = {"", "index"})
    public String index() {
        return "admin/index";
    }

    /**
     * 分页获取文章列表数据
     *
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
     *
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
     *
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
     *
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
     *
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
     *
     * @return
     */
    @PostMapping("deleteAll")
    @ResponseBody
    public boolean deleteAll() {
        articleService.deleteAll();
        return true;
    }

    /**
     * 调用掘金接口，解析获取的文章集合，保存到 es
     *
     * @return
     */
    @GetMapping("batchSave")
    @ResponseBody
    public boolean batchSaveToES() {
        try {
            if (index > THE_CALLS_NUMBER) {
                executorService.shutdown();
            } else {
                String content = HttpUtil.get(GET_ARTICLE_URL + lastRankIndex);
                LOGGER.info(index + " : " + content);
                JSONObject resultObject = JSONObject.parseObject(content);
                index++;
                //调用一次接口获取50篇文章
                JSONArray entryArray = resultObject.getJSONObject("d").getJSONArray("entrylist");
                assert entryArray != null;
                //保存这次获取到的文章
                executorService.execute(() -> articleService.addArticles(entryArray));
                //根据这次获取到的最后一篇文章的 rankIndex 再次调用接口
                lastRankIndex = entryArray.getJSONObject(entryArray.size() - 1).getString("rankIndex");
                executorService.execute(this::batchSaveToES);
            }
        } catch (Exception e) {
            //调用掘金接口可能会出现异常，需要等待3s后根据上次的 rankIndex 重新调用接口
            e.printStackTrace();
            LOGGER.warning("CATCH EXCEPTION!!! CATCH EXCEPTION!!! CATCH EXCEPTION!!!");
            try {
                Thread.sleep(3000);
                batchSaveToES();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        return true;
    }
}
