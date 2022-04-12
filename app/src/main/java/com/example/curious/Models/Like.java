package com.example.curious.Models;

public class Like {

    private String articleID;
    private String userID;

    public Like(String articleID, String userID) {
        this.articleID = articleID;
        this.userID = userID;
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
}
