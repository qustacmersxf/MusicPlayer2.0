package com.example.elephantflysong.musicplayer.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.elephantflysong.musicplayer.Music.Music;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by ElephantFlySong on 2018/6/12.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 10;

    public static final String DATABASE_NAME = "db_musics";
    public static final String DATABASE_CREATE = "CREATE TABLE " + FileColumn.TABLE + "(" +
            FileColumn.ID + " integer primary key autoincrement," +
            FileColumn.NAME + " text," +
            FileColumn.PATH + " text," +
            FileColumn.LENGTH + " integer," +
            FileColumn.ARTIST + " text)";/* +
            FileColumn.ARTWORK + " blob)";*/
    public static final String DATABASE_CREATE_MUSICLISTS = "CREATE TABLE Table_musicList (" +
            "listName text," +
            "tableName text)";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(DATABASE_CREATE);
        db.execSQL(DATABASE_CREATE_MUSICLISTS);
        createNewList(db, "全部");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + FileColumn.TABLE);
        db.execSQL("drop table if exists Table_musicList");
        db.execSQL("drop table if exists Table_musicList_0");
        db.execSQL("drop table if exists Table_musicList_1");
        db.execSQL("drop table if exists Table_musicList_2");
        db.execSQL("drop table if exists Table_musicList_3");
        db.execSQL("drop table if exists Table_musicList_4");
        db.execSQL("drop table if exists Table_musicList_5");
        db.execSQL("drop table if exists Table_musicList_6");
        db.execSQL("drop table if exists Table_musicList_7");
        onCreate(db);
    }

    public void addAllToDataBase(SQLiteDatabase db, ArrayList<Music> data){
        for (Music music : data){
            addDataToDataBase(db, music);
        }
    }

    public void addDataToDataBase(SQLiteDatabase db, Music music){
        Cursor cursor = db.rawQuery("select * from "+ FileColumn.TABLE +
                " where "+ FileColumn.NAME +" = " + "'" + music.getName() + "'", null);
        if (cursor.getCount() == 0){
            ContentValues values = new ContentValues();
            values.put(FileColumn.NAME, music.getName());
            values.put(FileColumn.PATH, music.getPath());
            values.put(FileColumn.LENGTH, music.getLength());
            values.put(FileColumn.ARTIST, music.getArtist());
            //values.put(FileColumn.ARTWORK, music.getArtwork());
            db.insert(FileColumn.TABLE, null, values);
        }
        cursor.close();
    }

    public ArrayList<Music> getMusics(SQLiteDatabase db){
        return getMusics(db, FileColumn.TABLE);
    }

    public ArrayList<Music> getMusics(SQLiteDatabase db, String table){
        Log.i("info", "getMusics(,) " + table);
        ArrayList<Music> result = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + FileColumn.TABLE +
                " where id in (select id from " + table + ")", null);
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex(FileColumn.ID));
            String name = cursor.getString(cursor.getColumnIndex(FileColumn.NAME));
            String path = cursor.getString(cursor.getColumnIndex(FileColumn.PATH));
            int length = cursor.getInt(cursor.getColumnIndex(FileColumn.LENGTH));
            String artist = cursor.getString(cursor.getColumnIndex(FileColumn.ARTIST));
            //byte[] artwork = cursor.getBlob(cursor.getColumnIndex(FileColumn.ARTWORK));
            result.add(new Music(id, name, path, length, artist/*, artwork*/));
        }
        cursor.close();
        return result;
    }

    public void resetData(SQLiteDatabase db, ArrayList<Music> data){
        db.execSQL("DELETE FROM sqlite_sequence WHERE name = '" + FileColumn.TABLE + "'");
        addAllToDataBase(db, data);
    }

    public void createNewList(SQLiteDatabase db, String name){
        Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master where type = 'table'", null);
        int count = cursor.getCount() - 2;
        cursor.close();
        count++;
        String name_ = "Table_musicList_" + count;
        String sql = "CREATE TABLE " + name_ + "(" +
                "id integer)";
        db.execSQL(sql);
        db.execSQL("insert into Table_musicList values('" + name + "','" + name_ + "')");
        Log.i("info", "createNewList:" + name + " " + name_);
    }

    public String getTableName(SQLiteDatabase db,String listName){
        String result = "";
        Cursor cursor = db.rawQuery("select * from Table_musicList where listName = '" +
                listName + "'", null);
        if (cursor.moveToNext()){
            result = cursor.getString(cursor.getColumnIndex("tableName"));
        }
        cursor.close();
        return result;
    }
}
