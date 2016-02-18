package com.permenko.testtaskapp.player;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.permenko.testtaskapp.models.Song;

import java.util.ArrayList;

public class MediaPlayer implements ExoPlayer.Listener {

    //private final String TAG = MediaPlayer.class.getSimpleName();
    private ExoPlayer mediaPlayer;
    private final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private final int BUFFER_SEGMENT_COUNT = 160;
    private final String USER_AGENT = "Android";
    private int currentSongPosition = -1;
    private boolean playWhenReady = false;
    private int state;
    private Context context;
    private ArrayList<String> urls;

    public MediaPlayer(Context context, ArrayList<Song> songs) {
        this.context = context;
        // getting array of urls from array of Songs
        ArrayList<String> urls = new ArrayList<>();
        for (int i = 0; i < songs.size(); ++i) {
            urls.add(songs.get(i).getUrl());
        }
        this.urls = urls;
    }

    private void prepareMediaPlayer() {
        mediaPlayer = ExoPlayer.Factory.newInstance(1);
        mediaPlayer.addListener(this);
        Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        DataSource dataSource = new DefaultUriDataSource(context, null, USER_AGENT);
        ExtractorSampleSource sampleSource = new ExtractorSampleSource(Uri.parse(urls.get(currentSongPosition)), dataSource, allocator,
                BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);
        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT);
        mediaPlayer.prepare(audioRenderer);
        mediaPlayer.setPlayWhenReady(getPlayWhenReady());
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void startStream() {
        setPlayWhenReady(true);
        if (mediaPlayer == null) {
            prepareMediaPlayer();
        } else {
            mediaPlayer.setPlayWhenReady(getPlayWhenReady()); // play stream
        }
    }

    public void restartStream() {
        releaseMediaPlayer();
        if (getPlayWhenReady()) {
            startStream();
        }
    }
    
    public void stopStream() {
        if (mediaPlayer != null) {
            releaseMediaPlayer();
            setPlayWhenReady(false);
            mediaPlayerState.ended();
            setState(ExoPlayer.STATE_ENDED);
        }
    }

    public void nextTrack() {
        currentSongPosition = (currentSongPosition + 1) % urls.size();
        restartStream();
    }

    public void previousTrack() {
        currentSongPosition = (currentSongPosition + urls.size() - 1) % urls.size();
        restartStream();
    }

    private void setPlayWhenReady(Boolean playWhenReady) {
        this.playWhenReady = playWhenReady;
    }

    public boolean getPlayWhenReady() {
        return playWhenReady;
    }
    
    public int getCurrentSongPosition() {
        return currentSongPosition;
    }

    public void setCurrentSongPosition(int position) {
        this.currentSongPosition = position;
    }

    public int getState() {
        return state;
    }

    private void setState(int state) {
        this.state = state;
    }

    public interface MediaPlayerState {
        void buffering();
        void ended();
        void idle();
        void preparing();
        void ready();
        void unknown();
    }

    public MediaPlayerState mediaPlayerState;

    public void setStateListener(MediaPlayerState mediaPlayerState) {
        this.mediaPlayerState = mediaPlayerState;
    }

    @Override
    public void onPlayerStateChanged(boolean b, int playerState) {
        switch(playerState) {
            case ExoPlayer.STATE_BUFFERING:
                mediaPlayerState.buffering();
                setState(ExoPlayer.STATE_BUFFERING);
                break;
            case ExoPlayer.STATE_ENDED:
                mediaPlayerState.ended();
                setState(ExoPlayer.STATE_ENDED);
                break;
            case ExoPlayer.STATE_IDLE:
                mediaPlayerState.idle();
                setState(ExoPlayer.STATE_IDLE);
                break;
            case ExoPlayer.STATE_PREPARING:
                mediaPlayerState.preparing();
                setState(ExoPlayer.STATE_PREPARING);
                break;
            case ExoPlayer.STATE_READY:
                mediaPlayerState.ready();
                setState(ExoPlayer.STATE_READY);
                break;
            default:
                mediaPlayerState.unknown();
                break;
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {
    }
    
    @Override
    public void onPlayerError(ExoPlaybackException e) {
        mediaPlayerState.ended();
        Toast.makeText(context, "ExoPlaybackException, try to restart", Toast.LENGTH_SHORT).show();
    }

    public void setDataSource(String url) {
        int temp = urls.indexOf(url); // temp == -1 when no matches
        if (temp != -1) {
            currentSongPosition = urls.indexOf(url);
        }
        setPlayWhenReady(true);
        restartStream();
    }

}
