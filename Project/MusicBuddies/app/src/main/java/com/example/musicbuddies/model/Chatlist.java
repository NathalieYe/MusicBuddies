package com.example.musicbuddies.model;

public class Chatlist {
    public String id;

    public Chatlist(String id) {
        this.id = id;
    }

    // empty constructor to keep android studio(java) happy
    public Chatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

