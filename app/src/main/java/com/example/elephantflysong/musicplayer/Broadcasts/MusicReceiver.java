package com.example.elephantflysong.musicplayer.Broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.elephantflysong.musicplayer.Interfaces.MusicListener;
import com.example.elephantflysong.musicplayer.MainPlayActivity;
import com.example.elephantflysong.musicplayer.Music.Music;

public class MusicReceiver extends BroadcastReceiver {

    private MusicListener musicListener;

    public MusicReceiver(MusicListener listener){
        super();
        this.musicListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
        Music music = (Music)intent.getSerializableExtra(MainPlayActivity.SERIALIZABLE_KEY);
        switch (intent.getIntExtra("code", 0)){
            case MainPlayActivity.BC_STOP:
                //Toast.makeText(context, "停止", Toast.LENGTH_SHORT).show();
                Log.i("info", "停止");
                try {
                    musicListener.onStop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case MainPlayActivity.BC_PREVIOUS:
                //Toast.makeText(context, "上一曲", Toast.LENGTH_SHORT).show();
                Log.i("info", "上一曲");
                musicListener.onPrevious(music);
                break;
            case MainPlayActivity.BC_START:
                //Toast.makeText(context, "播放或暂停", Toast.LENGTH_SHORT).show();
                Log.i("info", "播放");
                try{
                    musicListener.onStart(music);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                break;
            case MainPlayActivity.BC_PAUSE:
                Log.i("info", "暂停");
                try {
                    musicListener.onPause();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case MainPlayActivity.BC_NEXT:
                //Toast.makeText(context, "下一曲", Toast.LENGTH_SHORT).show();
                Log.i("info", "下一曲");
                musicListener.onNext(music);
                break;
            default:
                break;
        }

    }
}
