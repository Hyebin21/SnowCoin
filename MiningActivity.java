/*
package com.snowcoin.snowcoin;

import android.app.Activity;
import android.app.AlertDialog;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import com.snowcoin.snowcoin.PowService;
import com.snowcoin.snowcoin.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.snowcoin.snowcoin.MiningSend;

public class MiningActivity extends AppCompatActivity {

    public Activity act = this; //progressBar 위한

    public static int nonce;
    myReceiver mReceiver;

    public ProgressBar pb1; //프로그레스바 설정

    private NotificationManager mNM;
    private Notification mNoti;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mining);
        Context context;
        context = getApplicationContext();
        Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(mClickListener);
        Button stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(mClickListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(PowService.MY_ACTION); //매칭될 액션
        mReceiver = new myReceiver();
        registerReceiver(mReceiver, filter);
        //fileO file = new file();
        pb1 = (ProgressBar) findViewById(R.id.progressBar1);
        pb1.setVisibility(ProgressBar.GONE); //처음에는 안보이게

    }


    Button.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.start: //수신측

                    startService(new Intent(MiningActivity.this, PowService.class));
                    //pb1.setVisibility(ProgressBar.VISIBLE);
                    startLoading(act);
                    loadingDialog.setCancelable(true);//back키 눌렀을때 캔슬안되게,
                    //하지만 back키 누르면 mainactivity 부분으로 돌아가야하잖아
                    loadingDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//배경검정색으로 바뀌는거막기
                    loadingDialog.setCanceledOnTouchOutside(false); //밖을 터치했을때 사라지게하는거 막기
                    loadingDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                    break;
                case R.id.stop:
                    //endLoading();
                    pb1.setVisibility(ProgressBar.GONE);
                    loadingDialog.dismiss();
                    Log.i("LOG", "stopbuttonClicked()");
                    stopService(new Intent(MiningActivity.this, PowService.class));
                    break;

            }


        }
    };

*/
/*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
                startActivity(new Intent(this, MainActivity.class));
                //finish();
                break;

        }
        return super.onKeyDown(keyCode, event);
    }

*//*

    //브로드캐스트 등록

    */
/** 1. intent filter를 만든다
     *  2. intent filter에 action을 추가한다.
     *  3. BroadCastReceiver를 익명클래스로 구현한다.
     *  4. intent filter와 BroadCastReceiver를 등록한다.*//*


    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mReceiver = null;
    }


    private class myReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent arg1){
            nonce = arg1.getIntExtra("nonce",0);
            if(nonce > 0) {
                //성공시간 기록
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String trans_time = sdf.format(date);

                Toast.makeText(MiningActivity.this, "nonce: "+String.valueOf(nonce),Toast.LENGTH_SHORT).show();

                NotifyInsert("SUCCESS, nonce"+nonce);
                //다디어로그 사라지게 , 채굴 완료라고 노티피케이션

                //다른 사람에게 채굴정보 전송.
*/
/*
                Intent intent1 = new Intent(
                        getApplicationContext(),//현재제어권자
                        MiningSend.class); // 이동할 컴포넌트

                //추가 전송
                intent1.putExtra("nonce",String.valueOf(nonce));
                intent1.putExtra("hash",PowService.hash_result);
                intent1.putExtra("block_time",trans_time);
                //전송할 첫번째 사람추가
                //newms
                Intent intent1 = new Intent(
                        getApplicationContext(),//현재제어권자
                        MiningSend.class); // 이동할 컴포넌트
                //추가 전송
                intent1.putExtra("nonce",String.valueOf(nonce));
                intent1.putExtra("hash",PowService.hash_result);
                intent1.putExtra("block_time",trans_time);*//*

                //newms
                //앞에서 초기화 해주고
                */
/*count_who_ms=0;
                count_who_ms++;
                mining_send_to = Miningsend_to();
                intent1.putExtra("send_to_ms",mining_send_to);
                intent1.putExtra("ok_check",count_who_ms);
                startService(intent1); //잠시 주석처리
                //앞에서 초기화 해주고
                count_who_ms=0;
                count_who_ms++;
                String mining_send_to = Miningsend_to();
                intent1.putExtra("send_to_ms",mining_send_to);
                intent1.putExtra("ok_check",count_who_ms);
                startService(intent1); //잠시 주석처리*//*


            }
            else if(nonce == 0)
            {
                Toast.makeText(MiningActivity.this, "채굴 실패",Toast.LENGTH_SHORT).show();;
                NotifyInsert("Fail");

            }
            else{
                Toast.makeText(MiningActivity.this, "채굴 중단",Toast.LENGTH_SHORT).show();;
                NotifyInsert("has Stopped");

            }

            //endLoading();
            loadingDialog.dismiss();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void NotifyInsert(String line){
        NotificationManager mNotimanager;
        mNotimanager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification noti_m = new Notification.Builder(getApplicationContext())
                .setTicker("2")
                .setContentTitle("나의 채굴 정보")
                .setContentText("Mining " +line)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setVibrate(new long[]{200})
                .setSmallIcon(R.drawable.snow)
                .setAutoCancel(true)
                .build();

        mNotimanager.notify(2,noti_m);
    }


    public ProgressDialog loadingDialog;
    public void startLoading(Context ctx){
        if(loadingDialog == null){
            loadingDialog = ProgressDialog.show(ctx,"채굴중" ,"기다려주세요",false,true);

        }
    }
    */
/*
    public void endLoading(){
        endLoader endLoader = new endLoader();
        Timer timer = new Timer(false);
        timer.schedule(endLoader, 4000);
    }
    class endLoader extends TimerTask {
        endLoader(){

        }
        public void run(){
            if(loadingDialog != null){
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        }
    }
    *//*


}*/
