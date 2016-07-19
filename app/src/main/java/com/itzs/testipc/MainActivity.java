package com.itzs.testipc;

import android.content.ContentProviderOperation;
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
 * 1、使用Bundle<br/>
 * 2、使用文件共享，适合在对数据同步要求不高额进程之间进行通信，SharePreferences作为文件共享的一个特例，由于系统在实现时使用了缓存的机制，所以在多进程模式下变得更不可靠；<br/>
 * 3、使用Messenger，是一种轻量级的IPC方案，底层实现也是AIDL；缺点一是采用串行运行方式，所有消息需要一个一个处理，不适合大量并发请求；二是无法实现跨进程的方法调用；<br/>
 * 4、使用AIDL，更好的支持大量并发请求，并支持夸进程的方法调用；<br/>
 * 5、ContentProvider是Android提供的专门用于不同应用间进行数据共享的方式，和Messager一样，底层实现同样是Binder，使用简单，但是要注意防止SQL注入和权限控制等问题；<br/>
 * 6、使用Socket，分为流式套接字和用户数据报套接字，分别对应于网络的传输控制层中的TCP和UDP协议。
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnMessenger, btnAIDL, btnBinder, btnProvider, btnSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnMessenger = (Button) findViewById(R.id.btn_messenger);
        btnAIDL = (Button) findViewById(R.id.btn_aidl);
        btnBinder = (Button) findViewById(R.id.btn_binder_pool);
        btnProvider = (Button) findViewById(R.id.btn_content_provider);
        btnSocket = (Button) findViewById(R.id.btn_socket);

        btnMessenger.setOnClickListener(this);
        btnAIDL.setOnClickListener(this);
        btnBinder.setOnClickListener(this);
        btnProvider.setOnClickListener(this);
        btnSocket.setOnClickListener(this);
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
            case R.id.btn_binder_pool:
                startActivity(new Intent(this, BinderPoolActivity.class));
                break;
            case R.id.btn_content_provider:
                startActivity(new Intent(this, ProviderActivity.class));
                break;
            case R.id.btn_socket:
                startActivity(new Intent(this, TCPClientActivity.class));
                break;
        }
    }
}
