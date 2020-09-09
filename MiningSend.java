package com.snowcoin.snowcoin;

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

/**
 * Created by user on 2018-02-22.
 */

public class MiningSend  extends Service {
    String data;
    private String TAG = "MiningSend";
    public String my_ip;
    //일반 스레드로 실행되는 작업의 결과를 화면에 출력.
    private Handler mMainHandler;
    int ok_check;
    String ok_check_s="";
    //전송 정보
    String flag="Mining";
    String miner;
    String nonce;
    String hash;
    String block_time;
    Boolean flag_MS=false;

    private MineThread sender = null;
    //데이터를 서버에 전송하거나 일반 스레드를 종료.
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private HandlerThread thread;

    //서버로부터 데이터를 수신받는 기능을 수행하는 스레드 변수
    private BufferedReader networkReader = null;
    private BufferedWriter networkWriter = null;
    private  Context context;
    private String ip;
    private int PORT = 6000;
    private Socket socket;
    public static final int MSG_CONNECT = 1;
    public static final int MSG_STOP = 2;
    public static final int MSG_CLIENT_STOP = 3;
    public static final int MSG_MINE =4;
    public static final int MSG_SERVER_STOP = 5;
    public static final int MSG_MINE_CHECK = 6;
    DBManager manager;
    final static String MININGSEND_ACTION = "com.snowcoin.snowcoin.SERVICE_BR_MS";
    String Send_to_ms;
    public static final int MSG_ERROR = 999;
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sender != null)
            mServiceHandler.sendEmptyMessage(MSG_STOP);

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
        manager= new DBManager(context);
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

                    case MSG_MINE:
                        String line = (String)msg.obj;
                        m = "전송이 완료되었습니다";
/*
                        Toast.makeText(context, m, Toast.LENGTH_SHORT).show();
*/
                        break;

                    case MSG_MINE_CHECK:
                        m = "검증을 받음";
                        Toast.makeText(context, m, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        m = "error!";
//                        Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };
        //연결
    }//on create

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ok_check=intent.getIntExtra("ok_check",0);
        Log.d(TAG,String.valueOf(ok_check));
        nonce=intent.getStringExtra("nonce");
        hash=intent.getStringExtra("hash");
        block_time=intent.getStringExtra("block_time");

        DBManager manager=new DBManager(context);
        Info info2=manager.selectByIP(my_ip);
        miner=info2.name;
        data=flag+";"+miner+";"+nonce+";"+hash+";"+block_time;

