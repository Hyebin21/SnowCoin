/*
package com.snowcoin.snowcoin;
//SentoOther는 안했지만 밖으로 안나가도 여러번 전송이 가능.
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Handler;
import android.os.Message;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.LogRecord;
import java.util.Date;

public class TransferActivity extends AppCompatActivity {
    private String TAG = "TransferActivity";
    public String my_ip;
    //일반 스레드로 실행되는 작업의 결과를 화면에 출력.
    int count_who=0;
    private String ip;

    public Context context;
    String name;
    public int trans_coin;
    public Info info1;
    public DBManager manager;
    TransReceiver TReceiver;
    String data;

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context=getApplicationContext();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        //실험
        IntentFilter filter = new IntentFilter();
        filter.addAction(Transfer.TRANS_ACTION); //매칭될 액션
        TReceiver = new TransReceiver();
        registerReceiver(TReceiver, filter);

        Button ok = (Button)findViewById(R.id.ok);
        final EditText to_name = (EditText) findViewById(R.id.receiver);
        final EditText coin = (EditText) findViewById(R.id.coin_num);
        my_ip =getLocalIpAddress();
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                count_who=0;
                manager = new DBManager(context);
                name = to_name.getText().toString().trim();
                info1=manager.selectData(name);
                ip=info1.ipAddress;

                // 전송하려는 코인이 내 코인보다 많은지 확인
                Info result_info = manager.selectByIP(my_ip);
                trans_coin = Integer.parseInt(coin.getText().toString());
                int my_coin = result_info.coin;
                boolean transCompare = true;
                if(trans_coin>my_coin)
                    transCompare = false;
                else
                    transCompare = true;
                info1=manager.selectByIP(my_ip);
                String myName = info1.name;

                //여기서부터 transtime 변환
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String trans_time = sdf.format(date);

                if ((coin.getText().toString() != null) && transCompare == true ) {
                    data ="Transfer"+";"+myName+";"+to_name.getText().toString().trim()+";"+
                            coin.getText().toString().trim()+";"+ trans_time;
                    //실험 - 받는 사람한테만 전송.
                    ++count_who;
                    Intent intent2 = new Intent(
                            getApplicationContext(),//현재제어권자
                            Transfer.class); // 이동할 컴포넌트
                    intent2.putExtra("data",data);
                    intent2.putExtra("Send_to",ip);
                    startService(intent2); //잠시 주석처리

                    Toast.makeText(context,"전송 시작",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "코인이 부족합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class TransReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent arg1){
            if(count_who==1)
            {
                count_who++;
                String other_ip=SendtoOther();
                Intent intent2 = new Intent(
                        getApplicationContext(),//현재제어권자
                        Transfer.class); // 이동할 컴포넌트
                intent2.putExtra("data",data);
                intent2.putExtra("Send_to",other_ip);
                startService(intent2); //잠시 주석처리


                //서비스 한번 더 하기
            }
            if(count_who==2){
                //전송완료 다이얼로그 띄우기
                //전송완료했으니까 내 장부 업데인트하고 info수정
                Toast.makeText(context,"전송완료",Toast.LENGTH_SHORT).show();
                InsertMSG(data);
            }
            else {
                Log.d(TAG,"에 2가 아니라니 망한 각 : "+count_who);
            }

            //전송완료 다이얼로그 띄우기
            //여기서 전송한 사람 아이피 주소 받고 count까지 확인 해서 또 전송할지 확인하기.
        }
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }//end getLocalIpAddress


    private String SendtoOther(){
        DBManager manager = new DBManager(context);
        ArrayList<Info> info_result = new ArrayList<Info>();
        info_result = manager.selectAll();
        String Other_ip="";
        for(Info info : info_result) {
            if(info.ipAddress.equals(my_ip)||info.ipAddress.equals(ip)){
                ;
            }
            else{
                Other_ip= info.ipAddress;
            }//end else
        }
        return Other_ip;
    }//End SendtoOther

    private void InsertMSG(String line) {
        DBManager manager= new DBManager(context);
        DBManagerT Tmanager = new DBManagerT(context);
        String[] data = line.split(";");
        Tmanager.insertData(5, data[1], data[2], Integer.parseInt(data[3]), (data[4]), 1);
        fileIO file = new fileIO();
        int current_coin, myid;
        info1=manager.selectByIP(my_ip);
        myid=info1.Info_id;
        current_coin= info1.coin- Integer.parseInt(data[3]);
        manager.updateData(myid,current_coin);
        int current_tid = Tmanager.selectMaxTidData();
        file.writeString(context, "Block.txt", current_tid + " ");
    }
}
*/
