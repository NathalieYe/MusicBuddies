package com.example.musicbuddies.model;


import java.util.List;

//Holds user information retrieved from profile
public class User {
    private String name;
    private String birthday;
    private String genres;
    private String artists;
    private String gender;
    private String bio;
    private String youtubePlaylistURL;
    private String spotifyPlaylistUrl;
    private String id;

    public User (String name, String birthday, String gender, String genres, String artists, String bio, String spotifyPlaylistUrl, String youtubePlaylistURL, String id){
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
        this.genres = genres;
        this.artists = artists;
        this.bio = bio;
        this.spotifyPlaylistUrl = spotifyPlaylistUrl;
        this.youtubePlaylistURL = youtubePlaylistURL;
        this.id = id;

    }

    public User() {

    }

    public String getName(){
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return this.birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGenres() {
        return this.genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getArtists() {
        return this.artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public String getBio() {
        return this.bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }


    public String getSpotifyPlaylistUrl() {
        return this.spotifyPlaylistUrl;
    }

    public void setSpotifyPlaylistUrl(String spotifyPlaylistUrl) {
        this.spotifyPlaylistUrl = spotifyPlaylistUrl;
    }

    public String getYoutubePlaylistURL() {
        return this.youtubePlaylistURL;
    }

    public void setYoutubePlaylistURL(String youtubePlaylistURL) {
        this.youtubePlaylistURL = youtubePlaylistURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}

