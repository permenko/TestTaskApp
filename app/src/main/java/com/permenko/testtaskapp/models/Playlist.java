package com.permenko.testtaskapp.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class Playlist {

    public Playlist(String json) {
        songs = new Gson().fromJson(json, new TypeToken<ArrayList<Song>>() {}.getType());
    }

    public Playlist() {
    }

    private int count;
    private ArrayList<Song> songs;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }
}
