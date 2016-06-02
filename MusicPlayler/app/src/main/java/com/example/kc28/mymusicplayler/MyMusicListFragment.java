package com.example.kc28.mymusicplayler;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kc28.mymusicplayler.adapter.MyMusicListAdapter;
import com.example.kc28.mymusicplayler.entity.Mp3Info;
import com.example.kc28.mymusicplayler.utils.MediaUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;

/**
 * Description:
 * Company:
 * Created by Eric peng on 2015/12/23.
 */
public class MyMusicListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ListView listView;
    private ImageView image_album, image_play, image_next;
    private TextView songName, singer;
    private ArrayList<Mp3Info> mp3Infos;
    private MainActivity mainActivity;
    private MyMusicListAdapter musicListAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    public static MyMusicListFragment newInstance() {
        MyMusicListFragment my = new MyMusicListFragment();
        return my;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_music_list, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        image_album = (ImageView) view.findViewById(R.id.image_album);
        songName = (TextView) view.findViewById(R.id.songName);
        singer = (TextView) view.findViewById(R.id.singer);
        image_play = (ImageView) view.findViewById(R.id.image_play);
        image_next = (ImageView) view.findViewById(R.id.image_next);

        listView.setOnItemClickListener(this);
        image_play.setOnClickListener(this);
        image_next.setOnClickListener(this);
        image_album.setOnClickListener(this);
//        loadData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //绑定播放服务
        mainActivity.bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        //解除绑定播放服务
        mainActivity.unBindPlayService();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * 加载本地音乐列表
     */
    public void loadData() {
        mp3Infos = MediaUtils.getMp3Infos(mainActivity);
        //mp3Infos = mainActivity.playService.mp3Infos;
        musicListAdapter = new MyMusicListAdapter(mainActivity, mp3Infos);
        listView.setAdapter(musicListAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mainActivity.playService.getChangePlayList() != PlayService.MY_MUST_LIST) {
            mainActivity.playService.setMp3Infos(mp3Infos);
            mainActivity.playService.setChangePlayList(PlayService.MY_MUST_LIST);
        }
        mainActivity.playService.play(position);

        //保存播放时间
        savePlayRecord();
    }

    //保存播放记录
    private void savePlayRecord() {
        Mp3Info mp3Info = mainActivity.playService.getMp3Infos().get(mainActivity.playService.getCurrentPosition());
        try {
            Mp3Info playRecord = mainActivity.app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",mp3Info.getId()));
            if (playRecord == null){
                mp3Info.setMp3InfoId(mp3Info.getId());
                mp3Info.setPlayTime(System.currentTimeMillis());
                mainActivity.app.dbUtils.save(mp3Info);
            }else {
                playRecord.setPlayTime(System.currentTimeMillis());
                mainActivity.app.dbUtils.update(playRecord,"playTime");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    //回调播放状态下的 UI 设置
    public void changeUIStatusOnPlay(int position) {
        if (position >= 0 && position < mainActivity.playService.mp3Infos.size()) {
            Mp3Info mp3Info = mainActivity.playService.mp3Infos.get(position);
            songName.setText(mp3Info.getTitle());
            singer.setText(mp3Info.getArtist());
            image_play.setImageResource(android.R.drawable.ic_media_play);
            songName.setText(mp3Info.getTitle());
            songName.setText(mp3Info.getTitle());
            if (mainActivity.playService.isPlaying()) {
                image_play.setImageResource(R.drawable.play_pause);
            } else {
                image_play.setImageResource(R.drawable.play);
            }

            Bitmap albumBitmap = MediaUtils.getArtwork(mainActivity, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
            image_album.setImageBitmap(albumBitmap);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_play:
                if (mainActivity.playService.isPlaying()) {
                    image_play.setImageResource(R.drawable.play);
                    mainActivity.playService.pause();
                } else {
                    if (mainActivity.playService.isPause()) {
                        image_play.setImageResource(R.drawable.play_pause);
                        mainActivity.playService.start();
                    } else {
                        mainActivity.playService.play(mainActivity.playService.getCurrentPosition());
                    }
                }
                break;
            case R.id.image_next:
                mainActivity.playService.next();
                break;
            case R.id.image_album:
                Intent intent = new Intent(mainActivity, PlayActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
