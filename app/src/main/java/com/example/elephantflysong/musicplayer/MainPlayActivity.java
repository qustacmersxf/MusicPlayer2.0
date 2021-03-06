package com.example.elephantflysong.musicplayer;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SyncAdapterType;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
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
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.elephantflysong.musicplayer.Adapters.MusicAdapter;
import com.example.elephantflysong.musicplayer.Broadcasts.MusicReceiver;
import com.example.elephantflysong.musicplayer.DataBase.DBHelper;
import com.example.elephantflysong.musicplayer.DataBase.FileColumn;
import com.example.elephantflysong.musicplayer.Interfaces.MusicControler;
import com.example.elephantflysong.musicplayer.Interfaces.MusicListener;
import com.example.elephantflysong.musicplayer.Music.Music;
import com.example.elephantflysong.musicplayer.Services.MusicService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainPlayActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUESTCODE_ADDMUSICLIST = 1;

    private static final String TAG = "MUSICLIST";

    private static final String BROADCAST_ACTION = "com.example.elephant.music";
    public static final int BC_STOP = 0;
    public static final int BC_PREVIOUS = 1;
    public static final int BC_START = 2;
    public static final int BC_PAUSE = 3;
    public static final int BC_NEXT = 4;

    public static final int MUSIC_STOP = 0;
    public static final int MUSIC_START = 1;
    public static final int MUSIC_PAUSE = 2;

    public static final String SERIALIZABLE_KEY = "music";

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
                    adapter.setOnItemLongClickListern(onItemLongClickListener);
                    rv_musics.setAdapter(adapter);
                    position = 0;
                    return true;
                case 2:
                    int currentTime = binder.getCurrentProgress();
                    seekBar.setProgress(currentTime);
                    text_currentTime.setText(toTime(currentTime));
                    break;
                case 3:
                    text_currentTime.setText(toTime(files.get(position).getLength()));
                    seekBar.setProgress(files.get(position).getLength());
                    break;
                case 4:
                    String table = dbHelper.getTableName(db, (String)msg.obj);
                    files = dbHelper.getMusics(db, table);
                    adapter.resetData(files);
                    position = 0;
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
                    Thread.sleep(500);
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
            sendNotification(files.get(position), status);
        }
    };

    private MusicAdapter.OnItemLongClickListener onItemLongClickListener =
            new MusicAdapter.OnItemLongClickListener() {
        @Override
        public void onItemLongClickListener(final int position) {
            final Cursor cursor = db.rawQuery("select * from Table_musicList", null);
            ArrayList<String> list = new ArrayList<>();
            while (cursor.moveToNext()){
                list.add(cursor.getString(cursor.getColumnIndex("listName")));
            }
            cursor.close();
            final String[] items = list.toArray(new String[list.size()]);
            final boolean[] select = new boolean[items.length];
            for (int i=0; i<select.length; i++){
                select[i] = false;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainPlayActivity.this);
            builder.setTitle("添加到歌单");
            builder.setMultiChoiceItems(items, select,
                    new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    select[which] = isChecked;
                }
            });
            builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0){
                        files = dbHelper.getMusics(db);
                        Message msg = new Message();
                        msg.what = 1;
                        handler.sendMessage(msg);
                        return;
                    }

                    for (int i=1; i<select.length; i++){
                        if (select[i]){
                            String table = dbHelper.getTableName(db, items[i]);
                            Cursor cursor1 = db.rawQuery("select id from " + FileColumn.TABLE +
                                " where " + FileColumn.PATH + "='" + files.get(position).getPath() +
                                "'", null);
                            if (cursor1.moveToNext()){
                                int id = cursor1.getInt(cursor1.getColumnIndex("id"));
                                db.execSQL("insert into " + table + " values(" + id + ")");
                            }
                            cursor1.close();
                        }
                    }
                }
            });
            builder.setNegativeButton("取消", null);
            builder.show();
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

    private MusicControler musicControler = new MusicControler() {
        @Override
        public void pauseMusic_() {
            startMusic();
        }

        @Override
        public void continueMusic_() {
            startMusic();
        }

        @Override
        public void stopMusic_() {
            stopMusic();
        }

        @Override
        public void nextMusic_() {
            nextMusic();
        }

        @Override
        public void previousMusic_() {
            previousMusic();
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

    private MusicReceiver receiver;

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
        if (ContextCompat.checkSelfPermission(MainPlayActivity.this,
                Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.VIBRATE);
        }
        if (!permissions.isEmpty()){
            String[] _perssions = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(MainPlayActivity.this, _perssions, 1);
        }
        Intent intent = new Intent(MainPlayActivity.this, MusicService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        receiver = new MusicReceiver(musicListener, musicControler);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_ACTION);
        registerReceiver(receiver, filter);

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
            case R.id.item_menu_mainactivity_addMusicList:
                Intent intent = new Intent(MainPlayActivity.this, AddMusicListActivity.class);
                startActivityForResult(intent,REQUESTCODE_ADDMUSICLIST);
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
                case REQUESTCODE_ADDMUSICLIST:
                    String newlist = data.getStringExtra("new list");
                    dbHelper.createNewList(db, newlist);
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
        cursor.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ibtn_listplay:
                showMusicList();
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

    private void showMusicList(){
        Cursor cursor = db.rawQuery("select * from Table_musicList", null);
        ArrayList<String> list = new ArrayList<>();
        while (cursor.moveToNext()){
            list.add(cursor.getString(cursor.getColumnIndex("listName")));
        }
        cursor.close();
        final String[] items = list.toArray(new String[list.size()]);
        final int[] select = new int[1];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("我的歌单");
        builder.setCancelable(true);

        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                select[0] = which;
            }
        });
        builder.setPositiveButton("显示歌单", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Message msg = new Message();
                msg.what = 4;
                msg.obj = items[select[0]];
                handler.sendMessage(msg);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void startMusic(){
        if (binder != null){
            binder.setMusicListener(musicListener);//冗余操作
            if (status != MUSIC_START){
                if (status == MUSIC_STOP){
                    if (position == -1){
                        position = 0;
                    }
                    binder.startMusic(files.get(position));
                }else if (status == MUSIC_PAUSE){
                    binder.continueMusic();
                }
                status = MUSIC_START;
                sendNotification(files.get(position), MUSIC_START);
            }else{
                binder.pauseMusic();
                bt_play.setImageDrawable(getResources().getDrawable(R.drawable.start));
                status = MUSIC_PAUSE;
                sendNotification(files.get(position), MUSIC_PAUSE);
            }
        }
    }

    private void stopMusic(){
        if (binder != null){
            binder.stopMusic();
            status = MUSIC_STOP;
            sendNotification(files.get(position), MUSIC_STOP);
        }
    }

    private void previousMusic(){
        if (binder != null){
            position = (position + files.size() - 1) % files.size();
            binder.stopMusic();
            binder.startMusic(files.get(position));
            musicListener.onPrevious(files.get(position));
            status = MUSIC_START;
            sendNotification(files.get(position), MUSIC_START);
        }
    }

    private void nextMusic(){
        if (binder != null){
            position = (position + 1) % files.size();
            binder.stopMusic();
            binder.startMusic(files.get(position));
            musicListener.onNext(files.get(position));
            status = MUSIC_START;
            sendNotification(files.get(position), MUSIC_START);
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
                if (files.size() > 0){
                    dbHelper.resetData(db, files);
                    Cursor cursor = db.rawQuery("select id from " + FileColumn.TABLE, null);
                    while (cursor.moveToNext()){
                        int id = cursor.getInt(0);
                        String table = dbHelper.getTableName(db, "全部");
                        db.execSQL("insert into " + table + " values(" + id + ")");
                    }
                    cursor.close();
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
                            /*byte[] bytes = retriever.getEmbeddedPicture();
                            if (bytes == null){
                                Log.e("error", "bytes is null");
                            }
                            music.setArtwork(bytes);*/
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

    private void sendNotification(Music music, int status){

        Bundle bundle = new Bundle();
        bundle.putSerializable(SERIALIZABLE_KEY, files.get(position));

        NotificationManager notificationManager =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel =
                new NotificationChannel("1", "Channel1",  NotificationManager.IMPORTANCE_MIN
                );
        channel.enableVibration(false);
        channel.setVibrationPattern(new long[]{0});
        notificationManager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(this, "1");
        builder.setSmallIcon(R.drawable.music);
        builder.setAutoCancel(false);
        Notification notification = builder.build();

        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.notification);
        if (music.getArtwork() != null){
            remoteViews.setImageViewBitmap(R.id.notification_imgv, music.getArtworkBitmap());
        }else {
            remoteViews.setImageViewResource(R.id.notification_imgv, R.drawable.music);
        }
        remoteViews.setTextViewText(R.id.notification_text_name,music.getName());
        remoteViews.setTextViewText(R.id.notification_text_artist,music.getArtist());

        int requestCode = (int) SystemClock.uptimeMillis();
        Intent intent_stop = new Intent(BROADCAST_ACTION);
        intent_stop.putExtra("code", BC_STOP);
        intent_stop.putExtras(bundle);
        PendingIntent pendingIntent_stop = PendingIntent.getBroadcast(this, requestCode,
                intent_stop, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_stop, pendingIntent_stop);

        requestCode = (int) SystemClock.uptimeMillis();
        Intent intent_previous = new Intent(BROADCAST_ACTION);
        intent_previous.putExtra("code", BC_PREVIOUS);
        intent_previous.putExtras(bundle);
        PendingIntent pendingIntent_previous = PendingIntent.getBroadcast(this, requestCode,
                intent_previous, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_previous, pendingIntent_previous);

        requestCode = (int) SystemClock.uptimeMillis();
        Intent intent_start = new Intent(BROADCAST_ACTION);
        if (status == MUSIC_PAUSE || status == MUSIC_STOP){
            intent_start.putExtra("code", BC_START);
            remoteViews.setImageViewResource(R.id.notification_start, R.drawable.start);
        }else{
            intent_start.putExtra("code", BC_PAUSE);
            remoteViews.setImageViewResource(R.id.notification_start, R.drawable.pause);
        }
        intent_start.putExtras(bundle);
        PendingIntent pendingIntent_start = PendingIntent.getBroadcast(this, requestCode, intent_start,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_start, pendingIntent_start);

        requestCode = (int) SystemClock.uptimeMillis();
        Intent intent_next = new Intent(BROADCAST_ACTION);
        intent_next.putExtra("code", BC_NEXT);
        intent_next.putExtras(bundle);
        PendingIntent pendingIntent_next = PendingIntent.getBroadcast(this, requestCode,
                intent_next, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_next, pendingIntent_next);

        notification.bigContentView = remoteViews;
        notification.flags |= NotificationCompat.FLAG_NO_CLEAR;
        notificationManager.notify(1, notification);
    }
}
