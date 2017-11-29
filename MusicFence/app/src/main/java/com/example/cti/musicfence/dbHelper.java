package com.example.cti.musicfence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by laboratorio on 29/11/17.
 */

public class dbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "musicFence.db";
    private static final int DATABASE_VERSION = 1;
    private static String CREATE_TABLE = "CREATE TABLE geoFence ("+
            "latitude DOUBLE,"+
            "longitude DOUBLE,"+
            "raio DOUBLE,"+
            "music VARCHAR)";

    public dbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
