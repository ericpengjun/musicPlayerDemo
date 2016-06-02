package com.example.kc28.mymusicplayler;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.kc28.mymusicplayler.adapter.NetMusicAdapter;
import com.example.kc28.mymusicplayler.entity.SearchResult;
import com.example.kc28.mymusicplayler.utils.AppUtils;
import com.example.kc28.mymusicplayler.utils.Constant;
import com.example.kc28.mymusicplayler.utils.SearchMusicUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Description:
 * Company:
 * Created by Eric peng on 2015/12/23.
 */
public class NetMusicListFragment extends Fragment implements View.OnClickListener,AdapterView.OnItemClickListener{

    private MainActivity mainActivity;
    private ListView listView;
    private LinearLayout search_btn_layout,linearLayout2,loadLayout;
    private ImageButton search_btn;
    private EditText search_content;
    private ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();
    private NetMusicAdapter netMusicAdapter;
    private int page = 1;//搜索音乐的页码

    public static NetMusicListFragment newInstance(){
        NetMusicListFragment net = new NetMusicListFragment();
        return net;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_net_music_list,null);
        listView = (ListView) view.findViewById(R.id.listView);
        search_btn_layout = (LinearLayout) view.findViewById(R.id.linearLayout1);
        linearLayout2 = (LinearLayout) view.findViewById(R.id.linearLayout2);
        loadLayout = (LinearLayout) view.findViewById(R.id.loadLayout);
        search_btn = (ImageButton) view.findViewById(R.id.search_btn);
        search_content = (EditText) view.findViewById(R.id.search_content);

        listView.setOnItemClickListener(this);
        search_btn_layout.setOnClickListener(this);
        search_btn.setOnClickListener(this);
        loadNetData();
        return view;
    }

    private void loadNetData() {
        loadLayout.setVisibility(View.VISIBLE);
        //执行异步加载网络音乐的任务
        new LoadNetDataTask().execute(Constant.BAIDU_URL + Constant.BAIDU_DAYHOT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.linearLayout1:
                search_btn_layout.setVisibility(View.GONE);
                linearLayout2.setVisibility(View.VISIBLE);
                break;
            case R.id.search_btn:
                //搜索时间处理
                searchMUsic();
                break;
        }
    }

    //搜索音乐
    private void searchMUsic() {
        //隐藏输入法
        AppUtils.hideInputMethod(search_content);
        search_btn_layout.setVisibility(View.VISIBLE);
        linearLayout2.setVisibility(View.GONE);
        String key = search_content.getText().toString();
        if (TextUtils.isEmpty(key)){
            Toast.makeText(mainActivity, "请输入歌曲、歌手", Toast.LENGTH_SHORT).show();
            return;
        }
        loadLayout.setVisibility(View.VISIBLE);
        SearchMusicUtils.getInstance().setListener(new SearchMusicUtils.OnSearchResultListener(){
            @Override
            public void onSearchResult(ArrayList<SearchResult> results){
                ArrayList<SearchResult> sr = netMusicAdapter.getSearchResults();
                sr.clear();
                sr.addAll(results);
                netMusicAdapter.notifyDataSetChanged();
                loadLayout.setVisibility(View.GONE);
            }
        }).search(key, page);
    }

    //列表项的单击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position >= netMusicAdapter.getSearchResults().size() || position < 0){return;}
        showDownloadDialog(position);
    }

    //下载弹窗
    private void showDownloadDialog(final int position) {
        DownloadDialogFragment downloadDialogFragment = DownloadDialogFragment.newInstance(searchResults.get(position));
        downloadDialogFragment.show(getFragmentManager(),"download");
    }

    /**
     * 加载网络音乐的异步任务
     */
    class LoadNetDataTask extends AsyncTask<String,Integer,Integer>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            searchResults.clear();
        }

        @Override
        protected Integer doInBackground(String... params) {
            String url = params[0];
            try {
                //使用Jsoup组件请求网络，并解析音乐数据
                Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6*1000).get();
//                System.out.println(doc);
                Elements songTitle = doc.select("span.song-title");
                Elements artists = doc.select("span.author_list");
                for (int i = 0; i<songTitle.size(); i++){
                    SearchResult searchResult = new SearchResult();
                    Elements urls = songTitle.get(i).getElementsByTag("a");
                    searchResult.setUrl(urls.get(0).attr("href"));
                    searchResult.setMusicName(urls.get(0).text());

                    Elements artistElements = artists.get(i).getElementsByTag("a");
                    searchResult.setArtist(artistElements.get(0).text());

                    searchResult.setAlbum("热歌榜");
                    searchResults.add(searchResult);
                }

                System.out.println(searchResults);
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == 1){
                netMusicAdapter = new NetMusicAdapter(mainActivity,searchResults);
                listView.setAdapter(netMusicAdapter);

            }
            loadLayout.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }
}
