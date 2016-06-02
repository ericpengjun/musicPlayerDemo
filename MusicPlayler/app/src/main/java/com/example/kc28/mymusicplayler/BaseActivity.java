package com.example.kc28.mymusicplayler;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

import com.example.kc28.mymusicplayler.utils.Constant;

import java.util.ArrayList;

/**
 * Description:
 * Company:
 * Created by Eric peng on 2016/1/12.
 */
public abstract class BaseActivity extends FragmentActivity {

    protected PlayService playService;

    private ArrayList<Activity> list = new ArrayList<>();

    public PlayerApplication app;
    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (PlayerApplication) getApplication();
        list.add(this);
    }

    //全局退出
    public void exit(){
        for (int i=0;i<list.size();i++){
            list.get(i).finish();
        }
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayService.PlayBinder playBinder = (PlayService.PlayBinder) service;
            playService = playBinder.getPlayService();
            playService.setMusicUpdateListener(musicUpdateListener);
            musicUpdateListener.onChange(playService.getCurrentPosition());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playService = null;
            isBound = false;
        }
    };

    private PlayService.MusicUpdateListener musicUpdateListener = new PlayService.MusicUpdateListener() {
        @Override
        public void onPublish(int progress) {
            publish(progress);
        }

        @Override
        public void onChange(int position) {
            change(position);
        }
    };

    //抽象方法
    public abstract void publish(int progress);
    public abstract void change(int position);

    //绑定服务
    public void bindPlayService() {
        if (!isBound) {
            Intent intent = new Intent(this, PlayService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
            isBound = true;
        }
    }

    //解除绑定服务
    public void unBindPlayService() {
        if (isBound) {
            unbindService(conn);
            isBound = false;
        }
    }
}
