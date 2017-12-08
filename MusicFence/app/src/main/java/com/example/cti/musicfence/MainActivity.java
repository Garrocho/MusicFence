package com.example.cti.musicfence;

import android.Manifest;
import android.app.ActionBar;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    public static SeekBar seekBar;
    public static Intent makeNotificationIntent;
    private ServiceConnection conexao;
    private mp3player.PlayerBinder binder;
    private ListView listaViewMusicas;
    private ArrayList<Musica> listaMusic;
    public static TextView musicaAtual;
    private Context context;
    private static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;


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
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
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
}
