package com.itzs.testipc;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 单独运行在:bookManager进程，配置见Manifest文件<br/>
 * 此处演示两种权限验证方式，一种是在binder中验证权限，一种是在binder的onTransaction方法中验证权限和包名信息<br/>
 * Created by zhangshuo on 2016/5/25.
 */
public class BookManagerService extends Service {

    public static final String TAG = BookManagerService.class.getSimpleName();

    /**
     * AIDL支持ArrayList类型，但AIDL中支持的是抽象的List，
     * 而List是一个接口，CopyOnWriteArrayList虽然不是继承ArrayList，但是是继承自List，
     * 所以传入CopyOnWriteArrayList类型是允许的，只是会按照List的规范去访问数据并最终形成一个新的ArrayList传递给客户端；<br/>
     *
     * 这里之所以使用CopyOnWriteArrayList还有一个重要原因，是CopyOnWriteArrayList支持并发读写；
     */
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<Book>();

    /**
     * RemoteCallbackList是系统专门提供的用于删除跨进程listener的接口，（因为客户端传来的同一listener，服务收到后都会转化成一个全新的对象，所以使用普通list无法实现都某个listener对象的追踪）<br/>
     * RemoteCallbackList支持在客户端进程终止后，自动移除客户端注册的listener；<br/>
     * RemoteCallbackList内部自动实现了线程同步的功能；
     */
    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<IOnNewBookArrivedListener>();

    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);

    private Binder mBinder = new IBookManager.Stub(){

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.register(listener);
        }

        @Override
        public void unRegisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.unregister(listener);
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            //检查有无权限
            int check = checkCallingOrSelfPermission("com.itzs.testipc.permission.ACCESS_BOOK_SERVICE");
            if(check == PackageManager.PERMISSION_DENIED){
                return false;
            }

            //检查包名是否匹配
            String packageName = null;
            String[] packages = getPackageManager().getPackagesForUid(getCallingUid());
            if(null != packages && packages.length > 0){
                packageName = packages[0];
            }
            if(!packageName.startsWith("com.itzs")){
                return false;
            }

            return super.onTransact(code, data, reply, flags);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1, "Android群英传"));
        mBookList.add(new Book(2, "Android开发艺术探索"));

        new Thread(new ServiceWorker()).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        int check = checkCallingOrSelfPermission("com.itzs.testipc.permission.ACCESS_BOOK_SERVICE");
        if(check == PackageManager.PERMISSION_DENIED){
            Log.d(TAG, "onBind:客户端没有服务绑定权限");
            return null;
        }
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mIsServiceDestroyed.set(true);
        super.onDestroy();
    }

    private void onNewBookArrived(Book book){
        mBookList.add(book);
        Log.d(TAG, "onNewBookArrived, notify listeners:" + mListenerList.getRegisteredCallbackCount());

        final int n = mListenerList.beginBroadcast();
        for (int i = 0; i < n; i++){
            IOnNewBookArrivedListener listener = mListenerList.getBroadcastItem(i);
            if(null != listener){
                try {
                    listener.onNewBookArrived(book);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mListenerList.finishBroadcast();
    }

    private class ServiceWorker implements Runnable{

        @Override
        public void run() {
            while (!mIsServiceDestroyed.get()){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int bookId = mBookList.size() + 1;
                Book newBook = new Book(bookId, "new book#" + bookId);
                onNewBookArrived(newBook);
            }
        }
    }

}
