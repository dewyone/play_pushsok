package com.dewy.engine;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dewy.engine.anim_tester.renderer.TextManagerTest;
import com.dewy.engine.animation.SokobanMain;
import com.dewy.engine.platform.ActivityLifeCycle;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.text.TextManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;

/**
 * Created by dewyone on 2015-07-28.
 */
public class MainRenderer extends GLContext
        implements GLSurfaceView.Renderer, View.OnTouchListener, ActivityLifeCycle {


    private static final String TAG = "MainRenderrer";
    private static final boolean DEBUG_LIFECYCLE = true;

    //private Camera camera;
    private float [] viewProjectionMatrix;

    private SokobanMain sokobanMain;
    private TextManagerTest textManagerTest;

    public MainRenderer(Activity activityContext) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "instanciated...";
            Log.i(TAG, logMessage);
        }
        setActivityContext(activityContext);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onCreate() is called...";
            Log.i(TAG, logMessage);
        }

        // before pass this instance on to any other instances, we need to set up this instance(mainly GLContext)
        sokobanMain = new SokobanMain(this);
        sokobanMain.onCreate(savedInstanceState);


        //textManagerTest = new TextManagerTest(this);
        //textManagerTest.onCreate(savedInstanceState);
    }


    /*                       Renderer Life                      */
    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSurfaceCreated() is called...";
            Log.i(TAG, logMessage);
        }
        setEglConfig(config);

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        /*    ***   Viewer   ***   */
        //camera = new Camera(this);
        //camera.onCreate();

        //viewProjectionMatrix = camera.getCameraController().getViewProjectionMatrix();

        //   *** How to Render   ***
        //glEnable(GLES20.GL_CULL_FACE);

        for (GLSurfaceView.Renderer renderer : rendererList) {
            renderer.onSurfaceCreated(glUnused, config);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSurfaceChanged() is called...";
            Log.i(TAG, logMessage);
        }

        this.screenWidth = width; this.screenHeight = height;

        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);


        for (GLSurfaceView.Renderer renderer : rendererList) {
            renderer.onSurfaceChanged(glUnused, width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);

        for (GLSurfaceView.Renderer renderer : rendererList) {
            renderer.onDrawFrame(glUnused);
        }
    }

    /* called from GLSurfaceView */
    public void onRestoreInstanceState(Parcelable state) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestoreInstanceState() is called...";
            Log.i(TAG, logMessage);
        }
    }


    /*             Activity Life                     */
    @Override
    public void onRestart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestart() is called...";
            Log.i(TAG, logMessage);
        }

        for (ActivityLifeCycle activityLifeCycle : activityLifeCycleList) {
            activityLifeCycle.onRestart();
        }
    }

    @Override
    public void onStart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStart() is called...";
            Log.i(TAG, logMessage);
        }

        for (ActivityLifeCycle activityLifeCycle : activityLifeCycleList) {
            activityLifeCycle.onStart();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestoreInstanceState() is called...";
            Log.i(TAG, logMessage);
        }

        for (ActivityLifeCycle activityLifeCycle : activityLifeCycleList) {
            activityLifeCycle.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onResume() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onResume() is called...";
            Log.i(TAG, logMessage);
        }

        for (ActivityLifeCycle activityLifeCycle : activityLifeCycleList) {
            activityLifeCycle.onResume();
        }
    }

    @Override
    public void onPause() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onPause() is called...";
            Log.i(TAG, logMessage);
        }

        for (ActivityLifeCycle activityLifeCycle : activityLifeCycleList) {
            activityLifeCycle.onPause();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSaveInstanceState() is called...";
            Log.i(TAG, logMessage);
        }

        for (ActivityLifeCycle activityLifeCycle : activityLifeCycleList) {
            activityLifeCycle.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onStop() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStop() is called...";
            Log.i(TAG, logMessage);
        }

        for (ActivityLifeCycle activityLifeCycle : activityLifeCycleList) {
            activityLifeCycle.onStop();
        }
    }

    @Override
    public void onDestroy() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onDestroy() is called...";
            Log.i(TAG, logMessage);
        }

        for (ActivityLifeCycle activityLifeCycle : activityLifeCycleList) {
            activityLifeCycle.onDestroy();
        }
    }


    /**
     * Transfer a call to onTouchEvent to the listeners registered
     * @return boolean If all calls to listeners are true, then returns true, otherwise returns false
     */
    @Override
    public boolean onTouch(View glSurfaceView, MotionEvent motionEvent) {
        boolean b = true;
        for (View.OnTouchListener onTouchListener : onTouchListenerList) {
            b = (b && onTouchListener.onTouch(glSurfaceView, motionEvent));
        }

        return b;
    }
}