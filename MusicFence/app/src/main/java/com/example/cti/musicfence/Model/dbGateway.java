package com.example.cti.musicfence.Model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by laboratorio on 30/11/17.
 */

public class dbGateway {

    private static dbGateway gateway;
    private SQLiteDatabase db;

    private dbGateway(Context context){
        dbHelper helper = new dbHelper(context);
        db= helper.getWritableDatabase();
    }

    public static dbGateway getInstance(Context context){
        if(gateway == null)
            gateway = new dbGateway(context);
        return gateway;
    }

    public SQLiteDatabase getDatabase(){
        return this.db;
    }
}
