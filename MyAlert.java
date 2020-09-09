package com.snowcoin.snowcoin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by kjh on 2018. 2. 28..
 */

public class MyAlert extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String line;

        Intent intent1 = getIntent();
        line = intent1.getStringExtra("data");
        String[] data = line.split(";");
        if (data[0].equals("Transfer")) {
            //만약 sweetdialog 사용시
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("코인 도착!")
                    .setContentText(data[1] + "님으로부터 " + data[3] + "코인이 도착하였습니다.")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            stopService(getIntent());
                            sDialog.cancel();
                            finish();

                        }
                    })
                    .show();

        } else {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("블록 생성!")
                    .setContentText(data[1] + "님께서 채굴을 성공하여 블록이 생성되었습니다.")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            stopService(getIntent());
                            sDialog.cancel();
                            finish();
                        }
                    })
                    .show();

        }
    }
        //version 1
/*
        setContentView(R.layout.activity_my_alert);
*/
        //version 2
        /*if(data[0].equals("Transfer")){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this); // set title
            alertDialogBuilder.setTitle("코인도착"); // set dialog message
            alertDialogBuilder.setMessage(data[1]+"님으로부터 "+data[3]+"코인이 도착하였습니다.")
                    .setCancelable(false).setNeutralButton("닫기", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    stopService(getIntent());
                    dialog.cancel();
                    finish();
                }
            }); // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create(); // show it
            alertDialog.show();
        }
        else{
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this); // set title
            alertDialogBuilder.setTitle("블록 생성"); // set dialog message
            alertDialogBuilder.setMessage(data[1]+"님께서 채굴을 성공하여 블록이 생성되었습니다.")
                    .setCancelable(false).setNeutralButton("닫기", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    stopService(getIntent());
                    dialog.cancel();
                    finish();
                }
            }); // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create(); // show it
            alertDialog.show();
        }
    }*/
    //version 3
       /* if(data[0].equals("Transfer")){
            mCustomDialog = new CustomDialog(this,
                    "코인 도착",
                    "data[1]+\"님에게 \"+data[3]+\"코인이 도착하였습니다.\\",
                    leftClickListener,
                    rightClickListener);
            mCustomDialog.show();

        }
        else if(data[0].equals("Mining")){
            mCustomDialog = new CustomDialog(this,
                    "새 블록 생성",
                    data[1]+"님께서 채굴을 성공하여 블록이 생성되었습니다.",
                    leftClickListener,
                    rightClickListener);
            mCustomDialog.show();
        }
        }
    private View.OnClickListener leftClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "왼쪽버튼 Click!!",
                    Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener rightClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "오른쪽버튼 Click!!",
                    Toast.LENGTH_SHORT).show();
            mCustomDialog.dismiss();
        }
    };*/
}