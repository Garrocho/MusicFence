package com.example.cti.musicfence.Model;

import android.util.Log;

import com.google.android.gms.location.Geofence;

/**
 * Created by laboratorio on 01/12/17.
 */

public class geoFence implements Geofence {
    public double latitude;
    public double longitude;
    public double raio;
    public String musica;
    public int id;

    public geoFence(int id,double latitude, double longitude, double raio, String musica) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.raio = raio;
        this.musica = musica;
        this.id = id;
    }

    public geoFence() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRaio() {
        return raio;
    }

    public void setRaio(double raio) {
        this.raio = raio;
    }

    public String getMusica() {
        return musica;
    }

    public void setMusica(String musica) {
        this.musica = musica;
    }

    @Override
    public String getRequestId() {
        return String.valueOf(id);
    }

    public void setId(int id) {
        Log.d("id geofence",String.valueOf(this.id));
        this.id = id;
    }
}
