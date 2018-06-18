package com.example.elephantflysong.musicplayer.DataBase;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


/**
 * Created by ElephantFlySong on 2018/6/12.
 */

public class DBProveder extends ContentProvider {

    private DBHelper dbOpenHelper;
    public static final String AUTHORITY = "MUSIC";
    public static final Uri CONTENT_URI = Uri.parse("content://" +
        AUTHORITY + "/" + FileColumn.TABLE);

    @Override
    public boolean onCreate() {
        dbOpenHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = db.query(FileColumn.TABLE, projection, selection, selectionArgs,
                null, null, sortOrder);
        return cursor;
    }

    /*
    * 待实现
    * */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        long count = 0;
        try{
            count = db.insert(FileColumn.TABLE, null, values);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("error", "insert");
        }
        if (count > 0){
            return uri;
        }

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        try{
            db.delete(FileColumn.TABLE, selection, selectionArgs);
            Log.i("info", "delete");
        }catch (Exception e){
            e.printStackTrace();
            Log.e("error", "delete");
        }
        return 1;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int i = 0;
        try{
            i = db.update(FileColumn.TABLE, values, selection, null);
            return i;
        }catch (Exception e){
            e.printStackTrace();
            Log.e("error", "update");
        }
        return 0;
    }
}
