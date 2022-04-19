package com.example.curious.Models;

import com.google.firebase.firestore.FieldValue;

public class ArticleItem {
    private String aid;
    private String uid;
    private String title;
    private String coverUrl;
    private String uname;
    private String date;
    private Object timestamp;
    private Integer likeCount;
    private String status;

    public ArticleItem(String aid, String uid, String title, String coverUrl, String uname, String status) {
        this.aid = aid;
        this.uid = uid;
        this.title = title;
        this.coverUrl = coverUrl;
        this.uname = uname;
        this.date = "";
        this.timestamp = FieldValue.serverTimestamp();
        this.likeCount = 0;
        this.status = status;
    }

    public ArticleItem() {

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
