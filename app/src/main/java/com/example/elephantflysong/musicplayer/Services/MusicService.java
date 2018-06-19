package com.example.elephantflysong.musicplayer.Services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.elephantflysong.musicplayer.Interfaces.MusicListener;
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
        return new MusicBinder();
    }

    public class MusicBinder extends Binder{

        public void startMusic(Music music){
            if (player == null){
                player = new MediaPlayer();
                player.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        player.start();
                    }
                });
            }
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
                listener.onStart(music);
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
            listener.onPause();
        }

        public void stopMusic(){
            player.stop();
            listener.onStop();
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
