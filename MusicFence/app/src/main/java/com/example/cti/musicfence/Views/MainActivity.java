package com.example.cti.musicfence.Views;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.cti.musicfence.Service.GeoFenceTransitionsIntentService;
import com.example.cti.musicfence.Model.Musica;
import com.example.cti.musicfence.R;
import com.example.cti.musicfence.Util.calculaDistancia;
import com.example.cti.musicfence.Util.dbFunc;
import com.example.cti.musicfence.Model.geoFence;
import com.example.cti.musicfence.Model.mp3player;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ServiceConnection, ResultCallback<Status>,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static SeekBar seekBar;
    public static Intent makeNotificationIntent;
    private ServiceConnection conexao;
    private static mp3player.PlayerBinder binder;
    private ListView listaViewMusicas;
    public static ArrayList<Musica> listaMusic;
    public static TextView musicaAtual;
    private static Context context;
    private static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;
    private long duracaoGeofence = 60*60+1000;
    private GeofencingClient geofencingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);


        seekBar = (SeekBar) findViewById(R.id.music_progress);
        this.listaViewMusicas = (ListView)findViewById(R.id.lista_musicas);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE));
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                   MY_PERMISSIONS_READ_EXTERNAL_STORAGE );
        }

        musicaAtual = (TextView)findViewById(R.id.textView2);
        geofencingClient = LocationServices.getGeofencingClient(this);
        Intent intentGeofence = new Intent(".GeoFenceTransitionsIntentService");
        intentGeofence.setPackage("com.example.cti.");
        startService(intentGeofence);
        dbFunc dbFunc = new dbFunc(this);
        for (geoFence g : dbFunc.listar()) {
            Geofence g2 = createGeofence(g);
            GeofencingRequest geofencingRequest = geofencingRequest(g2);
            geofencingClient.addGeofences(geofencingRequest,CriargeoPendingIntent()).addOnSuccessListener(this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Status", "sucesso.");
                }
            });
        }
    }

    public static void entradaGeofence(LatLng latLng){
        Log.d("Id Geofence", "entrou em uma geofence");
        dbFunc dbFunc = new dbFunc(context);
        for (geoFence geo : dbFunc.listar()) {
            Log.d(geo.getMusica(), geo.getLatitude() + " - " + geo.getLongitude());
            if (calculaDistancia.distance(latLng.latitude,geo.getLatitude(),latLng.longitude,geo.longitude,1,1) < geo.getRaio()) {
                String nomeMusica = geo.getMusica();
                Log.i("Musica Geo", nomeMusica);
                int index = 0;
                for (Musica m : listaMusic) {
                  if (m.getTitulo().contains(nomeMusica)) {
                      Log.d("teste", "Play music");
                      binder.playMusic(index);
                      binder.play();
                  }
                  index++;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_READ_EXTERNAL_STORAGE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    configurarLista();

                }else{

                }return;
            }
        }
    }

    private Geofence createGeofence(geoFence g) {
        Log.d("Criar geofence", "Criada.");
        return new Geofence.Builder()
                .setRequestId("0")
                .setCircularRegion(g.getLatitude(), g.getLongitude(), (float) g.getRaio())
                .setExpirationDuration(duracaoGeofence)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
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
    private GeofencingRequest geofencingRequest(Geofence geofence) {
        Log.d("GeoRequest ", "Request");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    public void configurarLista(){
        listaMusic = getAllMusic();

        Intent intentService = new Intent("com.example.cti.musicfence.SERVICE_PLAYER_2").putParcelableArrayListExtra("listaMusicas",
                (ArrayList<Musica>)listaMusic);
        intentService.setPackage("com.example.cti.");
        startService(intentService);

        ArrayAdapter<Musica> adapter = new ArrayAdapter<Musica>(this,R.layout.lista_titulo_sumario_texto, listaMusic);
        listaViewMusicas.setAdapter(adapter);

        listaViewMusicas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                binder.playMusic(position);
            }
        });
        listaViewMusicas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("nomeMusica", ((TextView)view).getText().toString());
                startActivity(intent);
                return false;
            }
        });

        this.conexao = this;
        if(binder == null || !binder.isBinderAlive()){
            Intent intentPlayer = new Intent(this,mp3player.class);
            bindService(intentPlayer, this.conexao, Context.BIND_AUTO_CREATE);
            startService(intentPlayer);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intentService = new Intent("com.example.cti.musicfence.SERVICE_PLAYER_2");
        intentService.setPackage("com.example.cti.");
        stopService(intentService);
        Intent intentGeofence = new Intent(".GeoFenceTransitionsIntentService");
        intentGeofence.setPackage("com.example.cti.");
        stopService(intentGeofence);
    }

    public void playMusic(View view) {
        this.binder.play();
    }

    public void pauseMusic(View view) {
        this.binder.pause();
    }

    public void stopMusic(View view) {
        this.binder.stop();
    }

    public void nextMusic(View view) {
        this.binder.next();
    }

    public void previousMusic(View view) {
        this.binder.previous();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.binder = (mp3player.PlayerBinder) service;
        //this.musicas.setText(binder.getPath());
        mp3player.playlist = listaMusic;
        try {
            MainActivity.seekBar.setMax(this.binder.getDuration());
            MainActivity.seekBar.setProgress(this.binder.getCurrentPosition());
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        this.binder = null;
    }

    public ArrayList<Musica> getAllMusic(){
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection,selection,
                null,
                null);
        ArrayList<Musica> songs = new ArrayList<Musica>();
        if(cursor != null)
            while (cursor.moveToNext()){
                Musica musica = new Musica(cursor.getInt(0),cursor.getString(1),cursor.getString(2),
                        cursor.getString(3),cursor.getString(4),cursor.getInt(5));
                songs.add(musica);
            }
        return songs;
    }

    public static Intent makeNotificationIntent(Context applicationContext, String msg) {
        return makeNotificationIntent;
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess())
            Log.e("Tag", "O sistema esta monitorando");
        else
            Log.e("Tag", "o SISTEMA NAO ESTA MONITORANDO");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
