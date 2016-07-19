package com.itzs.testipc;

import android.os.RemoteException;

/**
 * 计算AIDL接口的实现
 * Created by zhangshuo on 2016/7/18.
 */
public class ComputeImpl extends ICompute.Stub {
    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

    }

    @Override
    public int add(int a, int b) throws RemoteException {
        return a + b;
    }
}
