package com.example.cti.musicfence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceReport;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.drakeet.materialdialog.MaterialDialog;

import static com.google.android.gms.common.api.GoogleApiClient.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    dbFunc func;
    String nomeMusica;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        Intent intent = getIntent();
        nomeMusica = intent.getStringExtra("nomeMusica");
        Log.d("Musica", nomeMusica);
    }


    private void readMyCurrentCoordenadas() {
        meuLocationListener locationListener = new meuLocationListener();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location location = null;
        double latitude = 0;
        double longitude = 0;

        if (!isGPSEnable && !isNetworkEnabled) {
            Log.i("Erro", "Necessita de GPS e Internet");
        } else {
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, locationListener);
                Log.d("Internet", "Network Ativo");
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
            if (isGPSEnable) {
                if (location == null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
                        Log.d("GPS", "GPS Ativo");
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        }
        LatLng minhaPos = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(minhaPos).title("Sua Posicao"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(minhaPos, 15));
        Log.i("Posicao", "Lat: " + latitude + "|Long: " + longitude);
    }

    public void callAcessLocation() {
        Log.i("Chamada", "Funcao Ativa");
        readMyCurrentCoordenadas();
    }

    @Override
    protected void onStop() {
        super.onStop();
        pararConexao();
    }

    public void pararConexao() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        callAcessLocation();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                String names[] = {"50","100","200","500"};
                final double[] raio = new double[1];
                final AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this)
                        .create();
                LayoutInflater layoutInflater = getLayoutInflater();
                View convertView = (View) layoutInflater.inflate(R.layout.custom,null);
                alertDialog.setView(convertView);
                alertDialog.setTitle("Selecione o Raio");
                ListView listView = (ListView) convertView.findViewById(R.id.listView1);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this,android.R.layout.simple_list_item_1,names);
                listView.setAdapter(adapter);
                alertDialog.show();
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String item = ((TextView)view).getText().toString();
                        addMarker(latLng,item);
                        raio[0] = Double.parseDouble(item);
                        alertDialog.dismiss();
                        Log.d("Latitude do Click", String.valueOf(latLng.latitude));
                        Log.d("Longitude do Click", String.valueOf(latLng.longitude));
                        Log.d("Raio ", String.valueOf(raio[0]));
                        Log.d("Musica nome", nomeMusica);
                        /*if(func.adicionar(latLng.latitude,latLng.longitude,raio[0],nomeMusica) == true){
                            Toast.makeText(MapsActivity.this, "Geofence adicionada com sucesso.", Toast.LENGTH_SHORT).show();
                        }
                        */
                    }
                });


            }
        });

        //LatLng suaPosicao = new LatLng(latitude, longitude);
        //mMap.addMarker(new MarkerOptions().position(suaPosicao).title("Sua posicao"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(suaPosicao));

    }

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

    private final class meuLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        pararConexao();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void addMarker(LatLng point, String item){
        if(point != null){
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(point.latitude, point.longitude))
                    .radius(Float.valueOf(item))
                    .fillColor(0x40ff0000)
                    .strokeColor(Color.TRANSPARENT)
                    .strokeWidth(2);
            Circle circle = mMap.addCircle(circleOptions);
        }
    }
}