package com.itzs.testipc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class BookManagerActivity extends AppCompatActivity {

    public static final String TAG = BookManagerActivity.class.getSimpleName();

    private IBookManager mRemoteBookManager;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constants.MSG_NEW_BOOK_ARRIVED:
                    Log.d(TAG, "receive new book notice:" + msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            if(null == service){
//                Log.d(TAG, "onServiceConnected:服务绑定失败");
//                return;
//            }
            mRemoteBookManager = IBookManager.Stub.asInterface(service);
            try {
                List<Book> list = mRemoteBookManager.getBookList();
                Log.d(TAG, "query book list, list type:" + list.getClass().getCanonicalName());
                Log.d(TAG, "query book list: " + list.toString());

                Book newBook = new Book(3, "Android开发讲义");
                mRemoteBookManager.addBook(newBook);
                Log.d(TAG, "add new book:" + newBook.toString());

                List<Book> newList = mRemoteBookManager.getBookList();
                Log.d(TAG, "query book list:" + newList.toString());

                //注册新课本回调监听器
                mRemoteBookManager.registerListener(mOnNewBookArrivedListener);

                //Service绑定成功后，设置死亡代理
                service.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.d(TAG, "onServiceDisconnected-name:" + name);
            //这里重新绑定远程Service
//            Intent intent = new Intent(BookManagerActivity.this, BookManagerService.class);
//            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        }
    };

    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub(){

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(Constants.MSG_NEW_BOOK_ARRIVED, newBook).sendToTarget();
        }
    };

    /**
     * Binder的死亡代理，绑定后，binder死亡时，会收到通知
     */
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if(mRemoteBookManager == null) return;
            Log.d(TAG, "binderDied-name-" + mRemoteBookManager.asBinder());
            mRemoteBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mRemoteBookManager = null;

            //这里重新绑定远程Service

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_manager);

        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        if(mRemoteBookManager != null && mRemoteBookManager.asBinder().isBinderAlive()){
            try {
                Log.d(TAG, "unregister new book arrive listener: " + mOnNewBookArrivedListener);
                mRemoteBookManager.unRegisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        unbindService(mConnection);
        super.onDestroy();
    }
}
