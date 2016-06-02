package com.example.kc28.mymusicplayler.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.kc28.mymusicplayler.R;
import com.example.kc28.mymusicplayler.entity.Mp3Info;
import com.example.kc28.mymusicplayler.utils.MediaUtils;

import java.util.ArrayList;

/**
 * Description:
 * Company:
 * Created by Eric peng on 2016/1/12.
 */
public class MyMusicListAdapter extends BaseAdapter {

    private Context ctx;
    private ArrayList<Mp3Info> mp3Infos;
    public MyMusicListAdapter(Context ctx,ArrayList<Mp3Info> musics) {
        this.ctx = ctx;
        this.mp3Infos = musics;
    }

    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    @Override
    public int getCount() {
        return mp3Infos.size();
    }

    @Override
    public Object getItem(int position) {
        return mp3Infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null){
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_music_list,null);
            vh = new ViewHolder();
            vh.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            vh.tv_artist = (TextView) convertView.findViewById(R.id.tv_artist);
            vh.tv_duration = (TextView) convertView.findViewById(R.id.tv_duration);
            convertView.setTag(vh);
        }
        vh = (ViewHolder) convertView.getTag();
        Mp3Info mp3Info = mp3Infos.get(position);
        vh.tv_title.setText(mp3Info.getTitle());
        vh.tv_artist.setText(mp3Info.getArtist());
        vh.tv_duration.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        return convertView;
    }

    static class ViewHolder{
        TextView tv_title;
        TextView tv_artist;
        TextView tv_duration;

    }
}
