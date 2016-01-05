package com.dewy.engine.animation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dewy.engine.R;
import com.dewy.engine.animation.activities.SetupScreen;
import com.dewy.engine.animation.env.Skbl;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.activities.LogoScreen;
import com.dewy.engine.animation.activities.PlaySokoban;
import com.dewy.engine.animation.activities.GLActivity;
import com.dewy.engine.animation.activities.StageSelector;
import com.dewy.engine.animation.activities.Welcome;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dewyone on 2015-09-19.
 *
 * Mimics dalvic machine's activity control
 */
public class ActivityController extends GLActivity {
    private static final String TAG = "ActivityController";
    private static final boolean DEBUG_LIFECYCLE = true;

    private final Context context;

    private LogoScreen logoScreen;
    private Welcome welcome;
    private StageSelector stageSelector;
    private PlaySokoban playSokoban;
    private SetupScreen setupScreen;

    private GLActivity currentActivity;

    public ActivityController(GLContext glContext) {
        String logMessage;
        logMessage = "ActivityController is instanciated...";
        Log.i(TAG, logMessage);

        this.glContext = glContext;
        this.context = glContext.getContext();

        /*
        SkbIntent welcomeIntent = new SkbIntent(this, Welcome.class);
        welcomeIntent.setBundle(null);
        startActivity(welcomeIntent); */

        logMessage = "Launching logoScreen activity.";
        Log.i(TAG, logMessage);
        SkbIntent logoScreenIntent = new SkbIntent(null, LogoScreen.class);         // null : this activity should not be stopped when make other activity start
        logoScreenIntent.setBundle(null);
        startActivity(logoScreenIntent);
    }

    protected void startActivity() {
        String logMessage;

        SkbIntent receivedIntent = getReceivedIntent();
        GLActivity fromWhom = receivedIntent.getFromWhom();
        Class toWhomClass = receivedIntent.getToWhom();
        GLActivity toWhom = getActivity(toWhomClass);
        Bundle bundle = receivedIntent.getBundle();

        /* Caution : Once an activity is created, it is never destroyed.., Destroying policy is not yet present*/
        if ( fromWhom != null) {
            logMessage = "pausing And stopping : " + fromWhom.getClass().getSimpleName();
            Log.i(TAG, logMessage);
            fromWhom.onPause();
            fromWhom.onStop();
            if (fromWhom.isFinishing()) {
                logMessage = fromWhom.getClass().getSimpleName() + " is finishing..";
                Log.i(TAG, logMessage);
                fromWhom.onDestroy();
                if ( toWhom.getClass().getName().equals(fromWhom.getClass().getName()) ) toWhom = null;     // signals to recreate if needed
                nullifyActivity(fromWhom.getClass());       // nullify the last reference to the object
            }
        }

        logMessage = "Starting the following activity";
        Log.i(TAG, logMessage);

        if (toWhom != null && toWhom.isFinishing()) {
            logMessage = toWhom.getClass().getSimpleName() + " is finishing..";
            Log.i(TAG, logMessage);
            toWhom.onDestroy();
            // TODO : think more
            if (fromWhom != null && fromWhom.getClass().getName().equals(toWhom.getClass().getName()) ) fromWhom = null;     // signals to recreate if needed
            nullifyActivity(toWhom.getClass());       // nullify the last reference to the object
            toWhom = null;
        }

        if (toWhom == null) {
            logMessage = toWhomClass.getSimpleName() + " is not instanciated..";
            Log.i(TAG, logMessage);
            createActivity(toWhomClass);
            toWhom = getActivity( toWhomClass);
            //if ( toWhom == null) throw new Exception("No such class, toWhom is null");
            logMessage = "created the class, " + toWhom.toString();
            Log.i(TAG, logMessage);
            toWhom.onCreate(bundle);
            toWhom.onStart();
            toWhom.onResume();
            //logMessage = "glContext.getScreenWidth() : " + glContext.getScreenWidth() +
            //        ", glContext.getScreenHeight() :" + glContext.getScreenHeight();
            //Log.i(TAG, logMessage);
            toWhom.onSurfaceCreated(null, glContext.getEglConfig());
            toWhom.onSurfaceChanged(null, (int) glContext.getScreenWidth(),
                    (int) glContext.getScreenHeight());

        } else if (toWhom.isStoped()) {
            logMessage = toWhomClass.getSimpleName() + " is stopped.., restarting..";
            Log.i(TAG, logMessage);

            toWhom.onRestart();
            toWhom.onStart();
            toWhom.onResume();
            toWhom.onSurfaceCreated(null, glContext.getEglConfig());
            toWhom.onSurfaceChanged(null, (int) glContext.getScreenWidth(),
                    (int) glContext.getScreenHeight());
        } else if ( toWhom.isPaused()) {
            logMessage = toWhomClass.getSimpleName() + " is paused.., resuming..";
            Log.i(TAG, logMessage);
            toWhom.onResume();
            toWhom.onSurfaceCreated(null, glContext.getEglConfig());
            toWhom.onSurfaceChanged(null, (int) glContext.getScreenWidth(),
                    (int) glContext.getScreenHeight());
        }

        currentActivity = toWhom;
        setHaveReceivedIntent(false);
    }


