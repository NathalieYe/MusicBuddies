package com.example.musicbuddies.model;

public class Pref {
    private String gender;
    private String genre;
    private String id;

    // empty constructor to keep android studio(java) happy
    public Pref(){

    }

    public Pref(String gender, String genre, String id){
        this.gender= gender;
        this.genre= genre;
        this.id= id;
    }

    public String getGender() {
        return gender;
    }

    public String getGenre() {
        return genre;
    }

    public void setGender(String s){
        this.gender= s;
    }

    public void setGenre(String s){
        this.genre= s;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
