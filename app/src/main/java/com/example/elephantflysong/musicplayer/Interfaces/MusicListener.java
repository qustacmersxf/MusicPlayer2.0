package com.example.elephantflysong.musicplayer.Interfaces;

import com.example.elephantflysong.musicplayer.Music.Music;

/**
 * Created by ElephantFlySong on 2018/6/18.
 */

public interface MusicListener {

    void onStart(Music music) throws InterruptedException;

    void onPause() throws InterruptedException;

    void onStop() throws InterruptedException;

    void onNext(Music music);

    void onPrevious(Music music);

    void onEndMusic();

    void onContinue();
}
