package com.snowcoin.snowcoin;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;
import com.vinayrraj.flipdigit.lib.Flipmeter;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import cn.pedant.SweetAlert.SweetAlertDialog;
import devlight.io.library.ntb.NavigationTabBar;
import dyanamitechetan.vusikview.VusikView;
import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    private final int value = 000000;
    private Flipmeter flipMeter = null;


    String mining_send_to;
    int count_who=0;
    int count_who_ms=0;
    private static final String TAG = "MainActivity";
    String my_ip;
    private Context context;
    private Intent intent;
    private RestartService restartService;
    private int port = 6000;
    private  String data[]=new String[3];
    private Block init_block;
    public static int current_bid;          //transaction 오고갈때 이거쓰면 좋을 듯 채굴되었을때도 이거 증가시키고 장부에서도 쓰고
    public DBManager manager;
    String name;
    public int trans_coin;
    public Info info1;
    String data_t;
    String ip_to;
    TransReceiver TReceiver;
    MSReceiver MSreceiver;
    Intent intent_transfer;
    Intent intent_miningsend;
    public Activity act = this; //progressBar 위한
    String trans_time_ms;
    public static int nonce;
    myReceiver mReceiver;
    //오늘 추가
    ActionProcessButton btn;
    SweetAlertDialog pDialog;


    private NotificationManager mNM;
    private Notification mNoti;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("MainActivity","onDestroy");
        //브로드 캐스트 해제
        unregisterReceiver(restartService);
        unregisterReceiver(mReceiver);
        unregisterReceiver(TReceiver);
        unregisterReceiver(MSreceiver);
    }

    private String Miningsend_to(){
        DBManager manager = new DBManager(context);
        ArrayList<Info> info_result = new ArrayList<Info>();
        info_result = manager.selectAll();
        String Other_ip="";
        for(Info info : info_result) {
            if(info.ipAddress.equals(my_ip)){
                ;
            }
            else{
                Other_ip= info.ipAddress;
                Log.d("MiningSend_to","Other : "+Other_ip);
                break;
            }//end else
        }
        return Other_ip;
    }

    private void initData(){
        //리스타트 서비스 생성
        restartService = new RestartService();
        intent = new Intent(MainActivity.this, PersistentService.class);
        IntentFilter intentFilter = new IntentFilter("PersistentService");
        //브로드 캐스트에 등록
        registerReceiver(restartService,intentFilter);
        // 서비스 시작
        startService(intent);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        context = getApplicationContext();

        //초기 블록은 설치시 한번만 생성되도록 한다.
        Preferences Pre = new Preferences();
        DBManagerT Transmanager = new DBManagerT(this);
        manager=new DBManager(this);
        my_ip=getLocalIpAddress();
        initData();

        IntentFilter filter_m = new IntentFilter();
        filter_m.addAction(PowService.MY_ACTION); //매칭될 액션
        mReceiver = new myReceiver();
        registerReceiver(mReceiver, filter_m);

        IntentFilter filter_t = new IntentFilter();
        filter_t.addAction(Transfer.TRANS_ACTION); //매칭될 액션
        TReceiver = new TransReceiver();
        registerReceiver(TReceiver, filter_t);
        //newms
        IntentFilter filter_ms = new IntentFilter();
        filter_ms.addAction(MiningSend.MININGSEND_ACTION); //매칭될 액션
        MSreceiver = new MSReceiver();
        registerReceiver(MSreceiver, filter_ms);

        //flag값이 저장되어 있는지 확인하고 없다면 초기블록을 생성하고 flag값을 true로 저장시켜준다.
        if(Pre.getPreferences(this).equals("true")!=true){
            int[] tid = {};
            init_block = new Block("no one", 0, "0", "0", tid, 0);
            init_block.block_time = "2018-01-01 01:01:01";                  //해쉬값이 자꾸 바뀌게 되므로 초기 블록의 생성 시간 고정
            init_block.hash = HashUtils.bytesToHex(HashUtils.calculateSha256(HashUtils.calculateSha256(init_block.toString(this))));

            Log.e(TAG, init_block.hash);
            fileIO file = new fileIO();
            //0
            file.writeNumber(context, "Block.txt", init_block.bid);
            file.writeString(context, "Block.txt", "\n");
            //1
            file.writeString(context, "Block.txt", init_block.miner + " : " + init_block.miner_coin);
            file.writeString(context, "Block.txt", "\n");
            //2
            file.writeString(context, "Block.txt", init_block.block_time);
            file.writeString(context, "Block.txt", "\n");
            //3
            file.writeNumber(context, "Block.txt", init_block.numberOfZeros);
            file.writeString(context, "Block.txt", "\n");
            //4
            file.writeString(context, "Block.txt", "#                                       ");               //nonce
            file.writeString(context, "Block.txt", "\n");
            //5
            file.writeNumber(context, "Block.txt", init_block.blockLength);
            file.writeString(context, "Block.txt", "\n");
            //6
            file.writeString(context, "Block.txt", init_block.prev); //prev
            file.writeString(context, "Block.txt", "\n");
            //7
            //초기 블록 해쉬 생성해야 함.
            file.writeString(context, "Block.txt", init_block.hash); //hash
            file.writeString(context, "Block.txt", "\n ");
            //8
            //trans 아직 없으니가 trans 내용은 기입하지 않음.
            Pre.savePreferences(this);
        }
    }
