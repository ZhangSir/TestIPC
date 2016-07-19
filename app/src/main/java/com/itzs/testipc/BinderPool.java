package com.itzs.testipc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

/**
 * Binder连接池的具体实现;
 * 通过Binder连接池，只需要创建一个Service即可完成多个AIDL接口的工作，避免对个AIDL接口重复创建Service的问题，减少资源消耗；
 * Created by zhangshuo on 2016/7/18.
 */
public class BinderPool {
    private static final String TAG = BinderPool.class.getSimpleName();

    /** 无效的binderCode*/
    public static final int BINDER_NONE = -1;
    /** ICompute的binderCode*/
    public static final int BINDER_COMPUTE = 0;
    /** ISecurityCenter的binderCode*/
    public static final int BINDER_SECURITY_CENTER = 1;

    private Context mContext;
    private IBinderPool mBinderPool;
    private static volatile BinderPool instance;
    private CountDownLatch mConnectBinderPoolCounDownLatch;

    private BinderPool(Context context){
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    public static BinderPool getInstance(Context context){
        if(null == instance){
            synchronized (BinderPool.class) {
                if(null == instance){
                    instance = new BinderPool(context);
                }
            }
        }
        return instance;
    }

    private synchronized void connectBinderPoolService(){
        mConnectBinderPoolCounDownLatch = new CountDownLatch(1);
        Intent intent = new Intent(mContext, BinderPoolService.class);
        mContext.bindService(intent, mBinderPoolConnection, Context.BIND_AUTO_CREATE);
        try {
            //通过CountDownLatch将bindService这一异步操作转换成了同步操作；
            // 此处发出bindService的请求后，进入等待，直到ServiceConnection中完成绑定操作后，才能释放；
            mConnectBinderPoolCounDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                mBinderPool.asBinder().linkToDeath(mBinderPoolDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            //servie绑定完成，释放connectBinderPoolService()方法
            mConnectBinderPoolCounDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.w(TAG, "binder died.");
            mBinderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipient, 0);
            mBinderPool = null;
            //断线重连
            connectBinderPoolService();
        }
    };

    public IBinder queryBinder(int binderCode){
        IBinder binder = null;
        try {
            if(null != mBinderPool){
                binder = mBinderPool.queryBinder(binderCode);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return binder;
    }

    /**
     * Binder连接池AIDL接口的实现
     */
    public static  class BinderPoolImpl extends IBinderPool.Stub{

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public IBinder queryBinder(int bindCode) throws RemoteException {
            IBinder binder = null;
            switch (bindCode){
                case BINDER_SECURITY_CENTER:
                    binder = new SecurityCenterImpl();
                    break;
                case BINDER_COMPUTE:
                    binder = new ComputeImpl();
            }
            return binder;
        }
    }

}
