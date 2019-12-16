package com.breiter.chatter.model;

public class ChatMessage {

    private String sender;
    private String recipient;
    private String message;
    private Long time;
    private boolean isread;
    private String imageURL;
    private String type;


    public ChatMessage(String sender, String recipient, String message, Long time, boolean isread, String imageURL, String type ) {

        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
        this.isread = isread;
        this.time = time;
        this.imageURL = imageURL;
        this.type = type;
    }

    public ChatMessage() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }


    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsread() {
        return isread;
    }

    public void setIsread(boolean isread) {
        this.isread = isread;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}