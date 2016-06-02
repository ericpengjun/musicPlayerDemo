package com.example.kc28.mymusicplayler;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.kc28.mymusicplayler.adapter.MyMusicListAdapter;
import com.example.kc28.mymusicplayler.entity.Mp3Info;
import com.example.kc28.mymusicplayler.utils.Constant;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;


public class PlayRecordListActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private ListView listView;
    private PlayerApplication app;
    private ArrayList<Mp3Info> mp3Infos;
    private MyMusicListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_record_list);
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

    //初始化最近播放的数据
    private void initData() {
        try {
            //查询最近播放的记录
            List<Mp3Info> list = app.dbUtils.findAll(Selector.from(Mp3Info.class)
                    .where("playTime", "!=", 0).orderBy("playTime",true).limit(Constant.PLAY_RECORD_MUN));
            if (list == null || list.size() == 0){
                listView.setVisibility(View.GONE);
            }else {
                listView.setVisibility(View.VISIBLE);
                mp3Infos = (ArrayList<Mp3Info>) list;
                adapter = new MyMusicListAdapter(this,mp3Infos);
                listView.setAdapter(adapter);
            }
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
        if (playService.getChangePlayList() != PlayService.PLAY_RECORD_MUSIC_LIST){
            playService.setMp3Infos(mp3Infos);
            playService.setChangePlayList(PlayService.PLAY_RECORD_MUSIC_LIST);
        }
        playService.play(position);
    }
}
