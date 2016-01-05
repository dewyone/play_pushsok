package com.dewy.engine;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;


public class MainActivity extends Activity{
    private static final String TAG = "MainActivity";
    private static final boolean DEBUG_LIFECYCLE = true;
    /**
     * Hold a reference to our GLSurfaceView
     */
    private MainGLSurfaceView mainGLSurfaceView;
    private boolean rendererSet = false;

    /* called when app process doesn't exist or app process is killed*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onCreate() is called...";
            Log.i(TAG, logMessage);
        }

        /*              Window Setting              */

        /*  we have to call these methods before we set the content view of our activity
        *   requestWindowFeature(), getWindow().setFlags()
        * */
        // Turn off the window's title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        // set volume coontrol stream to music stream
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Fullscreen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /* Keep Screen on*/
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        /*              The only view               */

        mainGLSurfaceView = new MainGLSurfaceView(this);
        mainGLSurfaceView.onCreate(savedInstanceState);
        rendererSet = mainGLSurfaceView.getRendererSet();

        if (rendererSet) setContentView(mainGLSurfaceView);
        else {
            Log.w(TAG, "Couldn't set Renderer");
            System.runFinalization();
        }
    }

    /* called After onStop() and before onStart() when user navigates to the activity*/
    @Override
    protected void onRestart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestart() is called...";
            Log.i(TAG, logMessage);
        }
        super.onRestart();

        if (rendererSet) {
            mainGLSurfaceView.onRestart();
        }
    }

    @Override
    protected void onStart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStart() is called...";
            Log.i(TAG, logMessage);
        }
        super.onStart();

        if (rendererSet) {
            mainGLSurfaceView.onStart();
        }
    }

    /* This method is called after onStart()
        when the activity is being re-initialized from a previously saved state
    */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestoreInstanceState() is called...";
            Log.i(TAG, logMessage);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onResume() is called...";
            Log.i(TAG, logMessage);
        }
        super.onResume();

        if (rendererSet) {
            mainGLSurfaceView.onResume();
        }
    }

    @Override
    protected void onPause() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onPause() is called...";
            Log.i(TAG, logMessage);
        }

        if (rendererSet) {
            mainGLSurfaceView.onPause();
        }

        if (isFinishing()) {
            logMessage = "isFinishing() is true";
            Log.i(TAG, logMessage);
        }

        super.onPause();
    }

    /* If called, this method will occur before onStop().
        There are no guarantees about whether it will occur before or after onPause()
    */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSaveInstanceState() is called...";
            Log.i(TAG, logMessage);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStop() is called...";
            Log.i(TAG, logMessage);
        }

        if (rendererSet) {
            mainGLSurfaceView.onStop();
        }

        super.onStop();

        if (isChangingConfigurations()) {
            logMessage = "isChangingConfigurations() is true";
            Log.i(TAG, logMessage);
        }
    }

    @Override
    public void onDestroy() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onDestroy() is called...";
            Log.i(TAG, logMessage);
        }

        if (rendererSet) {
            mainGLSurfaceView.onDestroy();
        }

        super.onDestroy();
    }
}