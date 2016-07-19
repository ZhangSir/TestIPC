package com.itzs.testipc;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

public class TCPClientActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int MESSAGE_RECEIVE_NEW_MSG = 1;
    private static final int MESSAGE_SOCKET_CONNECTED = 2;

    private Button btnSend;
    private TextView tvMsg;
    private EditText etInput;

    private PrintWriter mPrintWriter;
    private Socket mClient;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_RECEIVE_NEW_MSG:
                    tvMsg.append((String)msg.obj + "\n");
                    break;
                case MESSAGE_SOCKET_CONNECTED:
                    btnSend.setEnabled(true);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpclient);
        tvMsg = (TextView) findViewById(R.id.tv_tcpclient);
        etInput = (EditText) findViewById(R.id.et_tcpclient);
        btnSend = (Button) findViewById(R.id.btn_tcpclient);
        btnSend.setOnClickListener(this);

        Intent intent = new Intent(this, TCPServerService.class);
        startService(intent);

        new Thread(){
            @Override
            public void run() {
                connectTCPServer();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        if(null != mClient){
            try {
                mClient.shutdownInput();
                mClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v == btnSend){
            final String msg = etInput.getText().toString();
            if(!TextUtils.isEmpty(msg) && null != mPrintWriter){
                mPrintWriter.println(msg);
                etInput.setText("");
                String time = formatDateTime(System.currentTimeMillis());
                final String showMsg = "self " + time + ":" + msg + "\n";
                tvMsg.append(showMsg);
            }
        }
    }

    private String formatDateTime(long time){
        return new SimpleDateFormat("(HH:mm:ss)").format(time);
    }

    private void connectTCPServer(){
        Socket socket = null;
        while (socket == null){
            try{
                socket = new Socket("localhost", 8688);
                mClient = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
                System.out.println("connect tcp server success");
            } catch (UnknownHostException e) {
                e.printStackTrace();
                SystemClock.sleep(1000);
                System.out.println("connect tcp server failed, retry...");
            } catch (IOException e) {
                e.printStackTrace();
                SystemClock.sleep(1000);
                System.out.println("connect tcp server failed, retry...");
            }
        }

        try {
            //接收服务器端的消息
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!TCPClientActivity.this.isFinishing()){
                String msg = br.readLine();
                System.out.println("receive from Server:" + msg);
                if(null != msg){
                    String time = formatDateTime(System.currentTimeMillis());
                    final String showMsg = "server " + time + ":" + msg + "\n";
                    mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG, showMsg).sendToTarget();
                }
            }
            System.out.println("quit...");
            mPrintWriter.close();
            br.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
