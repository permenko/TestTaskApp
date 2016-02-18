package com.permenko.testtaskapp.eventbus;

import com.permenko.testtaskapp.models.Playlist;

/**
 Using for sending playlist to StreamService
 */

public class PlaylistEvent {
    private Playlist playlist;

    public PlaylistEvent(Playlist playlist) {
        this.playlist = playlist;
    }

    public Playlist getPlaylist() {
        return playlist;
    }
}
