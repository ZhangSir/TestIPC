package com.itzs.testipc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MessengerActivity extends AppCompatActivity {

    public static final String TAG = MessengerActivity.class.getSimpleName();

    /**
     * 向服务端发送消息的Messenger
     */
    private Messenger messenger;

    /**
     * 接受服务器回复信息的Messenger
     */
    private Messenger receiveReplyMessenger;

    /**
     * 处理服务端回复的消息
     */
    private Handler receiveReplyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Constants.MSG_FROM_SEVICE:
                    Log.d(TAG, "receive msg from service:" + msg.getData().getString("reply"));
                    break;
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //服务已连接，发送一条消息到服务器
            messenger = new Messenger(service);
            Message msg = Message.obtain(null, Constants.MSG_FROM_CLIENT);
            Bundle bundle = new Bundle();
            bundle.putString("msg", "Hello messenger, this is client!");
            msg.setData(bundle);
            //在发送消息给服务端时，将接受服务端回复消息的Messenger通过Message的replyTo参数传递给服务端
            msg.replyTo = receiveReplyMessenger;
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        receiveReplyMessenger = new Messenger(receiveReplyHandler);

        //绑定远程服务
        Intent intent = new Intent(this, MessengerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }
}
