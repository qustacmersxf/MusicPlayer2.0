package com.example.elephantflysong.musicplayer.Music;

import java.io.File;

/**
 * Created by ElephantFlySong on 2018/6/13.
 */

public class Music {
    private String name;
    private String path;
    private int length;
    private String artist;

    public Music() {
    }

    public Music(String name, String path, int length, String artist) {
        this.name = name;
        this.path = path;
        this.length = length;
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
