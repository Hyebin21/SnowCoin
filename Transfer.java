package com.snowcoin.snowcoin;
//SentoOther는 안했지만 밖으로 안나가도 여러번 전송이 가능.
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.Enumeration;

public class Transfer extends Service {
    private String TAG = "Transfer";
    public String my_ip;
    //일반 스레드로 실행되는 작업의 결과를 화면에 출력.
    private Handler mMainHandler;
    String data;
    private SendThread sender = null;
    Boolean flag_T=false;
    //데이터를 서버에 전송하거나 일반 스레드를 종료.
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private HandlerThread thread;
    int Transaction_update=0;
    //서버로부터 데이터를 수신받는 기능을 수행하는 스레드 변수
    private BufferedReader networkReader = null;
    private BufferedWriter networkWriter = null;
    String my_coin;
    private String ip;
    private int PORT = 6000;
    public Context context;
    private Socket socket;
    String name;
    public int trans_coin;
    public Info info1;
    /*
        public DBManager manager;
    */
    public static final int MSG_CONNECT = 1;
    public static final int MSG_STOP = 2;
    public static final int MSG_CLIENT_STOP = 3;
    public static final int MSG_SERVER_STOP = 4;
    public static final int MSG_START = 5;
    public static final int MSG_BROAD =6;
    public static final int MSG_ERROR = 999;
    //실험 추가
    final static String TRANS_ACTION = "com.snowcoin.snowcoin.SERVICE_BR_TR";
    String Send_to;

    @Override
    public void onDestroy() {
        Log.d(TAG,"Transfer Destroy call");
        super.onDestroy();
        if (sender != null) {
            Log.d(TAG,"Transfer Destroy execute");
            mServiceHandler.sendEmptyMessage(MSG_STOP);
        }
        // thread.quitSafely();
        thread.quit();
        SystemClock.sleep(100);
    }

