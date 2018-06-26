package com.example.elephantflysong.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.elephantflysong.musicplayer.Adapters.MusicAdapter;
import com.example.elephantflysong.musicplayer.DataBase.DBHelper;
import com.example.elephantflysong.musicplayer.DataBase.FileColumn;
import com.example.elephantflysong.musicplayer.Interfaces.MusicListener;
import com.example.elephantflysong.musicplayer.Music.Music;
import com.example.elephantflysong.musicplayer.Services.MusicService;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.jar.Manifest;

import static com.example.elephantflysong.musicplayer.R.drawable.music;

public class MainPlayActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUESTCODE_ACTION_GET_CONTENT = 1;

    public static final int MUSIC_STOP = 0;
    public static final int MUSIC_START = 1;
    public static final int MUSIC_PAUSE = 2;

    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    private ImageButton bt_list;
    private ImageButton bt_back;
    private ImageButton bt_stop;
    private ImageButton bt_play;
    private ImageButton bt_moveUp;
    private ImageButton bt_moveDown;
    private TextView text_endTime;
    private TextView text_currentTime;
    private TextView text_currentMusic;
    private SeekBar seekBar;

    private RecyclerView rv_musics;
    private MusicAdapter adapter;
    private ArrayList<Music> files;

    private MusicService.MusicBinder binder;

    private int status = MUSIC_STOP;
    public static int position = -1;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    rv_musics.setLayoutManager(new LinearLayoutManager(MainPlayActivity.this));
                    rv_musics.setItemAnimator(new DefaultItemAnimator());
                    adapter = new MusicAdapter(files);
                    adapter.setOnItemClickListener(onItemClickListener);
                    rv_musics.setAdapter(adapter);
                    position = 0;
                    return true;
                case 2:
                    Log.i("info","msg.what=2");
                    int currentTime = binder.getCurrentProgress();
                    seekBar.setProgress(currentTime);
                    text_currentTime.setText(toTime(currentTime));
                    break;
                case 3:
                    text_currentTime.setText(toTime(files.get(position).getLength()));
                    seekBar.setProgress(files.get(position).getLength());
                    break;
                default:
                    break;
            }
            return false;
        }
    });


    private MyThread seekBarThread;

    public class MyThread extends Thread{

        private volatile boolean exec = true;

        @Override
        public void run() {
            while (exec && !isInterrupted()){
                try{
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 2;
                    handler.sendMessage(msg);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        public void quit(){
            exec = false;
        }
    }

    private MusicAdapter.OnItemClickListener onItemClickListener = new MusicAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            MainPlayActivity.position = position;
            binder.setMusicListener(musicListener);//冗余操作
            binder.startMusic(files.get(position));
            bt_play.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            status = MUSIC_START;
        }
    };

    private MusicListener musicListener = new MusicListener() {
        @Override
        public void onStart(Music music) throws InterruptedException {
            bt_play.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            text_currentMusic.setText(music.getName());
            text_endTime.setText(toTime(music.getLength()));
            seekBar.setMax(music.getLength());
            if(seekBarThread!=null){
                seekBarThread.interrupt();
                seekBarThread.quit();
                seekBarThread.join();
                seekBarThread = null;
            }
            seekBarThread = new MyThread();
            seekBarThread.start();
        }

        @Override
        public void onPause() throws InterruptedException {
            bt_play.setImageDrawable(getResources().getDrawable(R.drawable.start));
            if (seekBarThread != null){
                seekBarThread.interrupt();
                seekBarThread.quit();
                seekBarThread.join();
                seekBarThread = null;
            }
        }

        @Override
        public void onStop() throws InterruptedException {
            bt_play.setImageDrawable(getResources().getDrawable(R.drawable.start));
            if (seekBarThread != null){
                seekBarThread.interrupt();
                seekBarThread.quit();
                seekBarThread.join();
                seekBarThread = null;
            }

            Message msg = new Message();
            msg.what = 3;
            handler.sendMessage(msg);
        }

        @Override
        public void onNext(Music music) {
            bt_play.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            text_currentMusic.setText(music.getName());
            text_endTime.setText(toTime(music.getLength()));
            seekBar.setMax(music.getLength());
        }

        @Override
        public void onPrevious(Music music) {
            bt_play.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            text_currentMusic.setText(music.getName());
            text_endTime.setText(toTime(music.getLength()));
            seekBar.setMax(music.getLength());
        }

        @Override
        public void onEndMusic(){
            nextMusic();
        }

        @Override
        public void onContinue(){
            seekBarThread = new MyThread();
            seekBarThread.start();
            bt_play.setImageDrawable(getResources().getDrawable(R.drawable.pause));
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MusicService.MusicBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainPlayActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainPlayActivity.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()){
            String[] _perssions = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(MainPlayActivity.this, _perssions, 1);
        }

        Intent intent = new Intent(MainPlayActivity.this, MusicService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        initMainView();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meun_mainactivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_menu_mainactivity_searchMP3:
                File file = Environment.getExternalStorageDirectory();
                browserFile(file);
                break;
            case R.id.item_menu_mainactivity_selectDir:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUESTCODE_ACTION_GET_CONTENT);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case REQUESTCODE_ACTION_GET_CONTENT:

                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return  false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    private void initMainView(){
        bt_stop = findViewById(R.id.ibtn_stop);
        bt_moveDown = findViewById(R.id.ibtn_next);
        bt_play = findViewById(R.id.ibtn_start);
        bt_moveUp = findViewById(R.id.ibtn_before);
        bt_list = findViewById(R.id.ibtn_listplay);
        text_currentMusic = findViewById(R.id.text_currentMusic);
        text_currentTime = findViewById(R.id.text_currentTime);
        text_endTime = findViewById(R.id.text_endTime);
        seekBar = findViewById(R.id.seekBar);
        rv_musics = findViewById(R.id.rv_musics);

        text_currentMusic.setText("无");

        bt_list.setOnClickListener(this);
        bt_play.setOnClickListener(this);
        bt_stop.setOnClickListener(this);
        bt_moveUp.setOnClickListener(this);
        bt_moveDown.setOnClickListener(this);

        bt_play.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    if (status == MUSIC_START) {
                        bt_play.setImageDrawable(getResources().getDrawable(R.drawable.pause_press));
                    }else{
                        bt_play.setImageDrawable(getResources().getDrawable(R.drawable.start_press));
                    }
                }
                return false;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binder.seekTo(seekBar.getProgress());
            }
        });

        dbHelper = new DBHelper(MainPlayActivity.this);
        db = dbHelper.getWritableDatabase();
        cursor = db.rawQuery("select * from " + FileColumn.TABLE, null);
        if (cursor.getCount() == 0){
            browserFile(Environment.getExternalStorageDirectory());
        }else {
            files = dbHelper.getMusics(db);
            Message msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ibtn_listplay:
                break;
            case R.id.ibtn_start:
                startMusic();
                break;
            case R.id.ibtn_stop:
                stopMusic();
                break;
            case R.id.ibtn_next:
                nextMusic();
                break;
            case R.id.ibtn_before:
                previousMusic();
                break;
            default:
                break;
        }
    }

    private void startMusic(){
        if (binder != null){
            binder.setMusicListener(musicListener);//冗余操作
            if (status != MUSIC_START){
                if (status == MUSIC_STOP){
                    binder.startMusic(files.get(position));
                }else if (status == MUSIC_PAUSE){
                    binder.continueMusic();
                }
                status = MUSIC_START;
            }else{
                binder.pauseMusic();
                bt_play.setImageDrawable(getResources().getDrawable(R.drawable.start));
                status = MUSIC_PAUSE;
            }
        }
    }

    private void stopMusic(){
        if (binder != null){
            binder.stopMusic();
            status = MUSIC_STOP;
        }
    }

    private void previousMusic(){
        if (binder != null){
            //position = binder.previousMusic();
            position = (position + files.size() - 1) % files.size();
            binder.stopMusic();
            binder.startMusic(files.get(position));
            musicListener.onPrevious(files.get(position));
            status = MUSIC_START;
        }
    }

    private void nextMusic(){
        if (binder != null){
            position = (position + 1) % files.size();
            binder.stopMusic();
            binder.startMusic(files.get(position));
            musicListener.onNext(files.get(position));
            status = MUSIC_START;
        }
    }

    private void browserFile(final File file){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("检索本地音频");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_search, null);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setPositiveButton("后台检索", null);
        final AlertDialog dialog = builder.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                files = new ArrayList<>();
                searchMP3(file);
                //dbHelper.addAllToDataBase(db, files);
                if (files.size() > 0){
                    adapter = new MusicAdapter(files);
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }else{
                    //有隐患
                    Toast.makeText(getApplicationContext(), "未找到MP3文件", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        }).start();
    }

    private void searchMP3(File file){
        File[] localFiles = file.listFiles();
        if (localFiles == null){
            return;
        }
        if (localFiles.length > 0){
            for (int i=0; i<localFiles.length; i++){
                if (!localFiles[i].isDirectory()){
                    if (localFiles[i].getPath().indexOf(".mp3") > -1){
                        try{
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            Music music = new Music();
                            music.setName(localFiles[i].getName());
                            music.setPath(localFiles[i].getPath());
                            retriever.setDataSource(localFiles[i].getPath());
                            music.setArtist(retriever
                                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                            music.setLength(Integer.valueOf(retriever
                                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
                            files.add(music);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }else{
                    searchMP3(localFiles[i]);
                }
            }
        }
        return;
    }

    private String toTime(int length){
        int seconds = length / 1000;
        int minutes = seconds / 60;
        seconds %= 60;
        if (seconds < 10){
            return "" + minutes + ":0" + seconds;
        }
        return "" + minutes + ":" + seconds;
    }

}
