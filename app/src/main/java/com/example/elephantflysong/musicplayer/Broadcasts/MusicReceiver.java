package com.example.elephantflysong.musicplayer.Broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.elephantflysong.musicplayer.Interfaces.MusicControler;
import com.example.elephantflysong.musicplayer.Interfaces.MusicListener;
import com.example.elephantflysong.musicplayer.MainPlayActivity;
import com.example.elephantflysong.musicplayer.Music.Music;

public class MusicReceiver extends BroadcastReceiver {

    private MusicListener musicListener;
    private MusicControler musicControler;

    public MusicReceiver(MusicListener listener, MusicControler musicControler){
        super();
        this.musicListener = listener;
        this.musicControler = musicControler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving

        Music music = (Music)intent.getSerializableExtra(MainPlayActivity.SERIALIZABLE_KEY);
        switch (intent.getIntExtra("code", 0)){
            case MainPlayActivity.BC_STOP:
                Log.i("info", "stop");
                try {
                    musicListener.onStop();
                    musicControler.stopMusic_();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case MainPlayActivity.BC_PREVIOUS:
                Log.i("info", "previous");
                musicListener.onPrevious(music);
                musicControler.previousMusic_();
                break;
            case MainPlayActivity.BC_START:
                Log.i("info", "start");
                musicListener.onContinue();
                musicControler.continueMusic_();
                break;
            case MainPlayActivity.BC_PAUSE:
                Log.i("info", "pause");
                try {
                    musicListener.onPause();
                    musicControler.pauseMusic_();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case MainPlayActivity.BC_NEXT:
                Log.i("info", "next");
                musicListener.onNext(music);
                musicControler.nextMusic_();
                break;
            default:
                break;
        }

    }
}
