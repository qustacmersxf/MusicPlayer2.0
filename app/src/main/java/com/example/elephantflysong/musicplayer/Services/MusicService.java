package com.example.elephantflysong.musicplayer.Services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.elephantflysong.musicplayer.Interfaces.MusicListener;
import com.example.elephantflysong.musicplayer.MainPlayActivity;
import com.example.elephantflysong.musicplayer.Music.Music;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service {
    private MediaPlayer player;

    private MusicListener listener;

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                player.start();
            }
        });
        return new MusicBinder();
    }

    public class MusicBinder extends Binder{

        public void startMusic(Music music){
            try{
                player.reset();
                player.setDataSource(music.getPath());
                player.prepare();
                player.start();
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp){
                        listener.onEndMusic();
                    }
                });
                try {
                    listener.onStart(music);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void continueMusic(){
            player.start();
            listener.onContinue();
        }

        public void pauseMusic(){
            player.pause();
            try {
                listener.onPause();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void stopMusic(){
            player.stop();
            try {
                listener.onStop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void seekTo(int progress){
            player.seekTo(progress);
        }

        public void setMusicListener(MusicListener listener_){
            listener = listener_;
        }

        public int getCurrentProgress(){
            return player.getCurrentPosition();
        }
    }
}
