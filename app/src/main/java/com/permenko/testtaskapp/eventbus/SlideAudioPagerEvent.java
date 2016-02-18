package com.permenko.testtaskapp.eventbus;

public class SlideAudioPagerEvent {

    public static final int TO_NEXT_PAGE = 1;
    public static final int TO_PREVIOUS_PAGE = -1;

    private int position;
    private int direction;

    public SlideAudioPagerEvent(int position, int direction) {
        this.position = position;
        this.direction = direction;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}
