package com.garrocho.cgplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.garrocho.cgplayer.mp3player.Musica;
import com.garrocho.cgplayer.mp3player.Player.PlayerBinder;
import com.garrocho.cgplayer.video.HomeArrayAdapter;
import com.garrocho.cgplayer.video.Video;

public class MainActivity extends Activity implements ServiceConnection {

	private TabHost tabHost;
	public static SeekBar seekBar;
	private ServiceConnection conexao;
	private PlayerBinder binder;
	private ListView listViewMusicas;
	private List<Musica> listaMusicas;
	public static TextView musicaAtual;
	private Context context;
	private Map<Integer, Video> mapaVideos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mapaVideos = new HashMap<Integer, Video>();

		setupTabHost();
		tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);

		setupTab(new TextView(this), "Musicas", R.id.aba_musicas, R.drawable.musica);
		setupTab(new TextView(this), "Videos", R.id.aba_videos, R.drawable.video);

		seekBar = (SeekBar) findViewById(R.id.music_progress);
		this.listViewMusicas = (ListView)findViewById(R.id.lista_musicas);

		listaMusicas = getAllMusics();
		startService(new Intent("com.garrocho.cgplayer.SERVICE_PLAYER").putParcelableArrayListExtra("lista_musicas", (ArrayList<Musica>)listaMusicas));

		ArrayAdapter<Musica> adapter = new ArrayAdapter<Musica>(this, R.layout.lista_titulo_sumario_texto, listaMusicas);
		listViewMusicas.setAdapter(adapter);
		listViewMusicas.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				binder.playMusic(position);	
			}
		});

		this.conexao = this;
		if(binder == null || !binder.isBinderAlive()){
			Intent intentPlayer = new Intent("com.garrocho.cgplayer.SERVICE_PLAYER");
			bindService(intentPlayer, this.conexao, Context.BIND_AUTO_CREATE);
		}
		
		musicaAtual = (TextView)findViewById(R.id.textView2);

		//video
		context = getBaseContext();

		// Grab references of all required widgets
		final ListView homeListView = (ListView) findViewById(R.id.activityhome_listview);

		getVideos();

		// Put all the titles in an arraylist for the adapter
		final ArrayList<String> list = new ArrayList<String>();

		for (int i=0; i < mapaVideos.size(); i++) {
			Video video = mapaVideos.get(i);
			list.add(video.getTitle());
		}

		// Inflate the listview with the example activities
		homeListView.setAdapter(new HomeArrayAdapter(getContext(), R.layout.lista_titulo_sumario_texto, list));
		homeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = null;
				
				try {
					if (binder.isPlay())
						binder.pause();
				} catch (Exception e) {
				}
				
				intent = new Intent("com.garrocho.cgplayer.video.VIDEO_PLAYER");
				// Launch the activity with some extras
				intent.putExtra("layout", "0");
				intent.putExtra(Video.class.getName(), mapaVideos.get(position));
				startActivityForResult(intent, 0);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		try {
			if (binder.isPaused())
				binder.play();
		} catch (Exception e) {
		}
	}
	
	public void addAnimation(View view) {
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
		fadeIn.setDuration(400);

		AnimationSet animation = new AnimationSet(false); //change to false
		animation.addAnimation(fadeIn);
		
		view.setAnimation(animation);
		view.startAnimation(animation);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent intentPlayer = new Intent("com.garrocho.cgplayer.SERVICE_PLAYER");
		stopService(intentPlayer);
	}

	public void playMusic(View view) {
		this.binder.play();
		addAnimation(view);
	}

	public void pauseMusic(View view) {
		this.binder.pause();
		addAnimation(view);
	}

	public void stopMusic(View view) {
		this.binder.stop();
		addAnimation(view);
	}

	public void nextMusic(View view) {
		this.binder.next();
		addAnimation(view);
	}

	public void previousMusic(View view) {
		this.binder.previous();
		addAnimation(view);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.binder = (PlayerBinder) service;
		//this.musicas.setText(binder.getPath());
		try {
			MainActivity.seekBar.setMax(this.binder.getDuration());
			MainActivity.seekBar.setProgress(this.binder.getCurrentPosition());
		} catch(Exception e) {
			return;
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		this.binder = null;
	}

	public List<Musica> getAllMusics(){
		//Some audio may be explicitly marked as not being music
		String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

		String[] projection = {
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DURATION
		};

		Cursor cursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				projection,
				selection,
				null,
				null);

		List<Musica> songs = new ArrayList<Musica>();
		if (cursor != null)
			while(cursor.moveToNext()){
				Musica musica = new Musica(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5));
				songs.add(musica);
			}
		return songs;
	}

	protected Context getContext() {
		return context;
	}

	void getVideos() {
		String[] cols = new String[] {
				MediaStore.Video.Media._ID,
				MediaStore.Video.Media.TITLE,
				MediaStore.Video.Media.DATA,
				MediaStore.Video.Media.MIME_TYPE,
				MediaStore.Video.Media.ARTIST
		};
		ContentResolver resolver = getContentResolver();
		if (resolver == null) {
			System.out.println("resolver = null");
		} else {
			String mSortOrder = MediaStore.Video.Media.TITLE + " COLLATE UNICODE";
			String mWhereClause = MediaStore.Video.Media.TITLE + " != ''";
			Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cols, mWhereClause , null, mSortOrder);
			int i =0;
			if (cursor != null)
				while(cursor.moveToNext()) {
					Video video = new Video(cursor.getString(2));
					video.setTitle(cursor.getString(1));
					video.setAuthor(cursor.getString(4));
					mapaVideos.put(i++, video);
				}
		}
	}

	private void setupTab(final View view, final String tag, int id, int img) {
		View tabview = createTabView(tabHost.getContext(), tag, img);

		TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview).setContent(id);
		tabHost.addTab(setContent);

	}

	private static View createTabView(final Context context, final String text, int img) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		ImageView im = (ImageView) view.findViewById(R.id.tabsImage);
		im.setImageResource(img);
		tv.setText(text);
		return view;
	}

	private void setupTabHost() {
		tabHost = (TabHost)findViewById(R.id.player_tabhost);
		tabHost.setup();
	}

}
