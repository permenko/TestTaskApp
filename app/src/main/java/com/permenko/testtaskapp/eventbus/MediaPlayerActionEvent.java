package com.permenko.testtaskapp.eventbus;

public class MediaPlayerActionEvent {

    private int action;
    private String url;

    public MediaPlayerActionEvent(int action) {
        this.action = action;
    }

    public MediaPlayerActionEvent(int action, String url) {
        this.action = action;
        this.url = url;
    }

    public int getMediaPlayerAction() {
        return action;
    }

    public String getMediaPlayerUrl() {
        return url;
    }
}
