package com.example.curious.Models;

import com.google.firebase.firestore.FieldValue;

public class Comment {
    private String cid;
    private String aid;
    private String uid;
    private String uname;
    private String text;
    private Object timestamp;
    private String date;

    public Comment(String cid, String aid, String uid, String uname, String text) {
        this.cid = cid;
        this.aid = aid;
        this.uid = uid;
        this.uname = uname;
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
}