    /*                    Activity Life Cycle                       */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onCreate() is called...";
            Log.i(TAG, logMessage);
        }
        super.onCreate(savedInstanceState);

        if ( currentActivity != null) currentActivity.onCreate(savedInstanceState);
    }

    @Override
    public void onRestart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestart() is called...";
            Log.i(TAG, logMessage);
        }
        super.onRestart();

        if ( currentActivity != null) currentActivity.onRestart();
    }

    @Override
    public void onStart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStart() is called...";
            Log.i(TAG, logMessage);
        }
        super.onStart();

        if ( currentActivity != null) currentActivity.onStart();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestoreInstanceState() is called...";
            Log.i(TAG, logMessage);
        }
        super.onRestoreInstanceState(savedInstanceState);

        if ( currentActivity != null) currentActivity.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onResume() is called...";
            Log.i(TAG, logMessage);
        }
        super.onResume();

        if ( currentActivity != null) currentActivity.onResume();
    }

    @Override
    public void onPause() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onPause() is called...";
            Log.i(TAG, logMessage);
        }

        if ( currentActivity != null) {
            currentActivity.onPause();

            if (glContext.getActivityContext().isFinishing()) {
                saveActivity(currentActivity);
            }
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSaveInstanceState() is called...";
            Log.i(TAG, logMessage);
        }

        if ( currentActivity != null) currentActivity.onSaveInstanceState(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStop() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStop() is called...";
            Log.i(TAG, logMessage);
        }

        if ( currentActivity != null) {
            currentActivity.onStop();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onDestroy() is called...";
            Log.i(TAG, logMessage);
        }

        if ( currentActivity != null) currentActivity.onDestroy();
        super.onDestroy();
    }


    /*                  Renderer Life Cycle                 */

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSurfaceCreated() is called...";
            Log.i(TAG, logMessage);
        }

        if ( currentActivity != null) currentActivity.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSurfaceChanged() is called...";
            Log.i(TAG, logMessage);
        }
        if ( currentActivity != null) currentActivity.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        String logMessage;
        if (haveReceivedIntent()) {
            logMessage = "Have Received Intent.. starting Activity..";
            Log.i(TAG, logMessage);
            startActivity();
        }

        //logMessage = "currentActivity.toString() : " + currentActivity.toString();
        //Log.i(TAG, logMessage);

        if (currentActivity != null) currentActivity.onDrawFrame(gl);
        else Log.i(TAG, "currentActivity is null !!!!");
    }

    /**
     * @return True if this method has consumed the event, false otherwise */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return (super.onTouch(v, event) && currentActivity.onTouch(v, event));
    }




    private GLActivity getActivity(Class toWhomClass) {
        String logMessage = "toWhomClass.toString() : " + toWhomClass.toString();
        //Log.i(TAG, logMessage);

        logMessage = "toWhomClass.getCanonicalName() : " + toWhomClass.getCanonicalName();
        //Log.i(TAG, logMessage);

        logMessage = "toWhomClass.getName() : " + toWhomClass.getName();
        //Log.i(TAG, logMessage);

        if (toWhomClass.getName().equals(LogoScreen.class.getName())) return logoScreen;
        else if (toWhomClass.getName().equals(Welcome.class.getName())) return welcome;
        else if ( toWhomClass.getName().equals(StageSelector.class.getName())) return stageSelector;
        else if ( toWhomClass.getName().equals(PlaySokoban.class.getName())) return playSokoban;
        else if ( toWhomClass.getName().equals(SetupScreen.class.getName())) return setupScreen;

        return null;
    }

    private void createActivity(Class activity) {
        if (activity.getName().equals(LogoScreen.class.getName())) logoScreen = new LogoScreen(this);
        else if (activity.getName().equals(Welcome.class.getName())) welcome = new Welcome(this);
        else if (activity.getName().equals(StageSelector.class.getName())) stageSelector = new StageSelector(this);
        else if (activity.getName().equals(PlaySokoban.class.getName())) playSokoban = new PlaySokoban(this);
        else if (activity.getName().equals(SetupScreen.class.getName())) setupScreen = new SetupScreen(this, currentActivity);
    }

    private void nullifyActivity(Class activity) {
        if (activity.getName().equals(LogoScreen.class.getName())) logoScreen = null;
        else if (activity.getName().equals(Welcome.class.getName())) welcome = null;
        else if (activity.getName().equals(StageSelector.class.getName())) stageSelector = null;
        else if (activity.getName().equals(PlaySokoban.class.getName())) playSokoban = null;
        else if (activity.getName().equals(SetupScreen.class.getName())) setupScreen = null;
    }

    // the function to save the class name of last activity
    public void saveActivity(GLActivity activity) {
        String logMessage = "saveActivity() is called..";
        Log.i(TAG, logMessage);

        String activityClassName = activity.getClass().getName();
        logMessage = "activityClassName : " + activityClassName;
        Log.i(TAG, logMessage);

        Context context = this.context;

        SharedPreferences sharedPref = context.getSharedPreferences(
                Skbl.LASTACTIVITYCLASSNAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();        // same with shared preference name, no special reason
        editor.putString(Skbl.LASTACTIVITYCLASSNAME, activityClassName);
        editor.commit();
    }

    // for testing
    private void saveActivityVerifier() {
        String logMessage = "saveActivityVerifier() is called";
        Log.i(TAG, logMessage);

        SharedPreferences sharedPref = context.getSharedPreferences(
                Skbl.LASTACTIVITYCLASSNAME, Context.MODE_PRIVATE);
        //String lastActivityClassName = sharedPref.getString(context.getString(R.string.last_activity_class_name), defaultActivityName);
        String lastActivityClassName = sharedPref.getString(Skbl.LASTACTIVITYCLASSNAME, null);
        if (lastActivityClassName == null) {
            logMessage = "lastActivityClassName is null";
            Log.i(TAG, logMessage);
        } else {
            logMessage = "last activity name : " + lastActivityClassName;
            Log.i(TAG, logMessage);
        }
    }
}