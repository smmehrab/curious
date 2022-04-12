package com.example.curious.Models;

public class Article {
    private String articleID;
    private String userID;
    private String title;
    private String coverURL;
    private String body;
    private String timestamp;
    private Integer likeCount;
    private Integer viewCount;
    private String[] comments = {};

    public Article(String articleID, String userID, String title, String coverURL, String body, String timestamp, Integer likeCount, Integer viewCount, String[] comments) {
        this.articleID = articleID;
        this.userID = userID;
        this.title = title;
        this.coverURL = coverURL;
        this.body = body;
        this.timestamp = timestamp;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.comments = comments;
    }

    public String getArticleID() {
        return articleID;
    }

    public void setArticleID(String articleID) {
        this.articleID = articleID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public String[] getComments() {
        return comments;
    }

    public void setComments(String[] comments) {
        this.comments = comments;
    }

    public Integer getCommentCount() {
        return comments.length;
    }
}
