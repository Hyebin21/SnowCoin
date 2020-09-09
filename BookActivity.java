package com.snowcoin.snowcoin;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class BookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        /*
        SlidingView sv = new SlidingView(this);
        View v1 = View.inflate(this, R.layout.t1, null);
        View v2 = View.inflate(this, R.layout.t2, null);
        sv.addView(v1);
        sv.addView(v2);
        setContentView(sv);
        */
        StringBuffer block_result = new StringBuffer();
        fileIO fileIO = new fileIO();
        block_result = fileIO.read(this);       //Block.txt 전체를 읽어서 BookActivity에 표시

        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText(block_result);

    }
}