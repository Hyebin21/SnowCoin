package com.snowcoin.snowcoin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import static com.snowcoin.snowcoin.HashUtils.bytesToHex;
import static com.snowcoin.snowcoin.PowService.MiningTask.TAG;


public class PowService extends Service {

    static boolean isStop;
    static String hash_result;
    MiningTask m;
    Block block;
    Block current_block;
    int current_bid;                    //현재 블록 bid를 받아와서 여기에 넣으면 될듯
    fileIO file = new fileIO();
    BroadcastReceiver mReceiver;
    final static String MY_ACTION = "com.snowcoin.snowcoin.SERVICE_BR";

    myReceiver_2 mReceiver_2; //isStop받기 위한

    private NotificationManager NM;
    private Notification noti_m;
    int nonce;


    //myReceiver_2 mReceiver2;
    //일단 생성자는 안 만들었음

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("LOG", "onBind()");
        return null;
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("LOG", "onStartCommand()");
        //여기서 객체.start()로 실행시키는 것 .
        //여기서 실행될 객체를 설정하고

        //추가--------------파일에서 블록하나 읽어와서 임의로 넣은 것
        //file에서 블록정보 받아와서 기본 setting


        //isStop = false;
        int lineNum = 0;
        current_bid = file.countBlock(this);
        Log.e(TAG, "blockNum : " + current_bid);

        current_block = file.readLine(this, current_bid);

        if(current_block!=null) {
            String str = current_block.hash;
            m = new MiningTask(str, block);
        }
        //Register BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(PersistentService.MY_ACTION_2); //매칭될 액션
        mReceiver_2 = new myReceiver_2();
        registerReceiver(mReceiver_2, filter);

