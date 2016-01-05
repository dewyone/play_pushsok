package com.dewy.engine.animation.media;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;

/**
 * Created by dewyone on 2015-10-30.
 */
public class SoundPlayer implements Sound {
    private static final String TAG = "SoundPlayer";

    private static boolean SoundOn = true;

    public static final int BOX_DRAG = 1;
    public static final int BOX_DRAG_ONGOAL = 2;
    public static final int BUTTON_CLICK = 3;

    private int boxDragID;
    private int boxDragOnGoalID;
    private int buttonClickID;

    private SoundPool soundPool;

    public SoundPlayer(Context context) {
        soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);

        try {

            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor = assetManager
                    .openFd("sound/chair_stool_wooden_dragged_12_44100.ogg");
            boxDragID = soundPool.load(descriptor, 1);

            descriptor = assetManager
                    .openFd("sound/steel_chain_being_dropped_and_dragged_on_a_cardboard_box.wav");
            boxDragOnGoalID = soundPool.load(descriptor, 1);

            descriptor = assetManager
                    .openFd("sound/junggle_waterdrop24_short.ogg");
            buttonClickID = soundPool.load(descriptor, 1);

        } catch (IOException e) {
            String logMessage = "IOException while loading sound";
            Log.i(TAG, logMessage);
        }
    }

    @Override
    public void play(int soundID) {

        String logMessage = "In play, isSoundOn : " + isSoundOn();
        Log.i(TAG, logMessage);
        if (! isSoundOn()) return;

        int priority = 0;       // the lowest priority
        int playMore = 0;
        float rate = 1.0f;

        switch (soundID) {
            case BOX_DRAG :
                priority = 5; playMore = 0; rate = 0.5f;
                //soundPool.stop(boxDragID);
                soundPool.play(boxDragID, 1, 1, priority, playMore, rate);
                break;
            case BOX_DRAG_ONGOAL :
                priority = 5; playMore = 0; rate = 0.5f;
                //soundPool.stop(boxDragID);
                soundPool.play(boxDragOnGoalID, 1, 1, priority, playMore, rate);
                break;
            case BUTTON_CLICK :
                priority = 3; playMore = 0; rate = 0.5f;
                //soundPool.stop(boxDragID);
                soundPool.play(buttonClickID, 1, 1, priority, playMore, rate);
                break;
        }
    }

    public int getBoxDragID() {
        return boxDragID;
    }

    public int getBoxDragOnGoalID() {
        return boxDragOnGoalID;
    }

    public int getButtonClickID() {
        return buttonClickID;
    }

    public SoundPool getSoundPool() {
        return soundPool;
    }

    public static boolean isSoundOn() {
        return SoundOn;
    }

    public static void setSoundOn(boolean soundOn) {
        SoundOn = soundOn;

        String logMessage = "SoundOn is " + isSoundOn();
        Log.i(TAG, logMessage);
    }
}
