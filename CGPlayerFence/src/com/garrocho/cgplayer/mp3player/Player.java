package com.garrocho.cgplayer.mp3player;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.garrocho.MainActivity;

public class Player extends Service implements OnCompletionListener {

	private MediaPlayer mediaPlayer;
	//private Playlist playlistManager;
	//private ArrayList<HashMap<String, String>> playlist;;
	private List<Musica> playlist;
	//private String path;
	private int musicIndex;
	public boolean playing;
	public boolean paused;
	private boolean changeMusic;
	
	public Player() { }
	
	public class PlayerBinder extends Binder implements PlayerInterface {
		
		@Override
		public String getMusicName() {
			return playlist.get(musicIndex).getTitulo();
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
					mediaPlayer.setDataSource(playlist.get(musicIndex).getPath());
					mediaPlayer.prepare();
				}
				playing = true;
				paused = false;
				changeMusic = false;
				MainActivity.seekBar.setMax(mediaPlayer.getDuration());
				MainActivity.musicaAtual.setText(playlist.get(musicIndex).getTitulo());
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
			if(musicIndex < playlist.size()-1) {
	    		musicIndex += 1;				
	    	} else {
	    		musicIndex = 0;
	    	}			
			changeMusic = true;
			this.play();
		}

		@Override
		public void previous() {
			if(musicIndex > 0) {
	    		musicIndex -= 1;
	    	} else {
	    		musicIndex = playlist.size() - 1;
	    	}	
			changeMusic = true;
			this.play();
		}
		
		public void playMusic(int index){
			if(index < playlist.size()){
				musicIndex = index;
			}
			
			changeMusic = true;
			this.play();
		}
		
	}
	
	@Override
	public void onCreate() {
		super.onCreate();		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//this.path = intent.getExtras().getString("path");
		this.playlist = intent.getParcelableArrayListExtra("lista_musicas");
		if(this.playlist != null) {
			
			MainActivity.seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				
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
			
			//this.playlistManager = new Playlist(this.path);
			//this.playlist = this.playlistManager.getPlaylist();
			
			this.musicIndex = 0;
			
			this.mediaPlayer = new MediaPlayer();
			this.mediaPlayer.setOnCompletionListener(this);
			
			try {
				this.mediaPlayer.setDataSource(this.playlist.get(musicIndex).getPath());
				this.mediaPlayer.prepare();
				//this.playing = true;
				//this.play();
			} catch(Exception e) {
				e.printStackTrace();
			}
			
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void play() {
		try {
			if(this.changeMusic) {
				this.mediaPlayer.reset();
				this.mediaPlayer.setDataSource(this.playlist.get(this.musicIndex).getPath());
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
	
	@Override
	public IBinder onBind(Intent intent) {
		return new PlayerBinder();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		if(this.musicIndex < this.playlist.size()-1) {
    		this.musicIndex += 1;				
    	} else {
    		this.musicIndex = 0;
    	}			
		this.changeMusic = true;
		this.play();
	}

}
