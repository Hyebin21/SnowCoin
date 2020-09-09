package com.snowcoin.snowcoin;

/**
 * Created by sookmyung on 2018-02-05.
 */

public class Info {
    int Info_id;
    String name;
    String ipAddress;
    int coin;


    public Info(){

    }

    public Info(int id, String name, String ipAddress, int coin){
        this.Info_id = id;
        this.name = name;
        this.ipAddress = ipAddress;
        this.coin = coin;
    }
}