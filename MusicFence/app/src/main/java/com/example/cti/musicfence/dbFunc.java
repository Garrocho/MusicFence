package com.example.cti.musicfence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by laboratorio on 30/11/17.
 */

public class dbFunc {

    private final String tabela = "geoFence";
    private dbGateway gateway;

    public dbFunc(Context context){
        gateway = dbGateway.getInstance(context);
    }

    public boolean adicionar(double latitude, double longitude, double raio, String musica){
        ContentValues contentValues = new ContentValues();
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        contentValues.put("raio", raio);
        contentValues.put("music", musica);
        return gateway.getDatabase().insert(tabela,null,contentValues) > 0;
    }

    public boolean remover(double latitude, double longitude, double raio, String musica){
        String[] cv = new String[]{""+latitude,""+longitude,""+raio,musica};
        return gateway.getDatabase().delete(tabela,"latitude=? and longitude=? and raio=? and musica=?",cv) > 0;
    }

    public ArrayList<geoFence> listar(){
        Cursor cursor = gateway.getDatabase().rawQuery("SELECT * FROM geoFence", null);
        ArrayList<geoFence> geoFences = new ArrayList<geoFence>();
        while (cursor.moveToNext()){
            geoFence g = new geoFence();
            g.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
            g.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
            g.setRaio(cursor.getDouble(cursor.getColumnIndex("raio")));
            g.setMusica(cursor.getString(cursor.getColumnIndex("music")));
            geoFences.add(g);
        }
        return geoFences;
    }
}
