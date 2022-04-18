package com.example.curious.Models;

public class Like {

    private String aid;
    private String uid;

    public Like(String aid, String uid) {
        this.aid = aid;
        this.uid = uid;
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
}
