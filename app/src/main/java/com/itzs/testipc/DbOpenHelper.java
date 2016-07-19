package com.itzs.testipc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhangshuo on 2016/7/18.
 */
public class DbOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "book_provider.db";
    public static final String TABLE_NAME_BOOK = "t_book";
    public static final String TABLE_NAME_USER = "t_user";

    private static final int DB_VERSION = 1;

    //图书和用户信息表
    private String CREATE_BOOK_TABLE = "create table if not exists " + TABLE_NAME_BOOK + "(_id INTEGER PRIMARY KEY, name TEXT)";
    private String CREATE_USER_TABLE = "create table if not exists " + TABLE_NAME_USER + "(_id INTEGER PRIMARY KEY, name TEXT, sex INT)";

    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK_TABLE);
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
