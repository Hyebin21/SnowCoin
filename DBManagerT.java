package com.snowcoin.snowcoin;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

//DB를 총괄관리
public class DBManagerT {

    // DB관련 상수 선언
    private static final String dbName = "Coin.db";
    private static final String tableName = "Transact";
    public static final int dbVersion = 1;

    // DB관련 객체 선언
    private OpenHelper opener; // DB opener
    private SQLiteDatabase db; // DB controller

    // 부가적인 객체들
    private Context context;

    // 생성자
    public DBManagerT(Context context) {
        this.context = context;
        this.opener = new OpenHelper(context, dbName, null, dbVersion);
        db = opener.getWritableDatabase();
    }

    // Opener of DB and Table
    private class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(Context context, String name, CursorFactory factory,
                          int version) {
            super(context, name, null, version);
            // TODO Auto-generated constructor stub
        }

        // 생성된 DB가 없을 경우에 한번만 호출됨
        @Override
        public void onCreate(SQLiteDatabase arg0) {
//                  String dropSql = "drop table if exists " + tableName;
//                  db.execSQL(dropSql);
            String tableName1 ="Info";
            String createSql ="CREATE TABLE IF NOT EXISTS " + tableName
                    + " (tid INTEGER PRIMARY KEY AUTOINCREMENT , from_id VARCHAR(20), to_id VARCHAR(20), coin_value INT, trans_time VARCHAR(35), bid INT);";
            String createSql1 ="CREATE TABLE IF NOT EXISTS " + tableName1
                    + " (Info_id INT, name VARCHAR(20), ipAddress VARCHAR(30), coin INT );";
            String inseartsql1 ="INSERT INTO " + tableName1
                    + " (Info_id, name, ipAddress, coin)  Values ( "+ 2+ ", '" + "Jihyo" + "', '" + "192.168.0.2" +"', 100 );";
            String inseartsql2 ="INSERT INTO " + tableName1
                    + " (Info_id, name, ipAddress, coin)  Values ( "+ 3+ ", '" + "Jungwoo" + "', '" + "192.168.0.3" +"', 100 );";
            String inseartsql3 ="INSERT INTO " + tableName1
                    + " (Info_id, name, ipAddress, coin)  Values ( "+ 4+ ", '" + "Hyebin" + "', '" + "192.168.0.4" +"', 100 );";

            arg0.execSQL(createSql);
            arg0.execSQL(createSql1);
            arg0.execSQL(inseartsql1);
            arg0.execSQL(inseartsql2);
            arg0.execSQL(inseartsql3);
        }
        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
        }
    }

    // 데이터 추가
    public void insertData(int tid, String from_id, String to_id, int coin_value, String trans_time, int bid) {
        String sql ="INSERT INTO " + tableName
                + " ( from_id, to_id, coin_value, trans_time, bid)  Values (  '"+from_id+"', '"+to_id+"', "+coin_value+", '"+trans_time+"', "+bid+");";
        db.execSQL(sql);
    }
/*
    // 데이터 갱신
    public void updateData(APinfo info, int index) {
        String sql = "update " + tableName + " set SSID = '" + info.getSSID()
                + "', capabilities = " + info.getCapabilities()
                + ", passwd = '" + info.getPasswd() + "' where id = " + index
                + ";";
        db.execSQL(sql);
    }

    // 데이터 삭제
    public void removeData(int index) {
        String sql = "delete from " + tableName + " where id = " + index + ";";
        db.execSQL(sql);
    }
*/

    // 데이터 검색
    public Transaction selectData(int index) {
        String sql = "select * from " + tableName + " where tid = " + index
                + ";";
        Cursor result = db.rawQuery(sql, null);

        // result(Cursor 객체)가 비어 있으면 false 리턴
        if (result.moveToFirst()) {
            Transaction transdb = new Transaction(result.getInt(0), result.getString(1), result.getString(2),result.getInt(3),result.getString(4),result.getInt(5));
            result.close();
            return transdb;
        }
        result.close();
        return null;
    }

    // 데이터 전체 검색
    public ArrayList<Transaction> selectAll() {
        String sql = "select * from " + tableName + ";";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();

        while (!results.isAfterLast()) {
            Transaction transaction = new Transaction(results.getInt(0), results.getString(1),
                    results.getString(2), results.getInt(3), results.getString(4), results.getInt(5));
            transactions.add(transaction);
            results.moveToNext();
        }
        results.close();
        return transactions;
    }

    //제일 최근의 tid 가지고 오기
    public int selectMaxTidData() {
        int result;
        String sql = "select * from " + tableName + ";";
        Cursor results = db.rawQuery(sql, null);
        result = results.getCount();
        results.close();
        return results.getCount();
    }

}