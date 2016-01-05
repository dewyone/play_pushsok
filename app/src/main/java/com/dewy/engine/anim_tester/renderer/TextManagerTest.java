package com.dewy.engine.anim_tester.renderer;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dewy.engine.animation.view.unit.TexturePrimitiveData;
import com.dewy.engine.platform.ActivityLifeCycle;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.text.TextManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dewyone on 2015-11-02.
 */
public class TextManagerTest implements GLSurfaceView.Renderer, View.OnTouchListener, ActivityLifeCycle {
    private static final String TAG = "TextManagerTest";
    private static final boolean DEBUG_LIFECYCLE = true;

    private final com.dewy.engine.platform.GLContext glContext;      // Activity( Context) + GLSurfaceView + TouchEventListener

    private TextManager textManager;

    public TextManagerTest(GLContext glContext) {
        String logMessage = "instantiated...";
        Log.i(TAG, logMessage);
        this.glContext = glContext;

        glContext.addRenderer(this);
        glContext.addActivityLifeCycleListener(this);
        glContext.addOnTouchListener(this);
    }

    /*                    Activity Life Cycle                       */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onCreate() is called...";
            Log.i(TAG, logMessage);
        }


    }

    @Override
    public void onRestart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestart() is called...";
            Log.i(TAG, logMessage);
        }


    }

    @Override
    public void onStart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStart() is called...";
            Log.i(TAG, logMessage);
        }


    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestoreInstanceState() is called...";
            Log.i(TAG, logMessage);
        }


    }

    @Override
    public void onResume() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onResume() is called...";
            Log.i(TAG, logMessage);
        }


    }

    @Override
    public void onPause() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onPause() is called...";
            Log.i(TAG, logMessage);
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSaveInstanceState() is called...";
            Log.i(TAG, logMessage);
        }


    }

    public void onStop() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStop() is called...";
            Log.i(TAG, logMessage);
        }


    }

    @Override
    public void onDestroy() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onDestory() is called...";
            Log.i(TAG, logMessage);
        }


    }



    /*                  Renderer Life Cycle                 */

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSurfaceCreated() is called...";
            Log.i(TAG, logMessage);
        }

        textManager = new TextManager(glContext);
        //TexturePrimitiveData texturePrimitiveData = textManager.createString(Integer.toString(0), 0, 0, 0.2f);
        textManager.addTextData(textManager.createString("0", 0, 0, 0.2f));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSurfaceChanged() is called...";
            Log.i(TAG, logMessage);
        }


    }

    @Override
    public void onDrawFrame(GL10 gl) {
        textManager.drawTexts();
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
