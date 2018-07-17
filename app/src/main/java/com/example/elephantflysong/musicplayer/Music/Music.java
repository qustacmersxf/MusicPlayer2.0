package com.example.elephantflysong.musicplayer.Music;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
    private byte[] artwork;

    public Music() {
    }

    public Music(int id, String name, String path, int length, String artist/*, byte[] artwork*/) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.length = length;
        this.artist = artist;
        this.artwork = null;
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
        if (artist.isEmpty()){
            this.artist = "未知";
        }
        this.artist = artist;
    }

    public Bitmap getArtworkBitmap(){
        Bitmap bitmap = BitmapFactory.decodeByteArray(artwork, 0, artwork.length);
        return bitmap;
    }

    public byte[] getArtwork() {
        return artwork;
    }

    public void setArtwork(byte[] artwork) {
        this.artwork = artwork;
    }
}
