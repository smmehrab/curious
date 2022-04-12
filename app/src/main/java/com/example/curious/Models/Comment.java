package com.example.curious.Models;

public class Comment {
    private String commentID;
    private String articleID;
    private String userID;
    private String text;
    private String timestamp;

    public Comment(String commentID, String articleID, String userID, String text, String timestamp) {
        this.commentID = commentID;
        this.articleID = articleID;
        this.userID = userID;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
