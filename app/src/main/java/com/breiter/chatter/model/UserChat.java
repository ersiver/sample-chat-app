package com.breiter.chatter.model;

import androidx.annotation.NonNull;

public class UserChat implements  Comparable<UserChat>{

    private String id;

    private Long time;

    public UserChat(String id, Long time){
        this.id = id;
        this.time = time;
    }

    public UserChat() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }



    @Override
    public int compareTo(@NonNull UserChat other) {

        return (int) (other.time - this.time);
    }
}
