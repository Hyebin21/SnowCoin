package com.snowcoin.snowcoin;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;


import static android.content.ContentValues.TAG;

/**
 * Created by sookmyung on 2018-02-07.
 */

public class fileIO {
    public int lineNum;
    public String prev;
    public String next;
    public Context context;

    public fileIO(){
        this.lineNum = 0;
    }

    public void writeString(Context context, String filename, String write_data){
        try {
            //파일에 쓰기
            FileOutputStream fos = context.openFileOutput
                    (filename, // 파일명 지정
                            Context.MODE_APPEND);// 파일이 이미 존재할 경우 뒤에 덧붙임.

            PrintWriter out = new PrintWriter(fos);
            out.print(write_data);        //println으로 하면 \n이 자동으로 뒤에 붙음
            out.close();

        }catch(Exception ex){
            Toast.makeText(context, "writeString error", Toast.LENGTH_SHORT).show();
        }
    }

    public void writeNumber(Context context, String filename, int write_data){
        try {
            //파일에 쓰기
            FileOutputStream fos = context.openFileOutput
                    (filename, // 파일명 지정
                            Context.MODE_APPEND);// 파일이 이미 존재할 경우 뒤에 덧붙임.
            PrintWriter out = new PrintWriter(fos);
            out.print(write_data);        //println으로 하면 \n이 자동으로 뒤에 붙음
            out.close();
        }catch(Exception ex){
            Toast.makeText(context, "writeNumber error", Toast.LENGTH_SHORT).show();
        }
    }

    public void writeLong(Context context, String filename, long write_data){
        try {
            //파일에 쓰기
            FileOutputStream fos = context.openFileOutput
                    (filename, // 파일명 지정
                            Context.MODE_APPEND);// 파일이 이미 존재할 경우 뒤에 덧붙임.
            PrintWriter out = new PrintWriter(fos);
            out.print(write_data);        //println으로 하면 \n이 자동으로 뒤에 붙음
            out.close();
        }catch(Exception ex){
            Toast.makeText(context, "writeLong error", Toast.LENGTH_LONG).show();
        }
    }



    //전체 읽어오기
    public StringBuffer read(Context context){
        StringBuffer data = new StringBuffer();
        try {
            //파일에서 읽어오기
            FileInputStream fis = context.openFileInput("Block.txt");//파일명
            BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));
            String str = buffer.readLine(); // 파일에서 한줄을 읽어옴
            while (str != null) {
                data.append(str + "\n");
                str = buffer.readLine();
            }
            buffer.close();
        }catch(Exception ex){
            Toast.makeText(context, "read error", Toast.LENGTH_SHORT).show();
        }
        return data;
    }

    //파일에서 줄 수 세기
    public int readLineNum(Context context){
        StringBuffer data = new StringBuffer();
        int count = 0;
        try {
            //파일에서 읽어오기
            FileInputStream fis = context.openFileInput("Block.txt");//파일명
            BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));
            String str = buffer.readLine(); // 파일에서 한줄을 읽어옴
            while (str != null) {
                count++;
                str = buffer.readLine();
            }
            this.lineNum = count;
            buffer.close();
        }catch(Exception ex){
            Toast.makeText(context, "readLineNum error", Toast.LENGTH_SHORT).show();
        }
        return this.lineNum;
    }

    //블록 갯수 세기
    public int countBlock(Context context){
        this.lineNum = readLineNum(context);
        return this.lineNum/9;
    }

    //파일에서 데이터 꺼내서 Block 객체로 저장
    public Block readLine(Context context, int bid){
        StringBuffer data = new StringBuffer();
        Block result_block = new Block();
        int currentline = (bid-1)*9;
        int count = 0;
        int i=0;
        String str;
        try {
            if(currentline>this.readLineNum(context)){
                Log.e(TAG, "null");
                return null;
            }
            //파일에서 읽어오기
            FileInputStream fis = context.openFileInput("Block.txt");//파일명
            BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));

            //currentline만큼 밑으로 읽어가기
            for(i=0; i<currentline; i++){
                str = buffer.readLine();     // 파일에서 한줄을 읽어옴
            }
            //currentline에서 이제 한줄씩 읽기
            str = buffer.readLine();
            while (str != null) {
                if(count==0){
                    //bid
                    str = str.trim();
                    result_block.bid = Integer.parseInt(str);
                }
                if(count==1){
                    //miner, miner_coin
                    str = str.trim();
                    String[] miner_info = str.split(":");
                    result_block.miner = miner_info[0].trim();
                    result_block.miner_coin = Integer.parseInt(miner_info[1].trim());
                }
                if(count==2){
                    //block_time
                    str = str.trim();
                    result_block.block_time = str;
                }
                if(count==3){
                    //numberOfZeros
                    str = str.trim();
                    result_block.numberOfZeros = Integer.parseInt(str);
                }
                if(count==4){
                    //nonce
                    str = str.trim();
                    //nonce가 update되기 전이라면 #이라고 쓰여있음 -> 경우를 달리하여 받는다.
                    if(str.equals("#"))
                        result_block.nonce = 0;
                    else
                        result_block.nonce = Integer.parseInt(str);
                }
                if(count==5){
                    //blockLength
                    str = str.trim();
                    result_block.blockLength = Integer.parseInt(str);
                }
                if(count==6){
                    //prev
                    str = str.trim();
                    result_block.prev = str;
                }
                if(count==7){
                    //hash
                    str = str.trim();
                    result_block.hash = str;
                }
                if(count==8){
                    //transaction
                    str = str.trim();

                    String[] array = str.split(" ");

                    int[] result_tid = new int[array.length];
                    //임의로 result_block.tid에 칸수 할당
                    result_block.tid = new int[array.length];
                    if(array.length>0 && (array[0].equals("")==false)) {
                        for (i = 0; i < array.length; i++) {
                            result_tid[i] = Integer.parseInt(array[i]);
                            result_block.tid[i] = result_tid[i];
                        }
                    }else{
                        result_block.tid = null;
                    }
                    result_block.tid = result_tid;
                }
                str = buffer.readLine();
                count++;
            }
            buffer.close();

        }catch(Exception ex){
            Toast.makeText(context, "readLine error", Toast.LENGTH_SHORT).show();
        }
        return result_block;
    }

    public void changeNonceHash(Context context, int new_nonce, String new_hash){
        try {
            String s="";
            File f = context.getFileStreamPath("Block.txt");
            if(f.exists() == true)
                Log.e(TAG, "true");
            RandomAccessFile raf = new RandomAccessFile(f.getAbsoluteFile(), "rw");

            //1. 현재 블록갯수 알아내기
            int current_bid = this.countBlock(context);
            //2. readLine처럼계산해서 마지막 블록직전으로 이동
            int i=0;
            for(i=0; i<(current_bid-1)*9; i++){
                s = raf.readLine();
            }
            //3. 4줄 읽고 쓰자. nonce.
            s = raf.readLine();
            s = raf.readLine();
            s = raf.readLine();
            s = raf.readLine();
            raf.write(String.valueOf(new_nonce).getBytes());
            //4. 2줄 읽고 쓰자. hash.
            s = raf.readLine();
            s = raf.readLine();
            s = raf.readLine();
            raf.write(new_hash.getBytes());

        }catch(Exception ex){
            Toast.makeText(context, "changeNonce error", Toast.LENGTH_SHORT).show();
        }
    }

}