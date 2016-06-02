package com.example.kc28.mymusicplayler;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.kc28.mymusicplayler.utils.Constant;
import com.lidroid.xutils.DbUtils;

/**
 * Description:
 * Company:
 * Created by Eric peng on 2016/1/16.
 */
public class PlayerApplication extends Application {

    public static SharedPreferences sp;
    public static DbUtils dbUtils;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences(Constant.SP_NAME,Context.MODE_PRIVATE);
        dbUtils = DbUtils.create(getApplicationContext(),Constant.DB_NAME);
        context = getApplicationContext();
    }
}
