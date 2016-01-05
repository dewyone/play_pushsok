package com.dewy.engine.animation;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dewy.engine.platform.ActivityLifeCycle;
import com.dewy.engine.platform.GLContext;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dewyone on 2015-09-02.
 * Role :
 *      platform for activities..
 *      platform as surface view ( manager)
 *
 *      for now, main activity (launcher) is LogoScreen class
 */
public class SokobanMain implements GLSurfaceView.Renderer, View.OnTouchListener, ActivityLifeCycle {
    private static final String TAG = "SokobanMain";
    private static final boolean DEBUG_LIFECYCLE = true;

    private final GLContext GLContext;      // Activity( Context) + GLSurfaceView + TouchEventListener
    private final ActivityController activityController;

    public SokobanMain(GLContext GLContext) {
        String logMessage = "instantiated...";
        Log.i(TAG, logMessage);
        this.GLContext = GLContext;

        GLContext.addRenderer(this);
        GLContext.addActivityLifeCycleListener(this);
        GLContext.addOnTouchListener(this);

        activityController = new ActivityController(GLContext);
    }

    /*                    Activity Life Cycle                       */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onCreate() is called...";
            Log.i(TAG, logMessage);
        }

        activityController.onCreate(savedInstanceState);
    }

    @Override
    public void onRestart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestart() is called...";
            Log.i(TAG, logMessage);
        }

        activityController.onRestart();
    }

    @Override
    public void onStart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStart() is called...";
            Log.i(TAG, logMessage);
        }

        activityController.onStart();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestoreInstanceState() is called...";
            Log.i(TAG, logMessage);
        }

        activityController.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onResume() is called...";
            Log.i(TAG, logMessage);
        }

        activityController.onResume();
    }

    @Override
    public void onPause() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onPause() is called...";
            Log.i(TAG, logMessage);
        }

        activityController.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSaveInstanceState() is called...";
            Log.i(TAG, logMessage);
        }

        activityController.onSaveInstanceState(outState);
    }

    public void onStop() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStop() is called...";
            Log.i(TAG, logMessage);
        }

        activityController.onStop();
    }

    @Override
    public void onDestroy() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onDestory() is called...";
            Log.i(TAG, logMessage);
        }

        activityController.onDestroy();
    }



    /*                  Renderer Life Cycle                 */

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSurfaceCreated() is called...";
            Log.i(TAG, logMessage);
        }

        activityController.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSurfaceChanged() is called...";
            Log.i(TAG, logMessage);
        }

        activityController.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        activityController.onDrawFrame(gl);
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return activityController.onTouch(v, event);
    }
}