/*
        SendtoOther(data);
*/
        Send_to_ms=intent.getStringExtra("Send_to_ms");
        SystemClock.sleep(300);
        flag_MS=false;

        try{
            //확인해서 1,2인지 확인하기.
            sender = new MineThread(Send_to_ms,PORT);
            sender.start();
            Log.d(TAG,"누구에게(각각나와야함..) : "+Send_to_ms +" and "+PORT);
            //추가
            //sender스레드에서 연결을 생성.
        }catch (Exception e){
            Log.e(TAG,"SEND 스레드 열기 error");
        }
        Log.d(TAG,"start누른다음이당");
        while(true){
            if(flag_MS){
                Message msg = mServiceHandler.obtainMessage();
                msg.what = MSG_MINE;
                msg.obj = data;
                //문자를 서버에 전달.
                mServiceHandler.sendMessage(msg);
                break;
            }
            SystemClock.sleep(200);
            Log.d(TAG,"flag_MS 도는중"+flag_MS);

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
                case MSG_MINE:
                    Message toMain = mMainHandler.obtainMessage();
                    try {
                        networkWriter.write((String)msg.obj);
                        networkWriter.newLine();
                        networkWriter.flush();
                        toMain.what = MSG_MINE;
                    } catch (IOException e) {
                        toMain.what = MSG_ERROR;
                        Log.e(TAG, "MSG_ERROR", e);
                    }
                    toMain.obj = msg.obj;
                    mMainHandler.sendMessage(toMain);
                    Intent broadcastintent_ms = new Intent();
                    broadcastintent_ms.setAction(MININGSEND_ACTION);
                    broadcastintent_ms.putExtra("Send_to_ms", Send_to_ms);
                    sendBroadcast(broadcastintent_ms);
                    Log.d(TAG,"브로드인텐트를 전송 : "+Send_to_ms);
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

    public class MineThread extends Thread {
        SocketAddress socketAddress;
        private int connection_timeout=1500;
        Boolean loop;
        String line;

        public MineThread(String ip,int port){//서버ip
            socketAddress = new InetSocketAddress(ip,port);
        }

        @Override
        public void run() {
            try {
                socket = new Socket();
                /*socket.setSoTimeout(connection_timeout);
                socket.setSoLinger(true, connection_timeout);*/
                socket.connect(socketAddress, connection_timeout);
                networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                InputStreamReader i = new InputStreamReader(socket.getInputStream());
                networkReader = new BufferedReader(i);
                Log.d(TAG,"생성각");
                flag_MS=true;

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
                                if(line.equals("ok")){
/*
                                    Toast.makeText(context,"받은 메시지 "+line,Toast.LENGTH_SHORT).show();
*/
                                    toMain.what=MSG_MINE_CHECK;
                                    mMainHandler.sendMessage(toMain);

                                    //선물 증정
                                    if(ok_check==2){
                                        Info info=manager.selectByIP(my_ip);
                                        int current_coin= info.coin+3;
                                        manager.updateData(info.Info_id,current_coin);


                                        // 채굴한 사람의 장부에도 블록 생성
                                        fileIO file = new fileIO();

                                        //채굴한 nonce와 hash로 채굴자의 장부의 블록 정보도 고쳐야 함
                                        //이전블록 nonce와 hash를 채굴자가 발견한 nonce와 hash로 바꿔줌.
                                        file.changeNonceHash(context, Integer.parseInt(nonce), hash);

                                        //2. 파일에 쓴다.
                                        //2-1. bid 증가시켜야 한다. 개수를 알아와서 증가시키자.
                                        //bid, miner, block_time, numberOfZeros, nonce, blockLength, prev, hash -> 이건 장부에 기록 순서
                                        int bid = file.countBlock(context)+1;

                                        file.writeString(context, "Block.txt", "\n");
                                        file.writeNumber(context, "Block.txt", bid);
                                        file.writeString(context, "Block.txt", "\n");
                                        file.writeString(context, "Block.txt", miner+" : 3");
                                        file.writeString(context, "Block.txt", "\n");
                                        file.writeString(context, "Block.txt", block_time);
                                        file.writeString(context, "Block.txt", "\n");
                                        file.writeNumber(context, "Block.txt", 3);              //numberOfZeros
                                        file.writeString(context, "Block.txt", "\n");
                                        file.writeString(context, "Block.txt", "#                                       ");              //새로운 nonce는 #으로 해줘야한다굿
                                        file.writeString(context, "Block.txt", "\n");
                                        file.writeNumber(context, "Block.txt", 0);                //blockLength
                                        file.writeString(context, "Block.txt", "\n");
                                        file.writeString(context, "Block.txt", hash);                         //prev
                                        file.writeString(context, "Block.txt", "\n");

                                        //현재 받은 정보들로 초기 hash를 계산해서 넣어준다.
                                        int[] tid = {};
                                        Block block = new Block(miner, 3, hash, "0", tid, 0);
                                        block.hash = HashUtils.bytesToHex(HashUtils.calculateSha256(HashUtils.calculateSha256(block.toString(context))));
                                        file.writeString(context, "Block.txt", block.hash);
                                        file.writeString(context, "Block.txt", "\n ");
                                    }
                                }
                                else if(line.equals("no")){
                                    Toast.makeText(context,"검증 실패",Toast.LENGTH_SHORT).show();
                                }
                            }
                        };
                        mMainHandler.post(showUpdate);
                    }
                } catch (InterruptedIOException e) {
                } catch (IOException e) {
                    //       loop = false;
                    Log.e(TAG, "error", e);
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
                Log.e(TAG, "error", e);
                Message toMain = mMainHandler.obtainMessage();
                toMain.what = MSG_ERROR;
                toMain.obj = "error";
                mMainHandler.sendMessage(toMain);
            }
        }//end sendThread

        public void quit() {
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
                Log.e(TAG, "error", e);
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
                SystemClock.sleep(300);
                try{
/*
                    Toast.makeText(context,"내가 전송한 ip 주소 : "+info.ipAddress,Toast.LENGTH_SHORT).show();
*/

                    sender = new MineThread(info.ipAddress,PORT);
                    sender.start();
                    SystemClock.sleep(500);
                    //sender스레드에서 연결을 생성.
                }catch (Exception e){
                    Log.e(TAG,"error");
                }
                Message msg = mServiceHandler.obtainMessage();
                msg.what = MSG_MINE;
                msg.obj = data;
                //문자를 서버에 전달.
                mServiceHandler.sendMessage(msg);
            }
        }
    }//End SendtoOther
}