/*
    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_main);

        *//*TextView coin_num = (TextView) findViewById(R.id.coin_num);
        Info result_info = InfoManager.selectByIP(my_ip);
        coin_num.setText(Integer.toString(result_info.coin));
*//*    }*/

    private class TransReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent arg1){
            if(count_who==1)
            {

                try{
                    count_who++;
                    String other_ip=SendtoOther(ip_to);
                    //서비스를 스탑하는 코드추가.Transfer Destory가 콜되었는지 확인.
/*
                    stopService(intent_transfer);
*/
                    Intent intent_transfer2= new Intent(
                            getApplicationContext(),//현재제어권자
                            Transfer.class); // 이동할 컴포넌트
                    intent_transfer2.putExtra("data",data_t);
                    intent_transfer2.putExtra("Send_to",other_ip);
/*
                    intent_transfer2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
*/
                    startService(intent_transfer2); //잠시 주석처리
                }catch(Exception e){
                    Log.d(TAG,"두번째 전송하기 위해 실패");
                    btn.setProgress(-1);
                    btn.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ActionProcessButton ok = (ActionProcessButton) findViewById(R.id.ok);
                            ok.setMode(ActionProcessButton.Mode.ENDLESS);
                            ok.setProgress(0);
                            //to test the animations, when we touch the button it will start counting

                            final EditText to_name = (EditText) findViewById(R.id.receiver);
                            final EditText coin = (EditText) findViewById(R.id.coin_num);
                            to_name.setText("");
                            coin.setText("");
                        }
                    },3000);
                }//trycatch end
                //서비스 한번 더 하기
            }
            else if(count_who==2){
                //전송완료 다이얼로그 띄우기
                //전송완료했으니까 내 장부 업데인트하고 info수정
                //오늘 추가
                btn.setProgress(100);
/*
                Toast.makeText(context,"전송완료",Toast.LENGTH_LONG).show();
*/
                InsertMSG(data_t);
                btn.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ActionProcessButton ok = (ActionProcessButton) findViewById(R.id.ok);
                        ok.setMode(ActionProcessButton.Mode.ENDLESS);
                        ok.setProgress(0);
//to test the animations, when we touch the button it will start counting

                        final EditText to_name = (EditText) findViewById(R.id.receiver);
                        final EditText coin = (EditText) findViewById(R.id.coin_num);
                        to_name.setText("");
                        coin.setText("");
                    }
                },3000);
            }
            else {
                btn.setProgress(-1);
/*
                Toast.makeText(context,"에 2가 아니라니 망한 각 : "+count_who,Toast.LENGTH_SHORT).show();
*/
                Log.d(TAG,"에 2가 아니라니 망한 각 : "+count_who);
            }

            //여기서 전송한 사람 아이피 주소 받고 count까지 확인 해서 또 전송할지 확인하기.
        }
    }

    private class MSReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent arg1){
            if(count_who_ms==1)
            {
                count_who_ms++;
                //다음 전송할 사람을 찾아야한다.
                String other_ip=SendtoOther(mining_send_to);
                //서비스를 스탑하는 코드추가.Transfer Destory가 콜되었는지 확인.
                stopService(intent_miningsend);
                Intent intent_ms2= new Intent(
                        getApplicationContext(),//현재제어권자
                        MiningSend.class); // 이동할 컴포넌트
                intent_ms2.putExtra("nonce",String.valueOf(nonce));
                intent_ms2.putExtra("hash",PowService.hash_result);
                intent_ms2.putExtra("block_time",trans_time_ms);
                intent_ms2.putExtra("ok_check",count_who_ms);
                intent_ms2.putExtra("Send_to_ms",other_ip);
                Log.d(TAG,"두번째 전송하기 위해  : "+"  : "+other_ip);
/*
                Toast.makeText(context,"두번째 전송  : "+other_ip,Toast.LENGTH_LONG).show();
*/
                startService(intent_ms2); //잠시 주석처리
                //서비스 한번 더 하기
            }
            else if(count_who_ms==2){

                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("채굴 성공")
                        .setContentText("채굴이 성공하였습니다.")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog
                                        .setConfirmText("확인")
                                        .dismissWithAnimation();
                            }
                        })
                        .show();
                //전송완료 다이얼로그 띄우기
                //전송완료했으니까 내 장부 업데인트하고 info수정
