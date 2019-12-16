package com.breiter.chatter.notification;

public class Data {
    private String user;
    private String title;
    private String body;
    private int icon;
    private String sent;

    public Data() { }

    public Data(String user, String title, String body, int icon, String sent) {
        this.user = user;
        this.title = title;
        this.body = body;
        this.icon = icon;
        this.sent = sent;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }
}

