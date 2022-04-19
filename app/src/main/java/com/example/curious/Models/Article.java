package com.example.curious.Models;

import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.FieldValue;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class Article {
    private String aid;
    private String uid;
    private String title;
    private String coverUrl;
    private String body;
    private String uname;
    private String date;
    private Object timestamp;
    private Integer likeCount;
    private Integer viewCount;
    private Integer commentCount;

    public Article(String aid, String uid, String title, String coverUrl, String body, String uname) {
        this.aid = aid;
        this.uid = uid;
        this.title = title;
        this.coverUrl = coverUrl;
        this.body = body;
        this.uname = uname;
        this.date = "";
        this.timestamp = FieldValue.serverTimestamp();
        this.likeCount = 0;
        this.viewCount = 0;
        this.commentCount = 0;
    }

    public Article() {

    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }
}
