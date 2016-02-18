package com.permenko.testtaskapp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.google.android.exoplayer.ExoPlayer;
import com.google.gson.Gson;
import com.permenko.testtaskapp.eventbus.MediaPlayerActionEvent;
import com.permenko.testtaskapp.eventbus.MediaPlayerStateEvent;
import com.permenko.testtaskapp.eventbus.PlaylistEvent;
import com.permenko.testtaskapp.eventbus.SearchEvent;
import com.permenko.testtaskapp.models.Playlist;
import com.permenko.testtaskapp.player.MediaPlayer;
import com.permenko.testtaskapp.eventbus.MediaPlayerAction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class StreamService extends Service implements MediaPlayer.MediaPlayerState {

    public static Playlist playlist;
    //private Playlist playlistAfterSearch = null;
    private boolean ignore = false; //ignoring playing track id update, mediaplayer stopped for updating data

    //private final String TAG = StreamService.class.getSimpleName();
    private final String STATE = "Player State";
    private boolean isSongsReceived = false;

    public static MediaPlayer mediaPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isSongsReceived && intent.getStringExtra("url") != null) {
            mediaPlayer.setDataSource(intent.getStringExtra("url"));
            mediaPlayer.startStream();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("playing_track_id", -1).apply();

        EventBus.getDefault().unregister(this);
        mediaPlayer.stopStream();
        mediaPlayer = null;
        super.onDestroy();

    }

    @Subscribe(sticky = true)
    public void onEvent(PlaylistEvent event) {
        if (mediaPlayer == null) { // receiving full playlist
            playlist = event.getPlaylist();
            mediaPlayer = new MediaPlayer(StreamService.this, event.getPlaylist().getSongs());
            mediaPlayer.setStateListener(this);
            isSongsReceived = true;

            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("playlist", new Gson().toJson(event.getPlaylist())).apply();
        }
    }

    @Subscribe
    public void onEvent(SearchEvent event) {
        //playlistAfterSearch = event.getPlaylist();
        playlist = event.getPlaylist();
    }

    @Subscribe
    public void onEvent(MediaPlayerActionEvent event) {
        int action = event.getMediaPlayerAction();
        switch (action) {
            case MediaPlayerAction.PLAY:

                if (mediaPlayer != null) {
                    ignore = true;
                    mediaPlayer.stopStream();
                    mediaPlayer = null;
                    mediaPlayer = new MediaPlayer(StreamService.this, playlist.getSongs());
                    mediaPlayer.setStateListener(this);
                }

                try {
                    if (!event.getMediaPlayerUrl().equals("null")) {
                        mediaPlayer.setDataSource(event.getMediaPlayerUrl());
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                mediaPlayer.startStream();
                break;
            case MediaPlayerAction.STOP:
                mediaPlayer.stopStream();
                break;
            case MediaPlayerAction.NEXT:
                //mediaPlayer.nextTrack();
                break;
            case MediaPlayerAction.PREV:
                //mediaPlayer.previousTrack();
                break;
            default:
                break;
        }
    }

    @Override
    public void buffering() {
        Log.d(STATE, "BUFFERING");
        //ignore
    }

    @Override
    public void ended() {
        Log.d(STATE, "ENDED");
        sendStateEvent(ExoPlayer.STATE_ENDED);
        if (mediaPlayer.getState() != ExoPlayer.STATE_ENDED) {
            mediaPlayer.stopStream();
        }
        if (!ignore) {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("playing_track_id", -1).apply();
            ignore = false;
        }
    }

    @Override
    public void idle() {
        Log.d(STATE, "IDLE");
        sendStateEvent(ExoPlayer.STATE_IDLE);
    }

    @Override
    public void preparing() {
        Log.d(STATE, "PREPARING");
        //ignore
    }

    @Override
    public void ready() {
        Log.d(STATE, "READY");
        //ignore
    }

    @Override
    public void unknown() {
        Log.d(STATE, "UNKNOWN");
        //ignore
    }

    private void sendStateEvent(int state) {
        EventBus.getDefault().postSticky(new MediaPlayerStateEvent(state));
    }

}