    @Override
    public void onCreate() {
        context=getApplicationContext();
        super.onCreate();
        my_ip =getLocalIpAddress();
        thread = new HandlerThread("HandlerThread");//handler 스레드는 따로 handler를 만들지 않아도 내장되어있음.
        try{
            thread.start();
        }catch(Exception e){
            Log.e("MINING FUCKING THREAD","THREAD FUCKING");
        }
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        mMainHandler = new Handler() {
            public void handleMessage(Message msg) {
                String m;
                switch (msg.what) {
                    case MSG_CONNECT:
                        m = "MSG_CONNECT";
/*
                        Toast.makeText(context, m, Toast.LENGTH_SHORT).show();
*/
                        break;

                    case MSG_CLIENT_STOP:
                        m = " MSG_CLIENT_STOP";
/*
                        Toast.makeText(context, m, Toast.LENGTH_SHORT).show();
*/
                        break;

                    case MSG_SERVER_STOP:
                        m = "MSG_SERVER_STOP";
/*
                        Toast.makeText(context, m, Toast.LENGTH_SHORT).show();
*/
                        break;

                    case MSG_START:
                        String line = (String)msg.obj;
                        m = "전송이 완료되었습니다";
/*
                        Toast.makeText(context, m+" : "+line, Toast.LENGTH_SHORT).show();
*/

                        break;

                    default:
                        m = "error!";
                        Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };
        //연결
    }//on create

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        data= intent.getStringExtra("data");
/*
        my_coin=intent.getStringExtra("coin");
*/
       /* Transaction_update=0;
        SendtoOther(data);

*/
        Send_to=intent.getStringExtra("Send_to");
        Log.d(TAG,"제대로 받았는가 "+Send_to);
        SystemClock.sleep(300);
        flag_T=false;
        try{
            //확인해서 1,2인지 확인하기.
            sender = new Transfer.SendThread(Send_to,PORT);
            sender.start();
            //sender스레드에서 연결을 생성.
        }catch (Exception e){
            Log.e(TAG,"SEND 스레드 열기 error");
        }
        while(true){
            if(flag_T){
                Message msg = mServiceHandler.obtainMessage();
                msg.what = MSG_START;
                msg.obj = data;
                //문자를 서버에 전달.
                mServiceHandler.sendMessage(msg);
                break;
            }
            SystemClock.sleep(200);
            Log.d(TAG,"flag_T 도는중"+flag_T);
        }

        return flags;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //핸들러 스레드에서 사용하는 핸들러
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START:
                    Message toMain = mMainHandler.obtainMessage();
                    try {
                        Log.d(TAG,"전송 핸들러 시작"+msg.obj);
                        networkWriter.write((String)msg.obj);
                        networkWriter.newLine();
                        networkWriter.flush();
                        toMain.what = MSG_START;
                    } catch (IOException e) {
                        toMain.what = MSG_ERROR;
                        Log.e(TAG, "MSG_ERROR", e);
                    }
                    toMain.obj = msg.obj;
                    mMainHandler.sendMessage(toMain);
                    //실험
                    Intent broadcastintent = new Intent();
                    broadcastintent.setAction(TRANS_ACTION);
                    broadcastintent.putExtra("Send_to", Send_to);
                    sendBroadcast(broadcastintent);
                    Log.d(TAG,"인텐트는 보냈니.."+Send_to);
                    break;
                case MSG_STOP:
                case MSG_CLIENT_STOP:
                case MSG_SERVER_STOP:
                default:
                    sender.quit();
                    sender = null;
                    break;
            }
        }
    }

    public class SendThread extends Thread {
        SocketAddress socketAddress;
        private int connection_timeout=1500;
        Boolean loop;
        String line;

        public SendThread(String ip,int port){//서버ip
            socketAddress = new InetSocketAddress(ip,port);
        }

        @Override
        public void run() {
            try {
                socket = new Socket();
/*
                socket.setSoTimeout(connection_timeout);
*/
/*
                socket.setSoLinger(true, connection_timeout);
*/

                socket.connect(socketAddress, connection_timeout);
                networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                InputStreamReader i = new InputStreamReader(socket.getInputStream());
                networkReader = new BufferedReader(i);
                flag_T=true;
                Message toMain = mMainHandler.obtainMessage();
                toMain.what = MSG_CONNECT;
                mMainHandler.sendMessage(toMain);
                loop = true;
            } catch (Exception e) {
                loop = false;
                Log.e(TAG, "socket connect fail", e);
                Message toMain = mMainHandler.obtainMessage();
                toMain.what = MSG_ERROR;
                toMain.obj = "socket connect fail";
                mMainHandler.sendMessage(toMain);
            }
            //여기서 server가 보내는 메시지를 읽어서 보여준다.
            while (loop) {
                try {
                    final Message toMain = mMainHandler.obtainMessage();
                    line = networkReader.readLine();
                    if (line == null){
                        break;
                    }
                    else{
                        Runnable showUpdate = new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context,"뭔가 받음.",Toast.LENGTH_SHORT).show();
                            }
                        };
                        mMainHandler.post(showUpdate);
                    }
                } catch (InterruptedIOException e) {
                } catch (IOException e) {
                    //       loop = false;
                    Log.e(TAG, "loop error", e);
                    /*
                    Message toMain = mMainHandler.obtainMessage();
                    toMain.what = MSG_ERROR;
                    toMain.obj = "�꽕�듃�썙�겕�뿉 �삁湲곗튂 紐삵븳 �뿉�윭媛� 諛쒖깮�뻽�뒿�땲�떎.";
                    mMainHandler.sendMessage(toMain);
                    */
                    break;
                }
            }
            //이하 모든 것들 종료

            try  {
                if (networkWriter != null) {
                    networkWriter.close();
                    networkWriter = null;
                }
                if (networkReader != null) {
                    networkReader.close();
                    networkReader = null;
                }
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
                sender = null;
                if (loop) {
                    loop = false;
                    Message toMain = mMainHandler.obtainMessage();
                    toMain.what = MSG_SERVER_STOP;
                    toMain.obj = "server_stop.";
                    mMainHandler.sendMessage(toMain);
                }
            } catch(IOException e ) {
                Log.e(TAG, "나머지 찌그레기들 종료 error", e);
                Message toMain = mMainHandler.obtainMessage();
                toMain.what = MSG_ERROR;
                toMain.obj = "error";
                mMainHandler.sendMessage(toMain);
            }
        }//end sendThread

        public void quit() {
            Log.d(TAG,"SendThread에서 quit을 호출하여 socket을 닫음.");

            loop = false;
            try {
                if (socket != null) {
                    socket.close();
                    socket = null;

                    Message toMain = mMainHandler.obtainMessage();
                    toMain.what = MSG_CLIENT_STOP;
                    toMain.obj = "error";
                    mMainHandler.sendMessage(toMain);
                }
            } catch (IOException e) {
                Log.e(TAG, "소켓 닫는 error", e);
            }
        }
    }//end sendThread

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

    private void SendtoOther(String data){
        DBManager manager = new DBManager(context);
        ArrayList<Info> info_result = new ArrayList<Info>();
        info_result = manager.selectAll();
        for(Info info : info_result) {
            if(info.ipAddress.equals(my_ip)){
            }
            else{
                SystemClock.sleep(500);
                try{
                    ++Transaction_update;
                    //확인해서 1,2인지 확인하기.
                    sender = new SendThread(info.ipAddress,PORT);

                    //sender스레드에서 연결을 생성.
                }catch (Exception e){
                    Log.e(TAG,"새로운 센드 스레드 센드투아더에서 열려고 했는데 error");
                }
                try{
                    sender.start();
                    SystemClock.sleep(500);
                }catch (Exception e){
                    Log.e(TAG,"센더를 열려고 했는데에러");
                }
                Message msg = mServiceHandler.obtainMessage();
                msg.what = MSG_START;
                msg.obj = data;
                //문자를 서버에 전달.
                mServiceHandler.sendMessage(msg);
            }//end else
        }
    }//End SendtoOther


}