package com.example.elephantflysong.musicplayer.Services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.example.elephantflysong.musicplayer.Interfaces.MusicListener;
import com.example.elephantflysong.musicplayer.Music.Music;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service {

    private MusicBinder binder;
    private ArrayList<Music> files;
    private MediaPlayer player;

    private int position = -1;

    private MusicListener listener;

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        binder = new MusicBinder();
        return binder;
    }

    public class MusicBinder extends Binder{
        public void startMusic(int index){
            if (player == null){
                player = new MediaPlayer();
            }
            try{
                player.reset();
                player.setDataSource(files.get(index).getPath());
                player.prepare();
                player.start();
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp){
                        nextMusic();
                    }
                });
                position = index;
                listener.onStart(position);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void startMusic(){
            if (position == -1) {
                position = 0;
                this.startMusic(position);
            }else{
                player.start();
                listener.onStart(position);
            }
        }

        public void pauseMusic(){
            player.pause();
            listener.onPause();
        }

        public void stopMusic(){
            player.stop();
            listener.onStop();
        }

        public int nextMusic(){
            player.stop();
            try{
                position = (position + 1) % files.size();
                Music file = files.get(position);
                player.reset();
                player.setDataSource(file.getPath());
                player.prepare();
                player.start();
                listener.onNext(position);
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                return position;
            }
        }

        public int previousMusic(){
            player.stop();
            try{
                position = (position + files.size() - 1) % files.size();
                Music file = files.get(position);
                player.reset();
                player.setDataSource(file.getPath());
                player.prepare();
                player.start();
                listener.onPrevious(position);
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                return position;
            }
        }

        public void setFiles(ArrayList<Music> files_){
            files = files_;
        }

        public void setMusicListener(MusicListener listener_){
            listener = listener_;
        }
    }
}
