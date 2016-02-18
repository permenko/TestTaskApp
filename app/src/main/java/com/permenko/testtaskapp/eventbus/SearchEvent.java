package com.permenko.testtaskapp.eventbus;

import com.permenko.testtaskapp.models.Playlist;

/**
Using for upgrading playlist after search
 */
public class SearchEvent {
    private Playlist playlist;

    public SearchEvent(Playlist playlist) {
        this.playlist = playlist;
    }

    public Playlist getPlaylist() {
        return playlist;
    }
}
