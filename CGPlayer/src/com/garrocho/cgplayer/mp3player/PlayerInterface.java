package com.garrocho.cgplayer.mp3player;

public interface PlayerInterface {
	//public String getPath();
	public String getMusicName();
	public int getDuration();
	public int getCurrentPosition();
	public void play();
	public void pause();
	public void stop();
	public void next();
	public void previous();
	public void playMusic(int index);
}
