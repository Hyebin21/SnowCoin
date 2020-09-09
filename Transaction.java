package com.snowcoin.snowcoin;

import android.content.Context;

import java.nio.ByteBuffer;
//import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Transaction {
    //LENGTH들은 버퍼의 공간을 미리 계산하기 위해서 필요
    private static final int    FROM_LENGTH         = 4;
    private static final int    TO_LENGTH           = 4;
    private static final int    VALUE_LENGTH        = 4;
    private static final int    TRANS_TIME_LENGTH    = 8;

    //DBopener에서 Select하기 위해서는 변경
    public int                  tid;
    public String               from;      //누가
    public String               to;         //누구에게
    public int                  value;      //얼마를
    public String                 trans_time;
    public int                  bid;
    public Transaction() { }

    //파라미터에서 String header 삭제
    //DBopener에서 Select하기 위해서는 변경
    public Transaction(int tid,String from, String to, int value,String trans_time,int bid) {
        this.tid =tid;
        this.from = from;
        this.to = to;
        this.value = value;
        this.trans_time = trans_time;

        this.bid=bid;
    }
    public void updateTimestamp() {
        long time = System.currentTimeMillis();
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        this. trans_time = dayTime.format(new Date(time));
    }

    public int getBufferLength() {
        int length = FROM_LENGTH + from.getBytes().length +
                TO_LENGTH + to.getBytes().length +
                VALUE_LENGTH +
                TRANS_TIME_LENGTH;

        return length;
    }

    public void fromBuffer(ByteBuffer buffer) {
        final int fLength = buffer.getInt();
        final byte[] fBytes = new byte[fLength];
        buffer.get(fBytes, 0, fLength);
        from = new String(fBytes);
        final int tLength = buffer.getInt();
        final byte[] tBytes = new byte[tLength];
        buffer.get(tBytes, 0, tLength);
        to = new String(tBytes);
        value = buffer.getInt();
        //trans_time = buffer.getLong();
    }

    public void toBuffer(ByteBuffer buffer) {

        final byte[] fBytes = from.getBytes();
        buffer.putInt(fBytes.length);
        buffer.put(fBytes);

        final byte[] oBytes = to.getBytes();
        buffer.putInt(oBytes.length);
        buffer.put(oBytes);

        buffer.putInt(value);
        //  buffer.putLong(trans_time);

    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("from='").append(from).append("'\n");
        builder.append("to='").append(to).append("'\n");
        builder.append("value='").append(value).append("'\n");
        builder.append("time='").append(trans_time).append("'\n");

        return builder.toString();
    }

}