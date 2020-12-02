package com.example.cti.musicfence.Views;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cti.musicfence.Service.GeoFenceTransitionsIntentService;
import com.example.cti.musicfence.R;
import com.example.cti.musicfence.Util.dbFunc;
import com.example.cti.musicfence.Model.geoFence;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.google.android.gms.common.api.GoogleApiClient.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status> {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    dbFunc func;
    String nomeMusica;
    private Button button;
    private GeofencingClient geofencingClient;
    private long duracaoGeofence = 60*60+1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapView mapView = findViewById(R.id.mapView);
        mapView.onResume();
        mapView.getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        Intent intent = getIntent();
        nomeMusica = intent.getStringExtra("nomeMusica");
        Log.d("Musica", nomeMusica);
        func = new dbFunc(this);
        button = (Button) findViewById(R.id.bDeleteFence);
        geofencingClient = LocationServices.getGeofencingClient(this);
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
        mMap.addMarker(new MarkerOptions().position(minhaPos).title("Sua Posicao")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
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

    public void desenhaGeoFence() {
        for (geoFence g : func.listar()) {
            LatLng latLng = new LatLng(g.getLatitude(), g.getLongitude());
            String item = String.valueOf(g.getRaio());
            String music = g.getMusica();
            addMarker(latLng, item, music);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        callAcessLocation();
        desenhaGeoFence();
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                String names[] = {"50", "100", "200", "500"};
                final double[] raio = new double[1];
                final AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this)
                        .create();
                LayoutInflater layoutInflater = getLayoutInflater();
                View convertView = (View) layoutInflater.inflate(R.layout.custom, null);
                alertDialog.setView(convertView);
                alertDialog.setTitle("Selecione o Raio");
                ListView listView = (ListView) convertView.findViewById(R.id.listView1);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this, android.R.layout.simple_list_item_1, names);
                listView.setAdapter(adapter);
                alertDialog.show();
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String item = ((TextView) view).getText().toString();
                        addMarker(latLng, item, nomeMusica);
                        raio[0] = Double.parseDouble(item);
                        alertDialog.dismiss();
                        Log.d("Latitude do Click", String.valueOf(latLng.latitude));
                        Log.d("Longitude do Click", String.valueOf(latLng.longitude));
                        Log.d("Raio ", String.valueOf(raio[0]));
                        Log.d("Musica nome", nomeMusica);
                        if (func.adicionar(latLng.latitude, latLng.longitude, raio[0], nomeMusica) == true) {
                            geoFence g = new geoFence();
                            Geofence geofence = createGeofence(latLng, raio[0]);
                            GeofencingRequest geofencingRequest = geofencingRequest(geofence);
                            addGeo(geofencingRequest);
                            Toast.makeText(MapsActivity.this, "Geofence adicionada com sucesso.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                final LatLng latLng1 = marker.getPosition();
                button.setVisibility(View.VISIBLE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteFence(latLng1);
                        marker.remove();
                        button.setVisibility(View.INVISIBLE);
                    }
                });
                return false;
            }
        });
        //LatLng suaPosicao = new LatLng(latitude, longitude);
        //mMap.addMarker(new MarkerOptions().position(suaPosicao).title("Sua posicao"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(suaPosicao));

    }

    public void deleteFence(LatLng latLng) {
        func.remover(latLng.latitude, latLng.longitude);
        Toast.makeText(this, "GeoFence removida com sucesso.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i("Resultado", ""+status);
        if(status.isSuccess()){
            Log.d("Criacao", "bem sucedida.");
        }
    }

    // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

    private final class meuLocationListener implements LocationListener {
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

    public void addMarker(LatLng point, String item, String musica) {
        if (point != null) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(point.latitude, point.longitude))
                    .title("Geofence " + musica)
                    .snippet("Raio " + item));


            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(point.latitude, point.longitude))
                    .radius(Float.valueOf(item))
                    .fillColor(0x40ff0000)
                    .strokeColor(Color.TRANSPARENT)
                    .strokeWidth(2);
            Circle circle = mMap.addCircle(circleOptions);
        }
    }

    private void addGeo(GeofencingRequest request){
        Log.d("Geo Add: ", "Adicionada.");
        LocationServices.GeofencingApi.addGeofences(googleApiClient,request,CriargeoPendingIntent()
        ).setResultCallback(this);
    }

    private Geofence createGeofence(LatLng latLng, double radius) {
        geoFence g = new geoFence();
        Log.d("Criar geofence", "Criada.");
        return new Geofence.Builder()
                .setRequestId(g.getRequestId())
                .setCircularRegion(latLng.latitude, latLng.longitude, (float) radius)
                .setExpirationDuration(duracaoGeofence)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private GeofencingRequest geofencingRequest(Geofence geofence) {
        Log.d("GeoRequest ", "Request");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private PendingIntent geoPendingIntent;
    private PendingIntent CriargeoPendingIntent() {
        Log.d("Criar Pending Intent", "Criado.");
        if (geoPendingIntent != null)
            return geoPendingIntent;

        Intent intent = new Intent(this, GeoFenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}