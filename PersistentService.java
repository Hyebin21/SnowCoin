package com.snowcoin.snowcoin;
//
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.locks.ReentrantLock;
//insert잘되고 잘 받는 버전. 아직 수전 전

public class PersistentService extends Service {
    //새로 추가
    private static final String TAG = "PersistentService";
    private Handler mHandler;
    private ArrayList<EchoThread> threadList = null;
    private ReentrantLock lock;
    private ServerThread thread = null;
    private Context context;
    private static final int MSG_ID = 1;
    private static final int MSG_INSERT = 3;
    private static final int QUIT_ID = 2;
    private static final int MSG_MINE = 4;
    private static final int MSG_ERR=5;
    //추가
    private boolean OK=true;
    private boolean NO=false;
    private String check_mining;
    final static String MY_ACTION_2 = "com.snowcoin.snowcoin.SERVICE_BR_run";
    private int port = 6000;

    private static final int MILLISINFUTURE = 1000 * 1000;
    private static final int COUNT_DOWN_INTERVAL = 1000;
    private CountDownTimer countDownTimer;
    //fileIO관련 변수
    private fileIO file = new fileIO();
    String[] data;      //data로

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        unregisterRestartAlarm();
        super.onCreate();
        initData();

        if (thread == null) {
            try {
                Log.d(TAG,"onCreate에서 새로운 서버스레드를 생성");
                thread = new ServerThread(port);
                thread.start();
            } catch (IOException e) {
            }
        } else {
        }

        mHandler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_ID:
/*
                        Toast.makeText(context, "MSG_ID  " +msg.obj, Toast.LENGTH_SHORT).show();
*/
                        break;
                    case QUIT_ID:
/*
                        Toast.makeText(context, "QUIT_ID", Toast.LENGTH_SHORT).show();
*/
                        break;
                    case MSG_INSERT:
                        String line = (String) msg.obj;
                        //일단 DB건들지 않기.
//                        PushWakeLock.acquireCpuWakeLock(context);
                        //나중에 Mine까지 잘 받고 자르면 주석해지
                        InsertMSG(line);
/*
                        Toast.makeText(context, "MSG_INSERT : "+msg.obj, Toast.LENGTH_SHORT).show();
*/
//                        PushWakeLock.releaseCpuLock();
                        //날라가서 다시 만듦
                        //tid붙이기
                        DBManagerT Tmanager = new DBManagerT(context);
                        int current_tid = Tmanager.selectMaxTidData();
                        file.writeString(context, "Block.txt", current_tid + " ");

                        //hash값 업데이트하고
                        //그거를 다시 파일에 써야한다...귀찮.....................
                        break;

                    case MSG_MINE:
/*
                        Toast.makeText(context, "MSG_MINE : "+msg.obj, Toast.LENGTH_SHORT).show();
*/
                        //채굴 성공시 block생성
                        //노티피케이션 뜨게하기.
                        //여기서 current_bid 증가
                        line = (String) msg.obj;
                        //miner, prev, hash, block_time, tid, block_length
                        NotifyMining(line);
                        //다이얼로그 띄우기


                        //1. 메시지가오면 메시지를 split으로 뽑아낸다
                        //메시지가 이렇게 온다고 가정.
                        //flag;miner;nonce;hash;block_time
                        String[] data = line.split(";");
                        String miner = data[1].trim();
                        int nonce = Integer.parseInt(data[2].trim());
                        String hash = data[3].trim();
                        String block_time = data[4].trim();

                        //Info에서 그 사람의 코인 증가시켜주기(일단 3코인)
                        DBManager manager = new DBManager(context);
                        Info info = manager.selectData(miner);
                        info.coin += 3;
                        manager.updateData(info.Info_id,info.coin);

                        //이전블록 nonce와 hash를 채굴자가 발견한 nonce와 hash로 바꿔줌.
                        file.changeNonceHash(context, nonce, hash);

