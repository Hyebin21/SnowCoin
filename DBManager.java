package com.snowcoin.snowcoin;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.widget.Toast;

//DB를 총괄관리
public class DBManager {

    // DB관련 상수 선언
    private static final String dbName = "Coin.db";
    private static final String tableName = "info";
    public static final int dbVersion = 1;

    // DB관련 객체 선언
    private OpenHelper opener; // DB opener
    private SQLiteDatabase db; // DB controller

    // 부가적인 객체들
    private Context context;

    // 생성자
    public DBManager(Context context) {
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
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
        }
    }

    // 데이터 추가
    public void insertData(int info_id, String name, String ipAddress, int coin) {
        String sql ="INSERT INTO " + tableName
                + " (Info_id, name, ipAddress, coin )  Values ( "+ info_id + ", '" + name + "', '" + ipAddress +"', " + coin +" );";
        db.execSQL(sql);
        Toast.makeText(context, "insert complete", Toast.LENGTH_SHORT).show();
    }

    // 데이터 갱신
    public void updateData( int Info_id,int coin) {
        String sql = "update " + tableName + " set coin = " + coin + " where Info_id = " + Info_id
                + ";";
        db.execSQL(sql);
    }

    // 데이터 삭제
    public void removeData(int index) {
        String sql = "delete from " + tableName + " where Info_id = " + index + ";";
        db.execSQL(sql);
    }

    // 데이터 검색
    public Info selectData(String name) {
        String sql = "select * from " + tableName + " where name = '" + name + "';";
        Cursor result = db.rawQuery(sql, null);

        // result(Cursor 객체)가 비어 있으면 false 리턴
        if (result.moveToFirst()) {
            Info info = new Info(result.getInt(0), result.getString(1),
                    result.getString(2), result.getInt(3));
            result.close();
            return info;
        }
        result.close();
        return null;
    }

    public Info selectByIP(String ip) {
        String sql = "select * from " + tableName + " where ipAddress = '" + ip + "';";
        Cursor result = db.rawQuery(sql, null);

        // result(Cursor 객체)가 비어 있으면 false 리턴
        if (result.moveToFirst()) {
            Info info = new Info(result.getInt(0), result.getString(1),
                    result.getString(2), result.getInt(3));
            result.close();
            return info;
        }
        result.close();
        return null;
    }

    // 데이터 전체 검색
    public ArrayList<Info> selectAll() {
        String sql = "select * from " + tableName + ";";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();
        ArrayList<Info> infos = new ArrayList<Info>();

        while (!results.isAfterLast()) {
            Info info = new Info(results.getInt(0), results.getString(1),
                    results.getString(2), results.getInt(3));
            infos.add(info);
            results.moveToNext();
        }
        results.close();
        return infos;
    }
}