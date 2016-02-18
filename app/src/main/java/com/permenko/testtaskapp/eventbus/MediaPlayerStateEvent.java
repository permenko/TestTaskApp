package com.permenko.testtaskapp.eventbus;

public class MediaPlayerStateEvent {
    private int state;

    public MediaPlayerStateEvent(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