                        //2. 파일에 쓴다.
                        //그럼 메시지로 오는 정보는 모가 있을까?? -> miner, hash(이게 새로 생성되는 블록의 prev가 되는 것), block_time
                        //새로 설정해줘야 하는건? hash, tid, block_length. 일단 모두임의로 설정하겠다.
                        //2-1. bid 증가시켜야 한다. 개수를 알아와서 증가시키자.
                        //그렇게 해서 block객체에 저장
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
                        break;
                    case MSG_ERR:
/*
                        Toast.makeText(context,"MSG_ERR "+msg.obj,Toast.LENGTH_SHORT).show();
*/
                        break;
                    default:
                        break;

                }
                super.handleMessage(msg);
            }
        };
    }

    public class ServerThread extends Thread {
        private Boolean loop;
        private ServerSocket server;

        public ServerThread(int port) throws IOException {
            super();
            server = new ServerSocket(port);
            Log.d(TAG,"서버소켓 생성");

            server.setSoTimeout(15000);

            threadList = new ArrayList<EchoThread>();
            lock = new ReentrantLock();
            loop = true;
        } //

        @Override
        public void run() {
            while (loop) {
                try {
                    Socket sock = server.accept();
                    Log.d(TAG,"서버소켓에서 어셉트");
                    EchoThread thread = new EchoThread(sock);
                    thread.start();
                    lock.lock();
                    threadList.add(thread);
                    lock.unlock();
                } catch (InterruptedIOException e) {
//                       e.printStackTrace();
                } catch (IOException e) {
                    Message m = new Message();
                    m.what = QUIT_ID;
                    m.obj = ("Server Thread에서 예외가 발생하였습니다." + e.toString());
                    mHandler.sendMessage(m);
                    break;
                }
            }
            try {
                if (server != null) {
                    Log.d(TAG, "SenderThread 에서 server 닫으려함." );
                    server.close();
                    server = null;
                }
            } catch (Exception e) {
                Log.e(TAG, "server종료 실패." + e);
            }
        }

        public void quit() {
            clearlist();
            loop = false;
            Log.d(TAG,"SendThread에서 quit호출");
        }

        private void clearlist() {
            if (!threadList.isEmpty()) {
                Log.d(TAG,"SendThread에서 clearlist호출");
                lock.lock();
                for (int index = 0; index < threadList.size(); ++index) {
                    threadList.get(index).quit();
                }
                lock.unlock();

            }
        }
    }//serverThread

    class EchoThread extends Thread {
        private Socket sock;
        private InetAddress inetaddr;
        private OutputStream out;
        private InputStream in;
        private PrintWriter pw;
        private BufferedReader br;

        public EchoThread(Socket sock) {
            this.sock = sock;
        } //
        @Override
        public void run() {
            try {
                inetaddr = sock.getInetAddress();
                out = sock.getOutputStream();
                in = sock.getInputStream();
                pw = new PrintWriter(new OutputStreamWriter(out));
                br = new BufferedReader(new InputStreamReader(in));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                String line = null;
                Log.d(TAG,"에코스레드에서 run안으로 들어옴.1");

                while((line = br.readLine()) != null){
                    String[] data = line.split(";");
                    Log.d(TAG,"에코스레드에서 라인을 읽음 : "+line);
                    if(data[0].equals("Transfer")){
                        Message m2 = new Message();
                        m2.what = MSG_INSERT;
                        m2.obj = line;
                        mHandler.sendMessage(m2);
                    }
                    else if(data[0].equals("Mining")){
                        //1. 수정 - 여기서 검증 함수 불러서 검증해야함.
                        //2. networkwriter로 써서 전송하기. ok인지 no인지
                        boolean isStop = true;
                        Intent broadcastintent = new Intent();
                        broadcastintent.setAction(MY_ACTION_2);
                        broadcastintent.putExtra("isStop", isStop);
                        sendBroadcast(broadcastintent);

                        //2. networkwriter로 써서 전송하기. ok인지 no인지

                        //msg 짤라서 hash랑 nonce 받아야 함.
                        byte[] hash_m = PowService.hexToByteArray(data[3]);
                        int nonce = Integer.parseInt(data[2]);
                        // 만약 ok이면 MSG_MINE으로 해서 넘기기
                        if(check(hash_m,nonce)){
                            check_mining="ok";
                            pw.println(check_mining);
                            pw.flush();
                            Message m2 = new Message();
                            m2.what = MSG_MINE;
                            m2.obj = line;
                            mHandler.sendMessage(m2);
                        }
                        else{
                            check_mining="no";
                            pw.println(check_mining);
                            pw.flush();
                            Message m2 = new Message();
                            m2.what = MSG_ERR;
                            m2.obj = "검증 했더니 틀린 Nonce이다.";
                            mHandler.sendMessage(m2);
                        }
                    }//MINING END
                /*else if(data[0].equals("Mining_Check")){
                    //수정 - 만약 ok가 두번이면 블록생성.
                    Message m2 = new Message();
                    m2.what = MSG_MINE;
                    m2.obj = line;
                    mHandler.sendMessage(m2);
                }*/
                    else{
                        Message m2 = new Message();
                        m2.what = MSG_ERR;
                        m2.obj = "Another message.";
                        mHandler.sendMessage(m2);
                    }
                }

                //기존 바로 mHandler로 보내는 것
                // .
                /**/
            } catch (InterruptedIOException e) {
//                     e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "예외가 발생하였습니다." + e);
            } finally {
                Message m3 = new Message();
                m3.what = MSG_ID;
                m3.obj = (inetaddr.getHostAddress() + "와의 접속이 종료되었습니다.");
                mHandler.sendMessage(m3);
                try {
                    lock.lock();
                    threadList.remove(this);
                    lock.unlock();
                    if (sock != null) {
                        sock.close();
                        Log.d(TAG, "EchoThread 에서 받은 데이터처리를 하고 Socket을 닫으려함." );
                        sock = null;
                    }
                    if (pw != null) {
                        pw.close();
                        pw = null;
                    }
                    if (br != null) {
                        br.close();
                        br = null;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "예외가 발생하였습니다." + e);
                }
            }
        } // run


        public void quit() {
            if (sock != null) {
                try {
                    sock.close();
                    Log.d(TAG, "Echo quit 에서 Socket을 닫으려함." );
                    sock = null;
                } catch (IOException e) {
                    Log.e(TAG, "socket종료 실패" + e);
                }
            }
        }//end quit
    }//end Thread

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartcommand시작");
        Notification notification = new Notification();
        startForeground(1, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("PersistentService", "onDestroy");
        countDownTimer.cancel();
        /**
         * 서비스 종료 시 알람 등록을 통해 서비스 재 실행
         */
        registerRestartAlarm();
    }

    /**
     * 데이터 초기화
     */
    private void initData() {
        countDownTimer();
        countDownTimer.start();
    }

    public void countDownTimer() {
        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
               /* Log.i("PersistentService", "onTick");*/
            }

            public void onFinish() {
                Log.d("PersistentService", "onFinish");
            }
        };
    }


    /**
     * 알람 매니져에 서비스 등록
     */
    private void registerRestartAlarm() {

        Log.d("000 PersistentService", "registerRestartAlarm");
        Intent intent = new Intent(PersistentService.this, RestartService.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 1 * 1000;
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        /**
         * 알람 등록
         */
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 1 * 1000, sender);

    }

    /**
     * 알람 매니져에 서비스 해제
     */
    private void unregisterRestartAlarm() {

        Log.d("000 PersistentService", "unregisterRestartAlarm");
        Intent intent = new Intent(PersistentService.this, RestartService.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        /**
         * 알람 취소
         */
        alarmManager.cancel(sender);

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void InsertMSG(String line) {
        DBManagerT Tmanager = new DBManagerT(context);
        data = line.split(";");
        //;로 잘라서 data[4]가 해
        Tmanager.insertData(5, data[1], data[2], Integer.parseInt(data[3]), (data[4]), 1);
        // Transaction trans = Tmanager.selectData(3);
        // Toast.makeText(context,trans.trans_time,Toast.LENGTH_SHORT).show();
        //Toast.makeText(context,data[4],Toast.LENGTH_SHORT).show();
        String myip=getLocalIpAddress();
        DBManager manager = new DBManager(context);
        Info info = manager.selectByIP(myip);
        if(info.name.equals(data[2])) {
            int id = info.Info_id;
            int current_coin = info.coin;
            current_coin += Integer.parseInt(data[3]);
            manager.updateData(id, current_coin);
            NotifyInsert(line);
            Intent intent;
            intent = new Intent(this, MyAlert.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.putExtra("data",line);
            startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void NotifyInsert(String line){

        String[] data = line.split(";");
        NotificationManager mNotimanager;
        mNotimanager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Intent intent= new Intent(this, MainActivity.class);
        PendingIntent pending= PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification noti = new Notification.Builder(context)
                .setTicker("코인 도착")
                .setContentTitle("코인 도착")
                .setContentText(data[1]+"님으로부터 "+data[3]+"코인이 도착하였습니다.")
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setVibrate(new long[]{200})
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build();
        mNotimanager.notify(0,noti);
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //알림을 확인했을 때(알림창 클릭) 다른 액티비티(ByNitificationActivity) 실행
        //클릭했을 때 시작할 액티비티에게 전달하는 Intent 객체 생성
        //클릭할 때까지 액티비티 실행을 보류하고 있는 PendingIntent 객체 생성
        //PendingIntent 설정
        // 클릭하면 자동으로 알림 삭제
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }
    //노티 새로 생성.
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void NotifyMining(String line){
        String[] data = line.split(";");
        NotificationManager mNotimanager;
        mNotimanager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Intent intent= new Intent(this, MainActivity.class);
        PendingIntent pending= PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification noti_m = new Notification.Builder(context)
                .setTicker("새로운 블록 생성 ")
                .setContentTitle("새 블록 생성")
                .setContentText(data[1]+"님께서 채굴을 성공하여 블록이 생성되었습니다.")
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setVibrate(new long[]{200})
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build();
        mNotimanager.notify(2,noti_m);
        Intent intent1;
        intent1 = new Intent(this, MyAlert.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.putExtra("data",line);
        startActivity(intent1);
    }//end NotifyMining

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

    public boolean check(byte[] sha256, int nonce) {
        final int length = sha256.length;
        long numberOfZerosInPrefix = 3;

        final ByteBuffer buffer = ByteBuffer.allocate(length+4);
        buffer.put(sha256, 0, length);

        // append nonce
        buffer.putInt(length, nonce);
        // calculate new hash
        final byte[] result = HashUtils.calculateSha256(buffer.array());
        // wrap in buffer for easier processing
        final ByteBuffer bb = ByteBuffer.wrap(result);


        int numOfZeros = 0;
        for (int i=0; i<bb.limit(); i++) {
            final byte b = bb.get(i);
            for (int j=0; j<8; j++) {
                final byte a = getBit(b,(i*8)+j);
                if (a == 0) {
                    numOfZeros++;
                } else {
                    NO = true;
                    break;
                }
                if (numOfZeros == numberOfZerosInPrefix) {
                    OK = true;
                    break;
                }
            }
            if (OK || NO)
                break;
        }
        return OK;
    }


    public static final byte getBit(int ID, int position) {
        return (byte) ((ID >> position) & (byte)1);
    }
}