package com.dewy.engine.animation.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.game_map.MapManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dewyone on 2015-08-30.
 */
public class StageSelector extends GLActivity {
    private static final String TAG = "StageSelector";
    private static final boolean DEBUG_LIFECYCLE = true;

    private final GLActivity platform;
    private final GLContext glContext;

    private StageSelectManager stageSelectManager;

    private float screenWidth;
    private float screenHeight;

    public static final int MAXSTAGE = MapManager.MAX_STAGE_NUMBER;

    public StageSelector(GLActivity platform) {
        String logMessage = "instanciated...";
        Log.i(TAG, logMessage);
        this.platform = platform;
        this.glContext = platform.glContext;

        stageSelectManager = new StageSelectManager(glContext, platform);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    }

    @Override
    public void onStart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStart() is called...";
            Log.i(TAG, logMessage);
        }
        super.onStart();
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {}

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSurfaceChanged() is called...";
            Log.i(TAG, logMessage);
        }
        screenWidth = (float) width;
        screenHeight = (float) height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        stageSelectManager.draw();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        stageSelectManager.onTouchEvent(event);
        return true;
    }

    public void onTouchEvent(MotionEvent motionEvent) {
    }
}