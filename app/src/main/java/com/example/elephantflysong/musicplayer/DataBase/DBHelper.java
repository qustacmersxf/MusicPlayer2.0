package com.example.elephantflysong.musicplayer.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;

/**
 * Created by ElephantFlySong on 2018/6/12.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "db_musics";
    public static final String TABLES_TABLE_NAME = "File_Table";
    public static final String DATABASE_CREATE = "CREATE TABLE" + FileColumn.TABLE + "(" +
            FileColumn.ID + " integer primary key autoincrement," +
            FileColumn.NAME + " text," +
            FileColumn.PATH + " test," +
            FileColumn.SORT + " integer," +
            FileColumn.TYPE + " text)";

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
}
