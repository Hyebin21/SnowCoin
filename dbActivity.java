/*
package com.snowcoin.snowcoin;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class dbActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);

        LinearLayout layout = (LinearLayout) findViewById(R.id.info_db);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        //infodb 표시
        ArrayList<Info> info_result = new ArrayList<Info>();
        DBManager dbManager = new DBManager(this);
        info_result = dbManager.selectAll();
        for(Info info : info_result) {
            TextView tv = new TextView(this);
            tv.setText("info_id : " + info.Info_id + ", name : " + info.name + ", ipAddress : " + info.ipAddress + ", coin : " + info.coin);
            tv.setLayoutParams(layoutParams);
            tv.setGravity(Gravity.NO_GRAVITY);
            layout.addView(tv);
        }

        LinearLayout layout2 = (LinearLayout) findViewById(R.id.tran_db);
        //trandb 표시
        ArrayList<Transaction> tran_result = new ArrayList<Transaction>();
        DBManagerT dbManagerT = new DBManagerT(this);
        tran_result = dbManagerT.selectAll();
        for(Transaction tran : tran_result){
            TextView tv = new TextView(this);
            tv.setText("tid : " + tran.tid + ", from : " + tran.from + ", to : " + tran.to + ", value : " + tran.value
                    + ", trans_time : " + tran.trans_time + ", bid : " + tran.bid);
            tv.setLayoutParams(layoutParams);
            tv.setGravity(Gravity.NO_GRAVITY);
            layout2.addView(tv);
        }
    }
}*/
