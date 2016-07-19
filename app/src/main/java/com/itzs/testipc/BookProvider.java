package com.itzs.testipc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * 自定义ContentProvider;<br/>
 * 主要以表格的形式来组织数据，也支持文件数据，如图片、视频等，参考MediaStore的实现；<br/>
 * 对底层的数据存储方式没有任何要求，即可以使用SQLite数据库，也也可以使用普通文件，甚至采用内存中的一个对象来进行数据的存储；<br/>
 * android:antuorities是ContentProvider的唯一标示，通过这个属性外部应用可以访问我们的BookProvider；<br/>
 * 可以为ContentProvider添加权限，其权限还可以细分为读权限和写权限，即android:readPermission和android:writePermission;<br/>
 * 根据Binder的工作原理，这六个方法允许在ContentProvider进程中，除onCreate由系统回调并运行在主线程中，其他五个方法由外界回调并运行在Binder线程池中；<br/>
 * query、update、insert、delete四大方法是存在多线程并发访问的，因此方法内部要做好线程同步；
 * （本例中采用的是SQlite并且只有一个SQLiteDatabase的连接，所以可以正确应对多线程的情况；
 * 具体原因是SqliteDatabase内部对数据库的操作时有同步处理的，但是如果通过多个SQLiteDatabase对象来操作数据库就无法保证线程同步，因为SQLiteDatabase对象之间无法进行线程同步）<br/>
 * ContentProvider还支持自定义调用，这个过程通过ContentProvider的Call方法和ContentResolver的Call方法来完成的；<br/>
 * Created by zhangshuo on 2016/7/18.
 */
public class BookProvider extends ContentProvider {

    private static final String TAG = BookProvider.class.getSimpleName();

    public static final String AUTHORITY = "com.itzs.testipc.provider";
    public static final Uri BOOK_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/book");
    public static final Uri USER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/user");

    public static final int BOOK_URI_CODE = 0;
    public static final int USER_URI_CODE = 1;

    public static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "book", BOOK_URI_CODE);
        uriMatcher.addURI(AUTHORITY, "user", USER_URI_CODE);
    }

    private Context mContext;
    private SQLiteDatabase mDb;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate, currentThread: " + Thread.currentThread().getName());
        mContext = getContext();
        //ContentProvider创建时，初始化数据库，注意：这里仅仅是为了演示，实际使用中不推荐在主线程中进行耗时的数据库操作；
        initProviderData();
        return true;
    }

    private void initProviderData(){
        mDb = new DbOpenHelper(mContext).getWritableDatabase();
        mDb.execSQL("delete from " + DbOpenHelper.TABLE_NAME_BOOK);
        mDb.execSQL("delete from " + DbOpenHelper.TABLE_NAME_USER);
        mDb.execSQL("insert into " + DbOpenHelper.TABLE_NAME_BOOK + " values(3, 'Android');");
        mDb.execSQL("insert into " + DbOpenHelper.TABLE_NAME_BOOK + " values(4, 'IOS');");
        mDb.execSQL("insert into " + DbOpenHelper.TABLE_NAME_BOOK + " values(5, 'HTML5');");
        mDb.execSQL("insert into " + DbOpenHelper.TABLE_NAME_USER + " values(1, 'Tom', 1);");
        mDb.execSQL("insert into " + DbOpenHelper.TABLE_NAME_USER + " values(2, 'lucy', 0);");
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query, currentThread: " + Thread.currentThread().getName());
        String tableName = getTableName(uri);
        if(null == tableName){
            throw new IllegalArgumentException("Unsupport URI:" + uri);
        }
        return mDb.query(tableName, projection, selection, selectionArgs, null, null,sortOrder, null);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType");
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert");
        String tableName = getTableName(uri);
        if(null == tableName){
            throw new IllegalArgumentException("Unsupport URI:" + uri);
        }
        mDb.insert(tableName,null, values);
        mContext.getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete");
        String tableName = getTableName(uri);
        if(null == tableName){
            throw new IllegalArgumentException("Unsupport URI:" + uri);
        }
        int count = mDb.delete(tableName, selection, selectionArgs);
        if(count > 0){
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "update");
        String tableName = getTableName(uri);
        if(null == tableName){
            throw new IllegalArgumentException("Unsupport URI:" + uri);
        }
        int row = mDb.update(tableName, values, selection, selectionArgs);
        if(row > 0){
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return row;
    }

    private String getTableName(Uri uri){
        String tableName = null;
        switch (uriMatcher.match(uri)){
            case BOOK_URI_CODE:
                tableName = DbOpenHelper.TABLE_NAME_BOOK;
                break;
            case USER_URI_CODE:
                tableName = DbOpenHelper.TABLE_NAME_USER;
                break;
        }
        return tableName;
    }
}
