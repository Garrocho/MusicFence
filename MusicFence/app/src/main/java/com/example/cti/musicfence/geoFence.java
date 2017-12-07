package com.example.cti.musicfence;

import com.google.android.gms.location.Geofence;

/**
 * Created by laboratorio on 01/12/17.
 */

public class geoFence{
    public double latitude;
    public double longitude;
    public double raio;
    public String musica;

    public geoFence(double latitude, double longitude, double raio, String musica) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.raio = raio;
        this.musica = musica;
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

}
