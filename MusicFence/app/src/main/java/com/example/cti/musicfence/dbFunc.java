package com.example.cti.musicfence;

import android.content.ContentValues;
import android.content.Context;

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
}
