package cc.fedtech.elasticsearch.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: lollipop
 * @date: 17/11/2
 */
@Document(indexName="elasticsearch",type="article",indexStoreType="fs",shards=5,replicas=1,refreshInterval="-1")
public class Article implements Serializable {
    @Id
    private Long id;
    /**标题*/
    @Field
    private String title;
    /**摘要*/
    @Field
    private String abstracts;
    /**内容*/
    @Field
    private String content;
    /**发表时间*/
    private Date postTime;
    /**点击率*/
    @Field
    private Long clickCount;
    /**作者*/
    private String author;
    /**所属教程*/
    private String tutorial;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstracts() {
        return abstracts;
    }

    public void setAbstracts(String abstracts) {
        this.abstracts = abstracts;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getPostTime() {
        return postTime;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTutorial() {
        return tutorial;
    }

    public void setTutorial(String tutorial) {
        this.tutorial = tutorial;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", abstracts='" + abstracts + '\'' +
                ", content='" + content + '\'' +
                ", postTime=" + postTime +
                ", clickCount=" + clickCount +
                ", author='" + author + '\'' +
                ", tutorial='" + tutorial + '\'' +
                '}';
    }
}
