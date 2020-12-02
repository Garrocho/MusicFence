package com.example.cti.musicfence.Interface;

/**
 * Created by Cti on 13/11/2017.
 */

public interface PlayerInterface {
    public String getMusicName();
    public int getDuration();
    public int getCurrentPosition();
    public void play();
    public void pause();
    public void stop();
    public void next();
    public void previous();
    public void playMusic(int Index);
}
