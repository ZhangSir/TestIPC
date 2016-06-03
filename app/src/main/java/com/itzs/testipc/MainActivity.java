package com.itzs.testipc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * <b>使用多进程会造成如下几个方面的问题：</b><br/>
 * 1、静态成员和单利模式完全失效<br/>
 * 2、线程同步机制完全失效<br/>
 * 3、SharedPreferences的可靠性下降<br/>
 * 4、Application会多次创建<br/>
 *
 * <b>android中的IPC方式</b>
 * 1、使用Bundle
 * 2、使用文件共享，适合在对数据同步要求不高额进程之间进行通信，SharePreferences作为文件共享的一个特例，由于系统在实现时使用了缓存的机制，所以在多进程模式下变得更不可靠；
 * 3、使用Messenger，是一种轻量级的IPC方案，底层实现也是AIDL；缺点一是采用串行运行方式，所有消息需要一个一个处理，不适合大量并发请求；二是无法实现跨进程的方法调用；
 * 4、使用AIDL，更好的支持大量并发请求，并支持夸进程的方法调用；
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnMessenger, btnAIDL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnMessenger = (Button) findViewById(R.id.btn_messenger);
        btnAIDL = (Button) findViewById(R.id.btn_aidl);

        btnMessenger.setOnClickListener(this);
        btnAIDL.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_messenger:
                startActivity(new Intent(this, MessengerActivity.class));
                break;
            case R.id.btn_aidl:
                startActivity(new Intent(this, BookManagerActivity.class));
                break;
        }
    }
}
