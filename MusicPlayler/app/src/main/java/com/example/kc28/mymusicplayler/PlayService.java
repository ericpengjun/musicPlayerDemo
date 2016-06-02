package com.example.kc28.mymusicplayler;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.example.kc28.mymusicplayler.entity.Mp3Info;
import com.example.kc28.mymusicplayler.utils.MediaUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 音乐播放的服务组件
 * 实现的功能：
 * 1.播放
 * 2.暂停
 * 3.上一首
 * 4.下一首
 * 5.获取当前的播放进度
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer mPlayer;
    private int currentPosition;//当前正在播放的位置

    ArrayList<Mp3Info> mp3Infos;

    private MusicUpdateListener musicUpdateListener;
    //创建线程池
    private ExecutorService es = Executors.newSingleThreadExecutor();
    private boolean isPause = false;

    //切换播放列表
    public static final int MY_MUST_LIST = 1;// 我的音乐列表
    public static final int LIKE_MUST_LIST = 2;// 我喜欢的列表
    public static final int PLAY_RECORD_MUSIC_LIST = 3; // 最近播放的列表
    private int changePlayList = MY_MUST_LIST;

    //播放模式
    public static final int ORDER_PLAY = 1;
    public static final int RANDOM_PLAY = 2;
    public static final int SINGER_PLAY = 3;
    private int play_mode = ORDER_PLAY;

    /**
     * @param play_mode ORDER_PLAY = 1;
     *                  RANDOM_PLAY = 2;
     *                  SINGER_PLAY = 3;
     */
    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    public int getPlay_mode() {
        return play_mode;
    }

    public boolean isPause() {
        return isPause;
    }

    public int getChangePlayList() {
        return changePlayList;
    }

    public void setChangePlayList(int changePlayList) {
        this.changePlayList = changePlayList;
    }

    public PlayService() {
    }

    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    private Random random = new Random();

    //播放完成监听
    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (play_mode) {
            case ORDER_PLAY:
                next();
                break;
            case RANDOM_PLAY:
                play(random.nextInt(mp3Infos.size()));
                break;
            case SINGER_PLAY:
                play(currentPosition);
                break;
            default:
                break;
        }
    }

    //播放错误监听
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    class PlayBinder extends Binder {

        public PlayService getPlayService() {
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not yet implemented");
        return new PlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PlayerApplication app = (PlayerApplication) getApplication();
        currentPosition = app.sp.getInt("currentPosition", 0);
        play_mode = app.sp.getInt("play_mode", PlayService.ORDER_PLAY);

        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mp3Infos = MediaUtils.getMp3Infos(this);
        es.execute(updateStatusRunnable);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (es != null && !es.isShutdown()) {
            es.isShutdown();
            es = null;
        }
    }

    Runnable updateStatusRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (musicUpdateListener != null && mPlayer != null && mPlayer.isPlaying()) {
                    musicUpdateListener.onPublish(getCurrentProgress());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    //播放
    public void play(int position) {
        Mp3Info mp3Info = null;
        if (position < 0 || position >= mp3Infos.size()) {
            position = 0;
        }
        mp3Info = mp3Infos.get(position);
        try {
            mPlayer.reset();
            mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
            mPlayer.prepare();
            mPlayer.start();
            currentPosition = position;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (musicUpdateListener != null) {
            musicUpdateListener.onChange(currentPosition);
        }

    }

    //暂停
    public void pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            isPause = true;
        }
    }

    //下一首
    public void next() {
        if (currentPosition + 1 > mp3Infos.size() - 1) {
            currentPosition = 0;
        } else {
            currentPosition++;
        }
        play(currentPosition);
    }

    //上一首
    public void prev() {
        if (currentPosition - 1 < 0) {
            currentPosition = mp3Infos.size() - 1;
        } else {
            currentPosition--;
        }
        play(currentPosition);
    }

    //开始
    public void start() {
        if (mPlayer != null && !mPlayer.isPlaying()) {
            mPlayer.start();
        }
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    public int getCurrentProgress() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        return mPlayer.getDuration();
    }

    public void seekTo(int msec) {
        mPlayer.seekTo(msec);
    }

    //更新状态的接口
    public interface MusicUpdateListener {
        public void onPublish(int progress);

        public void onChange(int position);
    }

    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }
}
