package com.example.kc28.mymusicplayler.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.kc28.mymusicplayler.PlayerApplication;

/**
 * Description:
 * Company:
 * Created by Eric peng on 2016/1/20.
 */
public class AppUtils {

    //隐藏输入法
    public static void hideInputMethod(View view){
        InputMethodManager imm = (InputMethodManager) PlayerApplication.context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()){
            imm.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
