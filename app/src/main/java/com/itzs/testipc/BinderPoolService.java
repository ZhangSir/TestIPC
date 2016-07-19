package com.itzs.testipc;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Binder连接池远程Service的实现
 * Created by zhangshuo on 2016/7/18.
 */
public class BinderPoolService extends Service {

    private static final String TAG = BinderPoolService.class.getSimpleName();

    private Binder mBinderPool = new BinderPool.BinderPoolImpl();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinderPool;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
