package com.example.elephantflysong.musicplayer.Music;

import java.io.Serializable;

/**
 * Created by ElephantFlySong on 2018/6/13.
 */

public class Music implements Serializable{

    private static final long serialVersionUID = -7620435178023928252L;

    private int id;
    private String name;
    private String path;
    private int length;
    private String artist;

    public Music() {
    }

    public Music(int id, String name, String path, int length, String artist) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.length = length;
        this.artist = artist;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
