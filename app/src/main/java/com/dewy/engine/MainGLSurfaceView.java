package com.dewy.engine;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by dewyone on 2015-08-04.
 */
public class MainGLSurfaceView extends GLSurfaceView {
    private static final String TAG = "MainGLSurfaceView";
    private static final boolean DEBUG_LIFECYCLE = true;

    private final Activity activityContext;

    // define Renderer
    private MainRenderer mRenderer;
    private boolean rendererSet = false;
    public boolean getRendererSet(){ return rendererSet;}

    public MainGLSurfaceView(Activity activityContext) {
        super(activityContext);
        String logMessage = "instantiator is called...";
        Log.i(TAG, logMessage);

        this.activityContext = activityContext;
    }

    public void onCreate(Bundle savedInstanceState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onCreate() is called...";
            Log.i(TAG, logMessage);
        }

        // Check if the system supports OpenGL ES 2.0.
        ActivityManager activityManager =
                (ActivityManager) activityContext.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager
                .getDeviceConfigurationInfo();
        // Even though the latest emulator supports OpenGL ES 2.0,
        // it has a bug where it doesn't set the reqGlEsVersion so
        // the above check doesn't work. The below will detect if the
        // app is running on an emulator, and assume that it supports
        // OpenGL ES 2.0.
        final boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));

        if (supportsEs2) {
            if (Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK built for x86"))
                setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            // Request an OpenGL ES 2.0 compatible context.
            setEGLContextClientVersion(2);

            // Assign our renderer.
            mRenderer = new MainRenderer(activityContext);
            setRenderer(mRenderer);
            rendererSet = true;
            mRenderer.onCreate(savedInstanceState);

            //Canvas canvas = new Canvas(BitmapFactory.decodeResource(activityContext.getResources(), R.drawable.dado_2));
        } else {
            /*
             * This is where you could create an OpenGL ES 1.x compatible
             * renderer if you wanted to support both ES 1 and ES 2. Since
             * we're not doing anything, the app will crash if the device
             * doesn't support OpenGL ES 2.0. If we publish on the market, we
             * should also add the following to AndroidManifest.xml:
             *
             * <uses-feature android:glEsVersion="0x00020000"
             * android:required="true" />
             *
             * This hides our app from those devices which don't support OpenGL
             * ES 2.0.
             */
            Toast.makeText(activityContext, "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        Button button;
    }

    public void onRestart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestart() is called...";
            Log.i(TAG, logMessage);
        }

        mRenderer.onRestart();
    }

    public void onStart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStart() is called...";
            Log.i(TAG, logMessage);
        }

        mRenderer.onStart();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestoreInstanceState() is called...";
            Log.i(TAG, logMessage);
        }
        super.onRestoreInstanceState(state);

        mRenderer.onRestoreInstanceState(state);
    }

    /**
     * Inform the view that the activity is resumed. The owner of this view must
     * call this method when the activity is resumed. Calling this method will
     * recreate the OpenGL display and resume the rendering
     * thread.
     * Must not be called before a renderer has been set.
     */
    @Override
    public void onResume(){
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onResume() is called...";
            Log.i(TAG, logMessage);
        }
        super.onResume();

        mRenderer.onResume();
    }

    /**
     * Inform the view that the activity is paused. The owner of this view must
     * call this method when the activity is paused. Calling this method will
     * pause the rendering thread.
     * Must not be called before a renderer has been set.
     */
    @Override
    public void onPause(){
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onPause() is called...";
            Log.i(TAG, logMessage);
        }
        super.onPause();

        mRenderer.onPause();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onResume() is called...";
            Log.i(TAG, logMessage);
        }

        //mRenderer.onSaveInstanceState();

        return super.onSaveInstanceState();
    }

    public void onStop() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStop() is called...";
            Log.i(TAG, logMessage);
        }

        mRenderer.onStop();
    }

    public void onDestroy() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onDestroy() is called...";
            Log.i(TAG, logMessage);
        }

        mRenderer.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mRenderer.onTouch(this, e);
        return true;
    }
}
