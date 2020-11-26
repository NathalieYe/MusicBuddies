package com.example.musicbuddies.model;

public class Match {
    private String id;

    // empty constructor to keep android studio(java) happy
    public Match() {
    }

    public Match(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
