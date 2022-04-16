package com.example.curious.Models;

public class User {
    private String uid;
    private String email;
    private String name;
    private String photo;
    private String device;

    public User(String uid, String email, String name, String photo, String device) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.photo = photo;
        this.device = device;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}
