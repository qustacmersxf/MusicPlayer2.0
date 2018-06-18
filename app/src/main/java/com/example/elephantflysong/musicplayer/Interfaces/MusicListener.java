package com.example.elephantflysong.musicplayer.Interfaces;

/**
 * Created by ElephantFlySong on 2018/6/18.
 */

public interface MusicListener {

    void onStart(int position);

    void onPause();

    void onStop();

    void onNext(int postion);

    void onPrevious(int postion);
}