/*
                Toast.makeText(context,"전송완료",Toast.LENGTH_LONG).show();
*/
/*
                InsertMSG(data_t);

*/


            }
            else {
/*
                Toast.makeText(context,"에 2가 아니라니 망한 각 : "+count_who,Toast.LENGTH_SHORT).show();
*/
                Log.d(TAG,"에 2가 아니라니 망한 각 : "+count_who);
            }
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


    private String SendtoOther(String ip_already){
        DBManager manager = new DBManager(context);
        ArrayList<Info> info_result = new ArrayList<Info>();
        info_result = manager.selectAll();
        String Other_ip="";
        for(Info info : info_result) {
            if(info.ipAddress.equals(my_ip)||info.ipAddress.equals(ip_already)){
                Log.d("Sendtoother",my_ip+"  ip_to :  "+ip_to);                ;
            }
            else{
                Other_ip= info.ipAddress;
                Log.d("SendtoOther","Other : "+Other_ip);
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

    private void initUI() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.vp_horizontal_ntb);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                View view = new View(getApplicationContext());
                if(position==0){
                    view = LayoutInflater.from(
                            getBaseContext()).inflate(R.layout.activity_db, null, false);
                    container.addView(view);
                    VusikView vusikView = (VusikView) findViewById(R.id.vusik);
                    int[]  myImageList = new int[]{R.drawable.snow1};

                    vusikView
                            .setImages(myImageList)
                            .start();
                    Info result_info = manager.selectByIP(my_ip);

                    TextView my_name= (TextView) findViewById(R.id.my_name);
                    my_name.setText(result_info.name);

                    TextView coin_num = (TextView) findViewById(R.id.my_coin);
                    coin_num.setText(Integer.toString(result_info.coin));

/*
                    VusikView vusikView = (VusikView) findViewById(R.id.vusik);
                    int[]  myImageList = new int[]{R.drawable.snow1};

                    vusikView
                            .setImages(myImageList)
                            .start();
                    Info result_info = manager.selectByIP(my_ip);*/
/*

                    TextView my_name= (TextView) findViewById(R.id.my_name);
                    my_name.setText(result_info.name);

                    TextView coin_num = (TextView) findViewById(R.id.my_coin);
                    coin_num.setText(Integer.toString(result_info.coin));
*/

                    /*LinearLayout layout = (LinearLayout) findViewById(R.id.info_db);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    //infodb 표시
                    ArrayList<Info> info_result = new ArrayList<Info>();
                    DBManager dbManager = new DBManager(context);
                    info_result = dbManager.selectAll();
                    for(Info info : info_result) {
                        TextView tv = new TextView(context);
                        tv.setText("info_id : " + info.Info_id + ", name : " + info.name + ", ipAddress : " + info.ipAddress + ", coin : " + info.coin);
                        tv.setLayoutParams(layoutParams);
                        tv.setGravity(Gravity.NO_GRAVITY);
                        layout.addView(tv);
                    }

                    LinearLayout layout2 = (LinearLayout) findViewById(R.id.tran_db);
                    //trandb 표시
                    ArrayList<Transaction> tran_result = new ArrayList<Transaction>();
                    DBManagerT dbManagerT = new DBManagerT(context);
                    tran_result = dbManagerT.selectAll();
                    for(Transaction tran : tran_result){
                        TextView tv = new TextView(context);
                        tv.setText("tid : " + tran.tid + ", from : " + tran.from + ", to : " + tran.to + ", value : " + tran.value
                                + ", trans_time : " + tran.trans_time + ", bid : " + tran.bid);
                        tv.setLayoutParams(layoutParams);
                        tv.setGravity(Gravity.NO_GRAVITY);
                        layout2.addView(tv);
                    }*/

                }
                if(position==1){
                    view = LayoutInflater.from(
                            getBaseContext()).inflate(R.layout.activity_transfer, null, false);
                    container.addView(view);
                    /*Button ok = (Button)findViewById(R.id.ok);*/
                    ActionProcessButton ok = (ActionProcessButton) findViewById(R.id.ok);
                    ok.setMode(ActionProcessButton.Mode.ENDLESS);
                    ok.setProgress(0);
//to test the animations, when we touch the button it will start counting

                    final EditText to_name = (EditText) findViewById(R.id.receiver);
                    final EditText coin = (EditText) findViewById(R.id.coin_num);
                    my_ip =getLocalIpAddress();

                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View V) {
                            //오늘 추가
                            btn = (ActionProcessButton) V;
                            // we add 25 in the button progress each click
                            btn.setProgress(btn.getProgress() + 25);

                            count_who=0;
                            manager = new DBManager(context);
                            name = to_name.getText().toString().trim();
                            boolean name_compare=false;
                            boolean transCompare = true;
                            String myName="";
                            String trans_time="";
                            if(name.equals("Jihyo")||name.equals("Jungwoo")||name.equals("Hyebin")){
                                name_compare=true;
                                info1=manager.selectData(name);
                                ip_to=info1.ipAddress;
                                // 전송하려는 코인이 내 코인보다 많은지 확인
                                Info result_info = manager.selectByIP(my_ip);
                                trans_coin = Integer.parseInt(coin.getText().toString());
                                int my_coin = result_info.coin;
                                if(trans_coin>my_coin)
                                    transCompare = false;
                                else
                                    transCompare = true;
                                info1=manager.selectByIP(my_ip);
                                myName = info1.name;

                                //여기서부터 transtime 변환
                                long now = System.currentTimeMillis();
                                Date date = new Date(now);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                trans_time = sdf.format(date);

                            }

                            if ((coin.getText().toString() != null) && transCompare == true &&name_compare==true) {
                                data_t ="Transfer"+";"+myName+";"+to_name.getText().toString().trim()+";"+
                                        coin.getText().toString().trim()+";"+ trans_time;
                                //실험 - 받는 사람한테만 전송.
                                ++count_who;
                                intent_transfer = new Intent(
                                        getApplicationContext(),//현재제어권자
                                        Transfer.class); // 이동할 컴포넌트
                                intent_transfer.putExtra("data",data_t);
                                intent_transfer.putExtra("Send_to",ip_to);
                                try{
                                    startService(intent_transfer); //잠시 주석처리
                                }catch (Exception e){
                                    Log.d(TAG,"첫번째 전송하기 위해 실패");
                                    btn.setProgress(-1);
                                    btn.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            ActionProcessButton ok = (ActionProcessButton) findViewById(R.id.ok);
                                            ok.setMode(ActionProcessButton.Mode.ENDLESS);
                                            ok.setProgress(0);
                                            //to test the animations, when we touch the button it will start counting
                                            final EditText to_name = (EditText) findViewById(R.id.receiver);
                                            final EditText coin = (EditText) findViewById(R.id.coin_num);
                                            to_name.setText("");
                                            coin.setText("");
                                        }
                                    },3000);
                                }

                            }else if(transCompare==false){
                                //오늘 추가 실패시에
                                btn.setProgress(-1);
                                Toast.makeText(context, "코인이 부족합니다.", Toast.LENGTH_SHORT).show();
                                changeview(V);

                            }
                            else if(name_compare==false){
                                btn.setProgress(-1);
                                Toast.makeText(context, "등록되지 않은 사용자입니다.", Toast.LENGTH_SHORT).show();
                                changeview(V);
                            }
                        }
                    });

                }
                if(position==2){
                    view = LayoutInflater.from(
                            getBaseContext()).inflate(R.layout.activity_mining, null, false);
                    container.addView(view);

                    flipMeter = (Flipmeter) findViewById(R.id.Flipmeter);
                    FButton start = (FButton) findViewById(R.id.start);
                    FButton stop = (FButton) findViewById(R.id.stop);
                    pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.setTitleText("채굴중");

                    start.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View V) {

                            Log.i("Main","start");
                            startService(new Intent(MainActivity.this, PowService.class));
                            //SystemClock.sleep(100);
                            //SharedPreferences sp = context.getSharedPreferences("my_db",MODE_PRIVATE);
                            //FButton sn = (FButton) V;
                            pDialog.show();
                            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                            pDialog.setCancelable(false);
                            pDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//배경검정색으로 바뀌는거막기
                            pDialog.setCanceledOnTouchOutside(false);
                            pDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                            // SharedPreferences sp = getSharedPreferences("my db",Context.MODE_PRIVATE);
                            //int nonce_live = sp.getInt("my_db",0);
                            try{
                                flipMeter.setValue(73464, true);
                                // if(nonce_live != 0)
                               //     flipMeter.setValue(value, true);

                            }catch (Exception e){
                                Log.e(TAG,"flip d");

                            }
                            //final SharedPreferences sp = getSharedPreferences("my_db", Context.MODE_PRIVATE);
                            }


                    });
                    stop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View V) {
                            try{

                               flipMeter.setValue(value, true);
                            }catch (Exception e){
                                Log.e(TAG,"flip d");
                            }

                           // flipMeter.setValue(value, true);
                            pDialog.dismiss();
                            Log.i("LOG", "stopbuttonClicked()");
                            stopService(new Intent(MainActivity.this, PowService.class));
                            flipMeter.setValue(value, false);
                        }
                    });


                }
                if(position==3){
                    if(position==3){


//                    view = LayoutInflater.from(
//                            getBaseContext()).inflate(R.layout.activity_book, null, false);
//                    container.addView(view);
//                    StringBuffer block_result = new StringBuffer();
//                    fileIO fileIO = new fileIO();
//                    block_result = fileIO.read(context);       //Block.txt 전체를 읽어서 BookActivity에 표시
//
//                    Block block = fileIO.readLine(context, 1);
//                    Log.e(TAG, block.block_time);
//                    Log.e(TAG, block.tid.length+"dd");
//                    Log.e(TAG, block.tid[1]+"ee");
//                    Log.e(TAG, block.tid[2]+"ff");
//                    TextView tv = (TextView) findViewById(R.id.textView);
//                    tv.setText(block_result);



                        AndroidTreeView tView;
                        view = LayoutInflater.from(
                                getBaseContext()).inflate(R.layout.fragment_default, null, false);
                        final ViewGroup containerView = (ViewGroup) view.findViewById(R.id.container);
                        final TreeNode root = TreeNode.root();

                        //현재 블록 수 만큼 TreeNode생성하기
                        fileIO file = new fileIO();
                        int blocknum = file.countBlock(context);
                        int i=0;        Block block;
                        TreeNode[] treeArray = new TreeNode[blocknum];

                        for(i=0; i<blocknum; i++){
                            treeArray[i] = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.drawable.ic_user, "BLOCK : " + (i+1))).setViewHolder(new ProfileHolder(context));;
                            block = file.readLine(context, i+1);
                            addProfileData(block, treeArray[i]);

                        }
                        root.addChildren(treeArray);

                        tView = new AndroidTreeView(context, root);
                        tView.setDefaultAnimation(true);
                        tView.setDefaultContainerStyle(R.style.TreeNodeStyleDivided, true);
                        containerView.addView(tView.getView());
                        container.addView(view);

                    }
                }
                return view;
            }
            private void addProfileData(Block block, TreeNode profile) {
                TreeNode miner = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_people, "채굴자")).setViewHolder(new HeaderHolder(context));
                TreeNode time = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_place, "블록 생성 시각")).setViewHolder(new HeaderHolder(context));
                TreeNode previous = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_people, "이전 블록 해시")).setViewHolder(new HeaderHolder(context));
                TreeNode current = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_people, "현재 블록 해시")).setViewHolder(new HeaderHolder(context));
                TreeNode translist = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_people, "거래 내역")).setViewHolder(new HeaderHolder(context));

                /*TreeNode facebook = new TreeNode(new SocialViewHolder.SocialItem(R.string.ic_post_facebook)).setViewHolder(new SocialViewHolder(context));
                TreeNode linkedin = new TreeNode(new SocialViewHolder.SocialItem(R.string.ic_post_linkedin)).setViewHolder(new SocialViewHolder(context));
                TreeNode google = new TreeNode(new SocialViewHolder.SocialItem(R.string.ic_post_gplus)).setViewHolder(new SocialViewHolder(context));
                TreeNode twitter = new TreeNode(new SocialViewHolder.SocialItem(R.string.ic_post_twitter)).setViewHolder(new SocialViewHolder(context));*/

                //채굴자
                TreeNode miner_sub = new TreeNode(new PlaceHolderHolder.PlaceItem(block.miner)).setViewHolder(new PlaceHolderHolder(context));
                //블록 생성 시각
                TreeNode  time_sub= new TreeNode(new PlaceHolderHolder.PlaceItem(block.block_time)).setViewHolder(new PlaceHolderHolder(context));
                //이전 블록 해시
                TreeNode previous_sub = new TreeNode(new PlaceHolderHolder.PlaceItem(block.prev)).setViewHolder(new PlaceHolderHolder(context));
                //현재 블록 해시
                TreeNode current_sub = new TreeNode(new PlaceHolderHolder.PlaceItem(block.hash)).setViewHolder(new PlaceHolderHolder(context));
                //거래 내역
                // - tid 배열로 db에서 뽑아와야함.
                DBManagerT tmanager = new DBManagerT(context);
                int i=0;        int t;      Transaction transaction;
                TreeNode[] treeNodes = new TreeNode[block.tid.length];

                if((block.tid.length==1 && block.tid[0]==0)==false){
                    for(i=0; i<block.tid.length; i++){
                        transaction = tmanager.selectData(block.tid[i]);
                        treeNodes[i] = new TreeNode(new PlaceHolderHolder.PlaceItem(transaction.from+"님이 "+transaction.to+"님에게 "
                                +transaction.value+"코인 송금")).setViewHolder(new PlaceHolderHolder(context));
                        translist.addChildren(treeNodes[i]);
                    }
                }
                miner.addChildren(miner_sub);
                time.addChildren(time_sub);
                previous.addChildren(previous_sub);
                current.addChildren(current_sub);
                profile.addChildren(miner, time, previous, current, translist);
            }
        });

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_user),
                        Color.parseColor(colors[2]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_sixth))
                        .badgeTitle("snow")
                        .title("홈")
                        .build()


        );

        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_transfer),
                        Color.parseColor(colors[1]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .badgeTitle("coin")
                        .title("송금")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_mining),
                        Color.parseColor(colors[0]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_seventh))
                        .badgeTitle("mining")
                        .title("채굴")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_book),
                        Color.parseColor(colors[3]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .badgeTitle("book")
                        .title("장부")
                        .build()
        );


        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                navigationTabBar.getModels().get(position).hideBadge();
            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });

        navigationTabBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < navigationTabBar.getModels().size(); i++) {
                    final NavigationTabBar.Model model = navigationTabBar.getModels().get(i);
                    navigationTabBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            model.showBadge();
                        }
                    }, i * 100);
                }
            }
        }, 500);


    }
    private class myReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent arg1){

            nonce = arg1.getIntExtra("nonce",0);
            /*
            SharedPreferences sp = getSharedPreferences("my_db",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("nonce",nonce);
            editor.commit();
            */


            if(nonce > 0) {
                //성공시간 기록
                try{
                    flipMeter.setValue(nonce, true);
                }catch (Exception e){
                    Log.e(TAG,"flip d");

                }

                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                trans_time_ms = sdf.format(date);

/*
                Toast.makeText(MainActivity.this, "nonce: "+String.valueOf(nonce),Toast.LENGTH_SHORT).show();
*/

                NotifyInsert("SUCCESS, nonce"+nonce);
                //다디어로그 사라지게 , 채굴 완료라고 노티피케이션

                //다른 사람에게 채굴정보 전송.
                //추가 전송
                intent_miningsend = new Intent(
                        getApplicationContext(),//현재제어권자
                        MiningSend.class); // 이동할 컴포넌트
                //추가 전송
                intent_miningsend.putExtra("nonce",String.valueOf(nonce));
                intent_miningsend.putExtra("hash",PowService.hash_result);
                intent_miningsend.putExtra("block_time",trans_time_ms);
                //newms
                //앞에서 초기화 해주고
                count_who_ms=1;
                mining_send_to = Miningsend_to();
                intent_miningsend.putExtra("Send_to_ms",mining_send_to);
                intent_miningsend.putExtra("ok_check",count_who_ms);
                Log.d(TAG,"ok check"+String.valueOf(count_who_ms));
                Log.d(TAG,"첫번째 전송"+mining_send_to);

                startService(intent_miningsend); //잠시 주석처리

                pDialog.dismiss();



            }
            else if(nonce == 0)
            {
                try {
                    flipMeter.setValue(value, true);
                }catch(Exception e){
                    Log.e("TAG","nonce =0");

                    }
/*
                Toast.makeText(MainActivity.this, "채굴 실패",Toast.LENGTH_SHORT).show();;
*/
                NotifyInsert("Fail");
                pDialog.dismiss();
                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("채굴 실패")
                        .setContentText("채굴이 실패하였습니다.")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog
                                        .setConfirmText("확인")
                                        .dismissWithAnimation();
                            }
                        })
                        .show();
            }
            else{
                try {
                    flipMeter.setValue(value, true);
                }catch(Exception e){
                    Log.e("TAG","nonce < 0");

                }
                NotifyInsert("has Stopped");
                pDialog.dismiss();
                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("채굴 중단")
                        .setContentText("채굴이 중단되었습니다.")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog
                                        .setConfirmText("확인")
                                        .dismissWithAnimation();
                            }
                        })
                        .show();

            }



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
                .setSmallIcon(R.drawable.icon)
                .setAutoCancel(true)
                .build();

        mNotimanager.notify(2,noti_m);
    }

    public void changeview(View V){
        V.postDelayed(new Runnable() {
            @Override
            public void run() {
                ActionProcessButton ok = (ActionProcessButton) findViewById(R.id.ok);
                ok.setMode(ActionProcessButton.Mode.ENDLESS);
                ok.setProgress(0);
//to test the animations, when we touch the button it will start counting

                final EditText to_name = (EditText) findViewById(R.id.receiver);
                final EditText coin = (EditText) findViewById(R.id.coin_num);
                to_name.setText("");
                coin.setText("");
            }
        },2000);
    }

}
