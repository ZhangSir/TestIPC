package com.itzs.testipc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Socket TCP 通信的实现
 * Created by zhangshuo on 2016/7/19.
 */
public class TCPServerService extends Service {

    private static final String TAG = TCPServerService.class.getSimpleName();

    private boolean mIsServiceDestroyed = false;

    private String[] mDefinedMessage = new String[]{"哈哈哈", "嘎嘎嘎", "呵呵呵", "嘿嘿嘿", "啦啦啦"};

    @Override
    public void onCreate() {
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mIsServiceDestroyed = true;
        super.onDestroy();
    }

    private class TcpServer implements Runnable{

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                //监听本地8688端口
                serverSocket = new ServerSocket(8688);
            } catch (IOException e) {
                System.out.println("establish tcp server failed, port: 8688");
                e.printStackTrace();
                return;
            }

            while (!mIsServiceDestroyed){
                try {
                    //接受客户端请求
                    final Socket client = serverSocket.accept();
                    System.out.println("accept");
                    new Thread(){
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void responseClient(Socket client) throws IOException{
        //用于接收客户端消息
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        //用于向客户端发送消息
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
        out.println("欢迎来到聊天室！");
        while (!mIsServiceDestroyed){
            String str = in.readLine();
            System.out.println("msg from client:" + str);
            if(null == str){
                //客户端断开连接
                break;
            }
            int i = new Random().nextInt(mDefinedMessage.length);
            String msg = mDefinedMessage[i];
            out.println(msg);
            System.out.println("send to client:" + msg);
        }
        System.out.println("client quit.");
        //关闭流
        out.close();
        in.close();
        client.close();
    }
}
