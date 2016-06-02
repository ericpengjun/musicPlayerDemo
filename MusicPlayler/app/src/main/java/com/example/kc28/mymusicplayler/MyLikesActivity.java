package com.example.kc28.mymusicplayler;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.kc28.mymusicplayler.adapter.MyMusicListAdapter;
import com.example.kc28.mymusicplayler.entity.Mp3Info;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

public class MyLikesActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private ListView listView;
    private PlayerApplication app;
    private ArrayList<Mp3Info> likeMp3Infos;
    private MyMusicListAdapter adapter;
    private boolean isChange = false; //表示当前播放列表是否为收藏的列表；
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_likes);
        app = (PlayerApplication) getApplication();
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unBindPlayService();
    }

    private void initData() {
        try {
            List<Mp3Info> list = app.dbUtils.findAll(Selector.from(Mp3Info.class).where("isLike","=","1"));
            if (list == null || list.size() == 0){
                return;
            }
            likeMp3Infos = (ArrayList<Mp3Info>) list;
            adapter = new MyMusicListAdapter(this,likeMp3Infos);
            listView.setAdapter(adapter);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (playService.getChangePlayList() != PlayService.LIKE_MUST_LIST){
            playService.setMp3Infos(likeMp3Infos);
            playService.setChangePlayList(PlayService.LIKE_MUST_LIST);
        }
        playService.play(position);
        //保存播放记录
        savePlayRecord();
    }

    //保存播放记录
    private void savePlayRecord() {
        //获取当前正在播放的音乐对象
        Mp3Info mp3Info = playService.getMp3Infos().get(playService.getCurrentPosition());
        try {
            Mp3Info playRecord = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",mp3Info.getMp3InfoId()));
            if (playRecord == null){
                mp3Info.setMp3InfoId(mp3Info.getId());
                mp3Info.setPlayTime(System.currentTimeMillis());
                app.dbUtils.save(mp3Info);
            }else {
                playRecord.setPlayTime(System.currentTimeMillis());
                app.dbUtils.update(playRecord,"playTime");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

}
