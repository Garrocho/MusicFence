package com.garrocho.cgplayer.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.garrocho.R;

public class VideoPlayerActivity extends Activity {

	/** Reference to the instance for use with the dispatchKeyEvent override method. */
	private Activity activity = this;

	/** The current activities configuration used to test screen orientation. */
	private Configuration configuration;

	/** The activities intent. */
	private Intent intent;
	private Thread updateThread;
	private VideoView videoView;
	private MediaController controller;
	private Video video;
	private int layout = -1;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		configuration = getResources().getConfiguration();
		intent = getIntent();

		// Loads in extras passed with the activity intent
		layout = Integer.parseInt(intent.getStringExtra("layout"));
		video = (Video) intent.getSerializableExtra(Video.class.getName());

		// Sets the content view based on the layout
		setContentView(R.layout.activity_simple_videoplayer);

		// Load in references
		videoView = (VideoView) findViewById(R.id.fragmentvideoplayer_videoview);

		// Show the back button on the actionbar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Set the title on the actionbar to the video title
		setTitle(video.getTitle());

		// Create a custom media controller that ignores the back button
		controller = new MediaController(this) {
			@Override
			public boolean dispatchKeyEvent(KeyEvent event) {
				if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
					((Activity) activity).finish();

				return super.dispatchKeyEvent(event);
			}
		};

		// Attach the media controller
		videoView.setVideoURI(Uri.parse(video.getUrl()));
		videoView.setMediaController(controller);

		int videoPosition = 0;
		if (savedInstanceState != null)
			videoPosition = savedInstanceState.getInt("videoPosition");

		videoView.seekTo(videoPosition);
		videoView.start();

		updateLayout();
		startUpdateThread();
	}

	@Override
	protected void onResume() {	
		startUpdateThread();
		super.onResume();
	}

	@Override
	protected void onPause() {
		if(updateThread != null)
			updateThread.interrupt();

		super.onPause();
	}

	@Override
	public void finish() {
		if(updateThread != null)
			updateThread.interrupt();

		super.finish();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (videoView.isPlaying())
			outState.putInt("videoPosition", videoView.getCurrentPosition());
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		updateLayout();
	}

	@SuppressLint({ "InlinedApi", "NewApi" })
	private void updateLayout() {
		// Hide the status bar
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setAttributes(attrs);

		// Hide the software buttons
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			View main_layout = findViewById(android.R.id.content).getRootView();
			main_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}

		// Hide the media controller
		controller.hide();

		// Hide the actionbar
		getActionBar().hide();
	}

	@SuppressLint("InlinedApi")
	private void startUpdateThread() {
		if(updateThread == null || updateThread.isInterrupted()) {
			updateThread = new Thread(new Runnable() {
				private View main_layout = findViewById(android.R.id.content).getRootView();
				private Handler mHandler = new Handler();
				private boolean canShow = true;

				@SuppressLint("NewApi")
				@Override
				public void run() {
					mHandler.postDelayed(this, 100);

					if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
						int currentUi = main_layout.getSystemUiVisibility();

						if(currentUi == 0 && controller != null && configuration != null && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
							try {
								if(!controller.isShowing() && canShow) {		
									WindowManager.LayoutParams attrs = getWindow().getAttributes();
									attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
									getWindow().setAttributes(attrs);

									controller.show();
									canShow = false;
								}
							} catch(WindowManager.BadTokenException ex) {
								// WindowManager$BadTokenException will be caught and the app would not display 
								// the 'Force Close' message
							} finally {
								if(!controller.isShowing()) {
									WindowManager.LayoutParams attrs = getWindow().getAttributes();
									attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
									getWindow().setAttributes(attrs);

									main_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
									canShow = true;
								}
							}
						} else if(currentUi == 0 && controller != null && configuration != null && configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
							try {
								if(!controller.isShowing() && canShow) {		
									WindowManager.LayoutParams attrs = getWindow().getAttributes();
									attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
									getWindow().setAttributes(attrs);

									controller.show();
									canShow = false;
								}
							} catch(WindowManager.BadTokenException ex) {
								// WindowManager$BadTokenException will be caught and the app would not display 
								// the 'Force Close' message
							} finally {
								if(!controller.isShowing()) {
									WindowManager.LayoutParams attrs = getWindow().getAttributes();
									attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
									getWindow().setAttributes(attrs);

									main_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
									canShow = true;
								}
							}
						}
					}
				}
			});

			updateThread.start();
		}
	}

	/**
	 * Converts milliseconds given as an integer into a string formatted hh:mm:ss.
	 * @param ms Milliseconds to convert to formatted string hh:mm:ss.
	 * @return Formatted string.
	 */
	private String getTime(int ms) {
		// Convert the milliseconds to seconds, minutes, and hours
		int seconds = (int) (ms / 1000) % 60 ;
		int minutes = (int) ((ms / (1000*60)) % 60);
		int hours = (int) ((ms / (1000*60*60)) % 24);

		// Convert the values to strings
		StringBuilder fMinutes = new StringBuilder(String.valueOf(minutes));
		StringBuilder fSeconds = new StringBuilder(String.valueOf(seconds));
		StringBuilder fHours = new StringBuilder(String.valueOf(hours));

		// Insert a 0 in front of the values if they are single digit
		if(fSeconds.length() == 1)
			fSeconds.insert(0, "0");
		if(fMinutes.length() == 1)
			fMinutes.insert(0, "0");
		if(fHours.length() == 1)
			fHours.insert(0, "0");

		// Decide to display hours if it is over 0
		if(hours <= 0)
			return fMinutes + ":" + fSeconds;
		else
			return fHours + ":" + fMinutes + ":" + fSeconds;
	}
}