        //m = new MiningTask("7982970534e089b839227b7e174725ce1878731ed6d700766e59cb16f1c25e27",block);
        new Thread(task).start();
        return super.onStartCommand(intent, flags, startId);



    }

    //새로추가
    Runnable task = new Runnable(){

        @Override
        public void run() {
            Looper.prepare();
            while(m.run)
            {
                try {
                    byte[] hash = null;
                    String str = m.hex;
                    hash = hexToByteArray(str);
                    isStop = false;

                    //int numberofZerosinPrefix = block.numberOfZeros;
                    int numberofZerosinPrefix = 3;

                    nonce = mineHash(m, hash, numberofZerosinPrefix); //원본은 block에서 hash값과 0의갯수 받아오기

                    Intent broadcastintent = new Intent();
                    broadcastintent.setAction(MY_ACTION);

                    broadcastintent.putExtra("nonce", nonce);
                    sendBroadcast(broadcastintent);

                    if (nonce < 0)
                        return;
                    else
                    {
                        m.run = false;
                        //Log.d("TAG","noti");
                        // block.nonce = nonce;
                    }



                } catch (Exception ex) {
                    Log.e("Mining", ex.toString());
                }
            }
            Looper.loop();
        }

    };
    @Override
    public void onDestroy() {
        Log.e("LOG", "onDestroy()");
        super.onDestroy();
        isStop = true;
        //객체.stop()를 해주는 것 같다.
        unregisterReceiver(mReceiver_2);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("LOG", "onUnbind()");
        return super.onUnbind(intent);
    }
    //여기까지는 service위한 함수 정의 부분


    public static class MiningTask {

        public static final String TAG = "Mining ";
        public volatile boolean run = true; //Thread종료를 위한 flag 최초값

        //private final   Peer    peer; //info에서 나에 대한 값을 가져와서 peer대신 입력하면 될듯하다.
        String hex;
        Block block;
        Context context;

        //맨 처음 피어가 mining을 위해서 minetask에서 어떤 피어가 어떤 블록블록으로 어떤 값인지를 지정해서 minetask에 넣음
        public MiningTask() {
            // this.peer = null;
            this.hex = null;
            this.block = null;
        }//여기서 hex란    final String hex = HashUtils.bytesToHex(block.hash);값이다.


        public MiningTask(String hex, Block block) {
            // this.peer = peer;
            this.hex = hex;
            this.block = block;
            /*TimerTask oldTask = peer.timerMap.put(hex, this); //timerMap이란 map에 hex와 현재 처리중인 miningtask를 넣는다.
            if (oldTask != null)
                oldTask.cancel();*/
        }//어떤피어가 어떤 블록에  현재 블록의 해시값

        public MiningTask(Context context) {
            this.context = context;
        }
        //위에 생성자 과정을 거치고 진짜 mine을 시작. 기존에서는 wallet의 minehash 함수를 부른다.

    }//class MiningTask

    //이하는 이해를 위해 wallet, hashutil, pow에서 가져온것.
    //sha256으로 block.hash의 값을 전송! block.hash의 값을 보낸다.
    protected  static int mineHash(MiningTask task, byte[] sha256, long numberOfZerosInPrefix) {
        final int nonce = solve(task, sha256, numberOfZerosInPrefix);
            /*
            if (DEBUG) {
                String status = "CANCELLED";
                if (nonce >= 0)
                    status = "SOLVED";
                System.err.println(myName+" mineHash() "+status+". nonce="+nonce+"\n"+"sha256=["+HashUtils.bytesToHex(sha256)+"]\n");
            }//end if
            */
        return nonce;
    }//endminehash


    //solve를 하는데 현 블록의 hash값을 sha256으로 보내서 buffer에 전송 하고  새로운 블록의 hash값을 찾기위해 calculatesha256을 호출해서 계산한다.
    //그 결과 주어진 어떠한 값 그 계산된 hash값이 numberofzero가 만족 되는 지 확인(이게 for)하고 계속 while 안에서 반복해서 x(즉 nonce를 찾아서 return)

    /*
    public static final byte getBit(int ID, int position) {
        return (byte) ((ID >> position) & (byte)1);
    }
    */
    public static final int solve(MiningTask task, byte[] sha256, long numberOfZerosInPrefix) {
        final int length = sha256.length;
        Log.i("pow","length: "+length);

        final ByteBuffer buffer = ByteBuffer.allocate(length+4);
        buffer.put(sha256, 0, length);
        int x = 0;

        while (task.run && x < Integer.MAX_VALUE && isStop != true) {
            // append x
            buffer.putInt(length, x);

            // calculate new hash
            byte[] result = HashUtils.calculateSha256(buffer.array()); //result 배열에 새로만든  hash넣음
            final ByteBuffer bb = ByteBuffer.wrap(result); //바뀔때마다 bb에 넣어줌

            //추가
            String res = bytesToHex(result);
            String b = hexToBin(res);

            //Log.i("pow","reslt 길이: "+result.length);
            Log.i("TAG","result: "+result);
            Log.i("TAG","resulthex: "+res);

            boolean wrong = false;
            boolean done = false;
            int numOfZeros = 0;

            for (int i=0; i<res.length(); i++) {
                if((b.charAt(i)& 1) == 0) {
                    numOfZeros++;
                    Log.i("TAG","zero");
                }
                else {
                    Log.i("TAG","one");
                    wrong = true;
                    break;
                }
                if (numOfZeros == numberOfZerosInPrefix*4) {
                    hash_result = res;
                    done = true;
                    break;
                }
                if (done || wrong)
                    break;
            }//end for
            if (done)
                break;
            x++;
        }//end while
        if (!task.run)
            return Integer.MIN_VALUE;

        if(isStop){
            //isStop을 flag으로 해주자 activity에서 여기로 전달
            Log.i("POW","stop: "+isStop);
            x = -1;
            isStop=false;
        }
        return x;
    }//end solve




    public static final byte[] calculateSha256(String text) {
        byte[] hash2;
        try {
            hash2 = calculateSha256(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return hash2;
    }//end calculatesha256

    public static final byte[] calculateSha256(byte[] utf8Bytes) {
        byte[] hash2;
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash1 = digest.digest(utf8Bytes);
            hash2 = digest.digest(hash1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return hash2;
    }//end calculatesha256

    public static byte[] hexToByteArray(String hex) {

        if (hex == null || hex.length() == 0) {
            return null;
        }

        byte[] ba = new byte[hex.length() / 2];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return ba;
    }
    public static String hexToBin(String hex){
        String bin = "";
        String binFragment = "";
        int iHex;
        hex = hex.trim();
        hex = hex.replaceFirst("0x", "");

        for(int i = 0; i < hex.length(); i++){
            iHex = Integer.parseInt(""+hex.charAt(i),16);
            binFragment = Integer.toBinaryString(iHex);

            while(binFragment.length() < 4){
                binFragment = "0" + binFragment;
            }
            bin += binFragment;
        }
        return bin;
    }


    ////Broadcast통신 위한
    private class myReceiver_2 extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent arg1){
            isStop = arg1.getBooleanExtra("isStop",false);

        }
    }

}