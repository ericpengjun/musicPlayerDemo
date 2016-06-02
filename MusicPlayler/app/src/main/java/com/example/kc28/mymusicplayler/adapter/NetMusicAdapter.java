package com.example.kc28.mymusicplayler.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.kc28.mymusicplayler.R;
import com.example.kc28.mymusicplayler.entity.Mp3Info;
import com.example.kc28.mymusicplayler.entity.SearchResult;
import com.example.kc28.mymusicplayler.utils.MediaUtils;

import java.util.ArrayList;

/**
 * Description:
 * Company:
 * Created by Eric peng on 2016/1/12.
 */
public class NetMusicAdapter extends BaseAdapter {

    private Context ctx;
    private ArrayList<SearchResult> searchResults;
    public NetMusicAdapter(Context ctx, ArrayList<SearchResult> SearchResult) {
        this.ctx = ctx;
        this.searchResults = SearchResult;
    }

    public ArrayList<SearchResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(ArrayList<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    @Override
    public int getCount() {
        return searchResults.size();
    }

    @Override
    public Object getItem(int position) {
        return searchResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null){
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_net_music_list,null);
            vh = new ViewHolder();
            vh.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            vh.tv_artist = (TextView) convertView.findViewById(R.id.tv_artist);
            convertView.setTag(vh);
        }
        vh = (ViewHolder) convertView.getTag();
        SearchResult searchResult = searchResults.get(position);
        vh.tv_title.setText(searchResult.getMusicName());
        vh.tv_artist.setText(searchResult.getArtist());
        return convertView;
    }

    static class ViewHolder{
        TextView tv_title;
        TextView tv_artist;

    }
}
