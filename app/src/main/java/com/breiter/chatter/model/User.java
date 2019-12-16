package com.breiter.chatter.model;

import androidx.annotation.NonNull;

public class User implements Comparable<User> {

    private String username;
    private String userId;
    private String imageURL;
    private String status;
    private String search;

    public User(){}

    public User(String username, String userId, String imageURL, String status, String search) {
        this.username = username;
        this.userId = userId;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    @Override
    public int compareTo(@NonNull User other) {
        return getUsername().compareTo(other.getUsername());
    }
}
