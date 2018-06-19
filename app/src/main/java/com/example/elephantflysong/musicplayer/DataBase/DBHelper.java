package com.example.elephantflysong.musicplayer.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.elephantflysong.musicplayer.Music.Music;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by ElephantFlySong on 2018/6/12.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "db_musics";
    public static final String TABLES_TABLE_NAME = "File_Table";
    public static final String DATABASE_CREATE = "CREATE TABLE " + FileColumn.TABLE + "(" +
            FileColumn.ID + " integer primary key autoincrement," +
            FileColumn.NAME + " text," +
            FileColumn.PATH + " text," +
            FileColumn.LENGTH + " integer," +
            FileColumn.ARTIST + " text)";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLES_TABLE_NAME);
        onCreate(db);
    }

    public void addAllToDataBase(SQLiteDatabase db, ArrayList<Music> data){
        for (Music music : data){
            addDataToDataBase(db, music);
        }
    }

    public void addDataToDataBase(SQLiteDatabase db, Music music){
        Cursor cursor = db.rawQuery("select * from "+ FileColumn.TABLE +
                " where "+ FileColumn.NAME +" = " + "'" + music.getName() + "'", null/*new String[]{FileColumn.TABLE,
            FileColumn.PATH, music.getPath()} */);
        if (cursor.getCount() == 0){
            ContentValues values = new ContentValues();
            values.put(FileColumn.NAME, music.getName());
            values.put(FileColumn.PATH, music.getPath());
            values.put(FileColumn.LENGTH, music.getLength());
            values.put(FileColumn.ARTIST, music.getArtist());
            db.insert(FileColumn.TABLE, null, values);
        }
    }

    public ArrayList<Music> getMusics(SQLiteDatabase db){
        ArrayList<Music> result = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + FileColumn.TABLE, null /*new String[]{FileColumn.TABLE}*/);
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex(FileColumn.ID));
            String name = cursor.getString(cursor.getColumnIndex(FileColumn.NAME));
            String path = cursor.getString(cursor.getColumnIndex(FileColumn.PATH));
            int length = cursor.getInt(cursor.getColumnIndex(FileColumn.LENGTH));
            String artist = cursor.getString(cursor.getColumnIndex(FileColumn.ARTIST));
            result.add(new Music(id, name, path, length, artist));
        }
        return result;
    }
}
