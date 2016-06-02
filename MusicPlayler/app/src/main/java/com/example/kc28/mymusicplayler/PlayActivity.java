package com.example.kc28.mymusicplayler;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kc28.mymusicplayler.entity.Mp3Info;
import com.example.kc28.mymusicplayler.utils.MediaUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;

public class PlayActivity extends BaseActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener {

    private TextView songName, singer, play_start, play_end;
    private ImageView image_album, play_mode, prev, play, next,collect;
    private SeekBar seekBar;
    //private ArrayList<Mp3Info> mp3Infos;
    private static final int UPDATE_TIME = 0x1;//更新播放时间的标记

    private PlayerApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        app = (PlayerApplication) getApplication();

        songName = (TextView) findViewById(R.id.songName);
        singer = (TextView) findViewById(R.id.singer);
        play_start = (TextView) findViewById(R.id.play_start);
        play_end = (TextView) findViewById(R.id.play_end);
        image_album = (ImageView) findViewById(R.id.image_album);
        play_mode = (ImageView) findViewById(R.id.play_mode);
        prev = (ImageView) findViewById(R.id.prev);
        play = (ImageView) findViewById(R.id.play);
        next = (ImageView) findViewById(R.id.next);
        collect = (ImageView) findViewById(R.id.collect);
        play_mode.setOnClickListener(this);
        prev.setOnClickListener(this);
        play.setOnClickListener(this);
        next.setOnClickListener(this);
        collect.setOnClickListener(this);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        //mp3Infos = MediaUtils.getMp3Infos(this);
        myHandler = new MyHandler(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        //绑定播放服务
        bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        //解除绑定播放服务
        unBindPlayService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private static MyHandler myHandler;


    //进度条拖动的方法
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser){
            playService.pause();
            playService.seekTo(progress);
            playService.start();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    static class MyHandler extends Handler {

        private PlayActivity playActivity;

        public MyHandler(PlayActivity playActivity) {
            this.playActivity = playActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (playActivity != null) {
                switch (msg.what) {
                    case UPDATE_TIME:
                        playActivity.play_start.setText(MediaUtils.formatTime(msg.arg1));
                        break;
                }
            }
        }
    }


    @Override
    public void publish(int progress) {
        Message msg = myHandler.obtainMessage(UPDATE_TIME);
        msg.arg1 = progress;
        myHandler.sendMessage(msg);
        seekBar.setProgress(progress);
    }

    @Override
    public void change(int position) {
        Mp3Info mp3Info = playService.mp3Infos.get(position);
        songName.setText(mp3Info.getTitle());
        singer.setText(mp3Info.getArtist());
        Bitmap albumBitmap = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
        image_album.setImageBitmap(albumBitmap);
        play_end.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        play.setImageResource(R.drawable.play);
        seekBar.setProgress(0);
        seekBar.setMax((int) mp3Info.getDuration());
        if (playService.isPlaying()) {
            play.setImageResource(R.drawable.play_pause);
        } else {
            play.setImageResource(R.drawable.play);
        }

        switch (playService.getPlay_mode()) {
            case PlayService.ORDER_PLAY:
                play_mode.setImageResource(R.drawable.order);
                play_mode.setTag(PlayService.ORDER_PLAY);
                break;
            case PlayService.RANDOM_PLAY:
                play_mode.setImageResource(R.drawable.random);
                play_mode.setTag(PlayService.RANDOM_PLAY);
                break;
            case PlayService.SINGER_PLAY:
                play_mode.setImageResource(R.drawable.repeat1);
                play_mode.setTag(PlayService.SINGER_PLAY);
                break;
        }

        //初始化收藏状态
        try {
            Mp3Info likeMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getId()));
            if (likeMp3Info != null) {
                collect.setImageResource(R.drawable.collected);
            } else {
                collect.setImageResource(R.drawable.collect);
            }
        }catch (DbException e) {
            e.printStackTrace();
        }
    }

    private long getId(Mp3Info mp3Info) {
        //初始化收藏状态
        long id = 0;
        switch (playService.getChangePlayList()){
            case PlayService.MY_MUST_LIST:
                id = mp3Info.getId();
                break;
            case PlayService.LIKE_MUST_LIST:
                id = mp3Info.getMp3InfoId();
                break;
            default:
                break;
        }
        return id;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prev:
                playService.prev();
                break;
            case R.id.play:
                if (playService.isPlaying()) {
                    play.setImageResource(R.drawable.play);
                    playService.pause();
                } else {
                    if (playService.isPause()) {
                        play.setImageResource(R.drawable.play_pause);
                        playService.start();
                    } else {
                        playService.play(playService.getCurrentPosition());
                    }
                }
                break;
            case R.id.next:
                playService.next();
                break;
            case R.id.play_mode:
                int mode = (int) play_mode.getTag();
                switch (mode) {
                    case PlayService.ORDER_PLAY:
                        play_mode.setImageResource(R.drawable.random);
                        play_mode.setTag(PlayService.RANDOM_PLAY);
                        playService.setPlay_mode(PlayService.RANDOM_PLAY);
                        Toast.makeText(PlayActivity.this,getString(R.string.random_play),Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.RANDOM_PLAY:
                        play_mode.setImageResource(R.drawable.repeat1);
                        play_mode.setTag(PlayService.SINGER_PLAY);
                        playService.setPlay_mode(PlayService.SINGER_PLAY);
                        Toast.makeText(PlayActivity.this,getString(R.string.singer_play),Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.SINGER_PLAY:
                        play_mode.setImageResource(R.drawable.order);
                        play_mode.setTag(PlayService.ORDER_PLAY);
                        playService.setPlay_mode(PlayService.ORDER_PLAY);
                        Toast.makeText(PlayActivity.this,R.string.order_play,Toast.LENGTH_SHORT).show();

                        break;
                }
                break;
            case R.id.collect:
                Mp3Info mp3Info = playService.mp3Infos.get(playService.getCurrentPosition());
                try {
                    Mp3Info likeMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",getId(mp3Info)));
                    if (likeMp3Info == null){
                        mp3Info.setMp3InfoId(mp3Info.getId());
                        mp3Info.setIsLike(1);
                        app.dbUtils.save(mp3Info);
                        collect.setImageResource(R.drawable.collected);
                    }else {
                        int isLike = likeMp3Info.getIsLike();
                        if (isLike == 1){
                            likeMp3Info.setIsLike(0);
                            collect.setImageResource(R.drawable.collect);
                        }else {
                            likeMp3Info.setIsLike(1);
                            collect.setImageResource(R.drawable.collected);
                        }
                        app.dbUtils.update(likeMp3Info,"isLike");
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

}
