package com.itzs.testipc;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * 单独运行在:remote进程，配置见Manifest文件
 * Created by zhangshuo on 2016/5/24.
 */
public class MessengerService extends Service {

    public static final String TAG = MessengerService.class.getSimpleName();

    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constants.MSG_FROM_CLIENT:
                    //收到客户端消息
                    Log.d(TAG, "receive msg from client:" + msg.getData().getString("msg"));
                    //回复客户端
                    Messenger messengerClient = msg.replyTo;
                    Message replyMsg = Message.obtain(null, Constants.MSG_FROM_SEVICE);
                    Bundle bundle = new Bundle();
                    bundle.putString("reply", "你的消息已收到！");
                    replyMsg.setData(bundle);
                    try {
                        messengerClient.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new MessengerHandler());

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
