package com.snowcoin.snowcoin;

import android.content.Context;
import android.content.SharedPreferences;


import static android.content.Context.MODE_PRIVATE;

/**
 * Created by sookmyung on 2018-02-23.
 */

public class Preferences {

    public Preferences(){    }

    // 값 불러오기
    public String getPreferences(Context context){
        String result;
        SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
        result = pref.getString("flag", "");
        return result;
    }

    //값 저장하기
    public void savePreferences(Context context){
        SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("flag", "true");
        editor.commit();
    }

}