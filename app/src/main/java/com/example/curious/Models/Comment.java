package com.example.curious.Models;

import com.google.firebase.firestore.FieldValue;

public class Comment {
    private String cid;
    private String aid;
    private String uid;
    private String text;
    private Object timestamp;

    public Comment(String cid, String aid, String uid, String text) {
        this.cid = cid;
        this.aid = aid;
        this.uid = uid;
        this.text = text;
        this.timestamp = FieldValue.serverTimestamp();
    }

    public Comment() {

    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
