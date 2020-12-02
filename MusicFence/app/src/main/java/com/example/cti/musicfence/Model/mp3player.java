package com.example.cti.musicfence.Model;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.SeekBar;

import com.example.cti.musicfence.Interface.PlayerInterface;
import com.example.cti.musicfence.Views.MainActivity;

import java.util.ArrayList;

public class mp3player extends Service implements MediaPlayer.OnCompletionListener{
    private MediaPlayer mediaPlayer;
    public static ArrayList<Musica> playlist;
    private int MusicIndex;
    public boolean playing;
    public boolean paused;
    private boolean changeMusic;

    public class PlayerBinder extends Binder implements PlayerInterface {

        @Override
        public String getMusicName() {
            return playlist.get(MusicIndex).getTitulo();
        }

        public boolean isPlay() {
            return playing;
        }

        public boolean isPaused() {
            return paused;
        }

        @Override
        public int getDuration() {
            return mediaPlayer.getDuration();
        }

        @Override
        public int getCurrentPosition() {
            return mediaPlayer.getCurrentPosition();
        }

        @Override
        public void play() {
            try {
                if(!playing || changeMusic) {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(playlist.get(MusicIndex).getPath());
                    mediaPlayer.prepare();
                }
                playing = true;
                paused = false;
                changeMusic = false;
                MainActivity.seekBar.setMax(mediaPlayer.getDuration());
                MainActivity.musicaAtual.setText(playlist.get(MusicIndex).getTitulo());
                if(mediaPlayer != null) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            while(mediaPlayer.isPlaying()) {
                                MainActivity.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            }
                        }
                    }).start();
                }
                mediaPlayer.start();
            } catch(Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void pause() {
            mediaPlayer.pause();
            paused = true;
        }

        @Override
        public void stop() {
            mediaPlayer.stop();
            MainActivity.seekBar.setProgress(0);
            playing = false;
        }

        @Override
        public void next() {
            if(MusicIndex < playlist.size()-1){
                MusicIndex += 1;
            }else{
                MusicIndex = 0;
            }
            changeMusic = true;
            this.play();
        }

        @Override
        public void previous() {
            if(MusicIndex > 0) {
                MusicIndex -= 1;
            } else {
                MusicIndex = playlist.size() - 1;
            }
            changeMusic = true;
            this.play();
        }

        @Override
        public void playMusic(int Index) {
            if(Index < playlist.size()){
                MusicIndex = Index;
            }
            changeMusic = true;
            this.play();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.playlist = intent.getParcelableArrayListExtra("listaMusicas");
        if (this.playlist != null) {

            MainActivity.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.seekTo(MainActivity.seekBar.getProgress());
                    mediaPlayer.start();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    // TODO Auto-generated method stub

                }
            });


            this.MusicIndex = 0;

            this.mediaPlayer = new MediaPlayer();
            this.mediaPlayer.setOnCompletionListener(this);

            try {
                this.mediaPlayer.setDataSource(this.playlist.get(MusicIndex).getPath());
                this.mediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Log.d("error musicfence", "playslist null");
        }

        return super.onStartCommand(intent ,flags,startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerBinder();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(this.MusicIndex < this.playlist.size()-1){
            this.MusicIndex += 1;
        }else{
            this.MusicIndex = 0;
        }
        this.changeMusic = true;
        this.play();
    }

    public void play() {
        try {
            if(this.changeMusic) {
                this.mediaPlayer.reset();
                this.mediaPlayer.setDataSource(this.playlist.get(this.MusicIndex).getPath());
                this.mediaPlayer.prepare();
            }
            MainActivity.seekBar.setMax(mediaPlayer.getDuration());
            if(this.mediaPlayer != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while(mediaPlayer.isPlaying()) {
                            MainActivity.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        }
                    }
                }).start();
                this.mediaPlayer.start();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
