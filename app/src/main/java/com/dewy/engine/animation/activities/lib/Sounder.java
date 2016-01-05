package com.dewy.engine.animation.activities.lib;

import android.media.SoundPool;

/**
 * Created by dewyone on 2015-10-30.
 */
public class Sounder {
    private static final String TAG = "Sounder";
    private int priority = 3;       // the lowest priority
    private int playMore = 0;
    private float rate = 0.5f;

    private SoundPool soundPoolPlayer;
    private int soundID;

    private static boolean SoundOn = true;

    public Sounder() {}
    public Sounder(SoundPool soundPoolPlayer, int soundID) {
        this.soundPoolPlayer = soundPoolPlayer;
        this.soundID = soundID;
    }

    public void setSoundPoolPlayer(SoundPool soundPoolPlayer) {
        this.soundPoolPlayer = soundPoolPlayer;
    }

    void play() {

        if (! isSoundOn()) return;

        //String logMessage = "play() is clicked..";
        //Log.i(TAG, logMessage);

        //soundPoolPlayer.stop(boxDragID);
        soundPoolPlayer.play(soundID, 1, 1, priority, playMore, rate);
    }

    public static boolean isSoundOn() {
        return SoundOn;
    }

    public static void setSoundOn(boolean soundOn) {
        SoundOn = soundOn;
    }
}
