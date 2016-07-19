package com.itzs.testipc;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class ProviderActivity extends AppCompatActivity {

    private static final String TAG = ProviderActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Uri bookUri = BookProvider.BOOK_CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put("_id", 6);
        values.put("name", "程序设计的艺术");
        getContentResolver().insert(bookUri, values);

        Cursor bookCursor = getContentResolver().query(bookUri, new String[]{"_id", "name"}, null, null, null);
        while (bookCursor.moveToNext()){
            Book book = new Book(bookCursor.getInt(0), bookCursor.getString(1));
            Log.d(TAG, "query book:" + book.toString());
        }
        bookCursor.close();

        Uri userUri = BookProvider.USER_CONTENT_URI;
        Cursor userCursor = getContentResolver().query(userUri, new String[]{"_id", "name", "sex"}, null, null, null);
        while (userCursor.moveToNext()){
            Log.d(TAG, "User[ _id=" + userCursor.getInt(0) + " name=" + userCursor.getString(1) + " sex=" + userCursor.getInt(2) + " ]");
        }
        userCursor.close();;

    }

}
