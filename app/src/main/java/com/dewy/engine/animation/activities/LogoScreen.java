package com.dewy.engine.animation.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dewy.engine.R;
import com.dewy.engine.animation.env.Skbl;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.SkbIntent;
import com.dewy.engine.animation.activities.view.TextureRectRenderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dewyone on 2015-10-04.
 */
public class LogoScreen extends GLActivity {
    private static final String TAG = "LogoScreen";

    private final GLActivity platform;
    private final GLContext GLContext;
    private final Context context;

    private TextureRectRenderer logoTRR;
    private int textureUnitNumber;
    private int imageID = R.drawable.push_push_title;

    private static final long secondInNano = 1000000000l;
    private static final long logoScreenTime = (long)(2.5f * secondInNano);      // 2.5 second in nano second
    //private static final long logoScreenTime = 2500000000l;
    private long logoScreenCreatedTime;

    private float screenWidth;
    private float screenHeight;

    public LogoScreen(GLActivity MainActivity) {
        this.platform = MainActivity;
        this.GLContext = platform.glContext;
        this.context = GLContext.getContext();

        textureUnitNumber = initTextureUnit();
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageID);
        float divider = Math.max( bitmap.getWidth(), bitmap.getHeight());       // puts the image in normalized coords, the largest side is 1.0f
        float imageWidth = (float) bitmap.getWidth() /divider;
        float imageHeight = (float) bitmap.getHeight() / divider;
        bitmap.recycle();
        logoTRR = new TextureRectRenderer(GLContext, 0, 0, imageWidth, imageHeight, textureUnitNumber);
        logoTRR.setRectDrawingInfo(getUvData());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        logoScreenCreatedTime = System.nanoTime();
        String logMessage = "Logo screen created Time : " + logoScreenCreatedTime;
        Log.i(TAG, logMessage);

        prepareDrawing();
    }

    public void prepareDrawing(){}

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        screenWidth = (float) width;
        screenHeight = (float) height;
    }

    float elapsedTime;
    @Override
    public void onDrawFrame(GL10 gl) {
        String logMessage = "logoScreenCreatedTime : " + logoScreenCreatedTime;
        //Log.i(TAG, logMessage);
        //logMessage = "System.nanoTime() - logoScreenCreatedTime : " + (System.nanoTime() - logoScreenCreatedTime);
        //Log.i(TAG, logMessage);

        elapsedTime = System.nanoTime() - logoScreenCreatedTime;
        if ( elapsedTime > logoScreenTime) {
            logMessage = "logoScreenTime : " + logoScreenTime;
            Log.i(TAG, logMessage);
            logMessage = "(System.nanoTime() - logoScreenCreatedTime) / secondInNano : " +
                    elapsedTime / secondInNano;
            Log.i(TAG, logMessage);

            //startStageSelector();       // plan to replace this call with the next call

            /**                               Starting Next activity                                   **/
            startNextActivity();
        }

        logoTRR.draw();
    }

    private void startNextActivity() {
        /* load the last activity in which the user was
        * and start the activity
        */

        String logMessage;

        String defaultActivityName =StageSelector.class.getName();



        /*                        load next activity information                           */
        SharedPreferences sharedPref = context.getSharedPreferences(
                Skbl.LASTACTIVITYCLASSNAME, Context.MODE_PRIVATE);
        //String lastActivityClassName = sharedPref.getString(context.getString(R.string.last_activity_class_name), defaultActivityName);
        String lastActivityClassName = sharedPref.getString(Skbl.LASTACTIVITYCLASSNAME, defaultActivityName);





        /*              Checking next activity               */
        if (lastActivityClassName == null) {        // next activity to start should never be null
            logMessage = "lastActivityClassName is null";
            Log.i(TAG, logMessage);
        } else {
            logMessage = "startNextActivity is called, last activity name : " + lastActivityClassName;
            Log.i(TAG, logMessage);
        }

        // next activity to start should not be same with this activity
        if ( lastActivityClassName.equals(LogoScreen.class.getName()) ) lastActivityClassName = defaultActivityName;
        else {
            logMessage = "lastActivityClassName " + lastActivityClassName +
                    " is not equel with LogoScreen.class.getName() " +LogoScreen.class.getName();
            Log.i(TAG, logMessage);
        }
        logMessage = "startNextActivity is called, last activity name : " + lastActivityClassName;
        Log.i(TAG, logMessage);




        /*              start next activity             */
        Class activityClass;
        try {
            activityClass = Class.forName(lastActivityClassName);
            logMessage = "got the class of the last activity" + activityClass.getName();
            Log.i(TAG, logMessage);

            SkbIntent nextStageIntent = new SkbIntent(this, activityClass);
            nextStageIntent.setBundle(null);        // null because we don't know what the activity really is, so just activate the activity
            platform.startActivity(nextStageIntent);

        } catch (ClassNotFoundException cnfe) {
            Log.i(TAG, cnfe.getMessage());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {

        /*
        if ( motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

            SkbIntent stageSelectorIntent = new SkbIntent(this, StageSelector.class);
            stageSelectorIntent.setBundle(null);
            platform.startActivity( stageSelectorIntent);

        } */

        return super.onTouch(v, motionEvent);
    }

    private int initTextureUnit() {
        int textureUnitConstTobeUsed = GLES20.GL_TEXTURE2;
        int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;

        int textureUnitCount = 1;
        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int imageID = this.imageID;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageID);

        GLES20.glActiveTexture(textureUnitConstTobeUsed);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);    // public static void texImage2D (int target, int level, Bitmap bitmap, int border)
        if (GLES20.glGetError() != 0) {
            Log.i("glGetError", "textImage2D()");
        }

        bitmap.recycle();

        return textureUnitNumber;
    }

    private float [] getUvData() {
        float [] uvData = {
                0f, 0f,
                0f, 1f,
                1f, 1f,
                1f, 0f
        };

        return uvData;
    }
}