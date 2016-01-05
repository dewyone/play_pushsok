package com.dewy.engine.animation.activities;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dewy.engine.platform.ActivityLifeCycle;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.SkbIntent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dewyone on 2015-08-31.
 */
public abstract class GLActivity implements ActivityLifeCycle, GLSurfaceView.Renderer, View.OnTouchListener {
    private static final String TAG = "GLActivity";
    protected GLContext glContext;

    private SkbIntent receivedIntent = null;
    private boolean haveReceivedIntent = false;

    /* states of activity life cycle  -- value 'false' is meaningless */
    private boolean isCreated = false;
    private boolean isRestarted= false;
    private boolean isStarted = false;
    private boolean isResumed = false;
    private boolean isPaused = false;
    private boolean isStoped = false;
    private boolean isDestroyed = false;
    private boolean mFinished = false;
    private boolean destroyAndStartAnotherActivity = false;

    /** EGL context problem due to thread
     * caller to this method should stop using egl thread ( need to think through more )
     *   or you can say should be out of method call stack
     *   or the method calls should not make a circle ( for example, this instance -> A -> this instance)
     */
    public final void startActivity(SkbIntent skbIntent){
        receivedIntent = skbIntent;
        setHaveReceivedIntent(true);
    }

    public final void finishAndStartActivity(GLActivity glActivity, SkbIntent skbIntent) {
        glActivity.finish();
        startActivity(skbIntent);
    }

    public final void finishBothAndStartActivity(GLActivity glCallerActivity, GLActivity glTargetActivity, SkbIntent skbIntent) {
        glCallerActivity.finish();
        glTargetActivity.finish();
        startActivity(skbIntent);
    }

    //protected void startActivity() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setIsCreated(true);
        setIsDestroyed(false);
    }

    @Override
    public void onRestart(){
        setIsRestarted(true);
        setStoped(false);
    }

    /** basic application startup logic that should happen only once for the entire life of the activity
     *   for example, UI, class-scope variables, verify that required system features are enabled
     *   After this method executed, this activity should be visible to users
     * */
    @Override
    public void onStart() {
        setStarted(true);

        /* for example
        // The activity is either being restarted or started for the first time
        // so this is where we should make sure that GPS is enabled
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            // Create a dialog here that requests the user to enable GPS, and use an intent
            // with the android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS action
            // to take the user to the Settings screen to enable GPS when they click "OK"
        }  */

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        /** usage for example..
         // Always call the superclass so it can restore the view hierarchy
         super.onRestoreInstanceState(savedInstanceState);

         // Restore state members from saved instance
         mCurrentScore = savedInstanceState.getInt(STATE_SCORE);
         mCurrentLevel = savedInstanceState.getInt(STATE_LEVEL);
         */
    }

    /**  initialize components that you release during onPause()
     *    for example, initiating camera
     */
    @Override
    public void onResume() {
        setResumed(true);
        setPaused(false);

        /* example
        // Get the Camera instance as the activity achieves full user focus
        if (mCamera == null) {
            initializeCamera(); // Local method to handle camera init
        } */

    }

    /**  Stop using CPU
     *   Stop using system resources
     *   save that should be saved while running
     * */
    @Override
    public void onPause() {
        setPaused(true);
        setResumed(false);

        /* for example
        // Release the Camera because we don't need it when paused
        // and other activities might need to use it.
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }  */

    }

    /** The system calls this method when the user is leaving your activity
     * As the system begins to stop your activity, it calls onSaveInstanceState()
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        /** usage for example
         // Save the user's current game state
         savedInstanceState.putInt(STATE_SCORE, mCurrentScore);
         savedInstanceState.putInt(STATE_LEVEL, mCurrentLevel);

         // Always call the superclass so it can save the view hierarchy state
         super.onSaveInstanceState(savedInstanceState);
         */
    }

    /** it's no longer visible and should release almost all resources
     * that aren't needed while the user is not using it
     * for example,
     *      finish writing to a database,
     *      save the contents of whatever there is to be saved to a persistent storage,
     *      finish memory use...
     */
    @Override
    public void onStop() {
        setStoped(true);
        setStarted(false);
        setIsRestarted(false);

        // create a bundle
        // pass it to onSaveInstanceState() method
        // save it to a persistent storage
    }

    /** This method is your last chance to clean out resources that could lead to a memory leak,
     * so you should be sure that additional threads are destroyed
     * and other long-running actions like method tracing are also stopped
     */
    @Override
    public void onDestroy() {
        setIsDestroyed(true);
        setIsCreated(false);
        //mFinished = false;
    }

    /**
     * Call this when your activity is done and should be closed.
     * //The ActivityResult is propagated back to whoever launched you via
     * //onActivityResult().
     *
     * At this moment, this method means "destroy me when start another activity"
     */
    public void finish() {
        mFinished = true;
    }

    /**
     * Check to see whether this activity is in the process of finishing,
     * either because you called {@link #finish} on it or someone else
     * has requested that it finished.  This is often used in
     * {@link #onPause} to determine whether the activity is simply pausing or
     * completely finishing.
     *
     * @return If the activity is finishing, returns true; else returns false.
     *
     * @see #finish
     */
    public boolean isFinishing() {
        return mFinished;
    }

    public void setDestroyAndStartAnotherActivity(boolean destroyAndStartAnotherActivity) {
        this.destroyAndStartAnotherActivity = destroyAndStartAnotherActivity;
    }

    public boolean isDestroyAndStartAnotherActivity() {
        return destroyAndStartAnotherActivity;
    }




    /**
     * Called when the rendering thread starts and whenever the EGL context is lost.
     * The EGL context will typically be lost when the Android device awakes after going to sleep
     */
    @Override
    public abstract void onSurfaceCreated(GL10 gl, EGLConfig config);

    @Override
    public abstract void onSurfaceChanged(GL10 gl, int width, int height);

    @Override
    public abstract void onDrawFrame(GL10 gl);


    /* True if the listener has consumed the event, false otherwise*/
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }



    public final SkbIntent getReceivedIntent() { return receivedIntent;}

    private final void setReceivedIntent(SkbIntent receivedIntent) { this.receivedIntent = receivedIntent;}

    public final boolean haveReceivedIntent() {
        return haveReceivedIntent;
    }

    public final void setHaveReceivedIntent(boolean haveReceivedIntent) {
        this.haveReceivedIntent = haveReceivedIntent;
    }





    public synchronized void setIsCreated(boolean isCreated) {
        this.isCreated = isCreated;
    }

    public synchronized boolean getIsCreated() {
        return isCreated;
    }

    public synchronized void setIsRestarted(boolean isRestarted) {
        String logMessage = "setIsRestarted is called.., isRestarted :  " + isRestarted;
        Log.i(TAG, logMessage);
        this.isRestarted = isRestarted;
    }

    public synchronized boolean isRestarted() {
        return isRestarted;
    }

    public synchronized boolean isStarted() {
        return isStarted;
    }

    private synchronized void setStarted(boolean isStarted) {
        this.isStarted = isStarted;
    }

    public synchronized boolean isResumed() {
        return isResumed;
    }

    private synchronized void setResumed(boolean isResumed) {
        this.isResumed = isResumed;
    }

    public synchronized void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public synchronized boolean isPaused() {
        return isPaused;
    }

    public synchronized void setStoped(boolean isStoped) {
        this.isStoped = isStoped;
    }

    public synchronized boolean isStoped() {
        return isStoped;
    }

    public void setIsDestroyed(boolean isDestroyed) {
        this.isDestroyed = isDestroyed;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }
}
