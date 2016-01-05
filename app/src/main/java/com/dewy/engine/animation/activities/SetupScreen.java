package com.dewy.engine.animation.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dewy.engine.R;
import com.dewy.engine.animation.SkbIntent;
import com.dewy.engine.animation.activities.lib.ImageButtoner;
import com.dewy.engine.animation.activities.lib.Sounder;
import com.dewy.engine.animation.activities.lib.GLToggleButton;
import com.dewy.engine.animation.activities.lib.ImageButtoner11;
import com.dewy.engine.animation.activities.view.TextureRectRenderer;
import com.dewy.engine.animation.env.Skbl;
import com.dewy.engine.animation.media.SoundPlayer;
import com.dewy.engine.animation.view.unit.TexturePrimitiveData;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.text.TextBuilder;
import com.dewy.engine.text.TextManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dewyone on 2015-10-31.
 */
public class SetupScreen extends GLActivity {
    private static final String TAG = "SetupScreen";
    private static final boolean DEBUG_LIFECYCLE = true;

    private final GLActivity platform;
    private final GLContext glContext;
    private final Context context;

    private GLActivity callerGLActivity;

    private float screenWidth;
    private float screenHeight;

    private SoundPlayer soundPlayer;

    private TextureRectRenderer selectStageTRR;
    private TextureRectRenderer soundTRR;
    private int textureUnitNumber;
    private int selectLevel_ImageID = R.drawable.select_stage02_label;
    private int sound_ImageID = R.drawable.sound_label;
    private int sndToggle = R.drawable.onofftoggle;
    private int nextLevel_ImageID  = R.drawable.arrow_button_prac01;
    private int resume_ImageID = R.drawable.resume02;
    private int play_ImageID = R.drawable.play_button;

    private ButtonManager bttnMngr;
    private int stageNumberSelected;
    private boolean soundOn;
    //private TextManager tm;
    //private ImageSpinner imageSpinner;

    private static final long secondInNano = 1000000000l;
    private static final long logoScreenTime = (long)(2.5f * secondInNano);      // 2.5 second in nano second
    //private static final long logoScreenTime = 2500000000l;
    private long logoScreenCreatedTime;

    public SetupScreen(GLActivity platform, GLActivity callerGLActivity) {
        this.platform = platform;
        this.glContext = platform.glContext;
        this.context = glContext.getContext();
        this.callerGLActivity = callerGLActivity;

        soundPlayer = new SoundPlayer(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        stageNumberSelected = savedInstanceState.getInt(Skbl.STAGENUMBER);
        soundOn = Sounder.isSoundOn();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        bttnMngr = new ButtonManager();
        bttnMngr.setStageNumber(stageNumberSelected);
    }

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
        // For transparent background and others
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // draw buttons
        bttnMngr.draw();
        //imageSpinner.draw();

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {
        boolean b = super.onTouch(v, motionEvent);

        b &= bttnMngr.onTouchEvent(motionEvent);

        String logMessage = "b &= " + b;
        Log.i(TAG, logMessage);

        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

            if (bttnMngr.nextLevelButton.isButtonClicked()) {
                // TODO : reflect the limit stage of this user
                stageNumberSelected++;
                if (stageNumberSelected >= 100) stageNumberSelected = 1;
                bttnMngr.setStageNumber(stageNumberSelected);

                bttnMngr.nextLevelButton.setButtonClicked(false);
            }
            else if (bttnMngr.prevLevelButton.isButtonClicked()) {
                //imageSpinner.spinUp();
                stageNumberSelected--;
                if (stageNumberSelected <=0) stageNumberSelected = 99;
                bttnMngr.setStageNumber(stageNumberSelected);

                bttnMngr.prevLevelButton.setButtonClicked(false);
            }
            else if (bttnMngr.playButton.isButtonClicked()) {
                bttnMngr.playButton.setButtonClicked(false);

                SkbIntent intent = new SkbIntent(this, PlaySokoban.class);
                Bundle bundle = new Bundle();
                bundle.putInt(Skbl.STAGENUMBER, stageNumberSelected);
                bundle.putBoolean(Skbl.NEWSTAGE, true);
                intent.setBundle(bundle);
                //platform.finishAndStartActivity(this, intent);
                platform.finishBothAndStartActivity(this, callerGLActivity, intent);        // finish myself and recreate target activity
            }
            else if (bttnMngr.soundToggleButton.isButtonClicked()) {
                Sounder.setSoundOn(bttnMngr.soundToggleButton.isToggleOn());

                bttnMngr.soundToggleButton.setButtonClicked(false);
            }
            else if (bttnMngr.resumeButton.isButtonClicked()) {
                bttnMngr.resumeButton.setButtonClicked(false);

                SkbIntent intent = new SkbIntent(this, PlaySokoban.class);
                platform.finishAndStartActivity(this, intent);
            }
        }

        b &= true;      // we consumed the event

        /*
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

            if (bttnMngr.nextLevelButton.isButtonDown()) {
                // TODO : reflect the limit stage of this user
                stageNumberSelected++;
                if (stageNumberSelected >= 100) stageNumberSelected = 1;
                bttnMngr.setStageNumber(stageNumberSelected);
            }
            else if (bttnMngr.prevLevelButton.isButtonDown()) {
                //imageSpinner.spinUp();
                stageNumberSelected--;
                if (stageNumberSelected <=0) stageNumberSelected = 99;
                bttnMngr.setStageNumber(stageNumberSelected);
            }
            else if (bttnMngr.soundToggleButton.isButtonDown()) {
                Sounder.setSoundOn(bttnMngr.soundToggleButton.isToggleOn());
            }
            else if (bttnMngr.resumeButton.isButtonDown()) {
                SkbIntent intent = new SkbIntent(this, PlaySokoban.class);
                platform.startActivity(intent);
            }
        } */

        return b;
    }


    private int initTextureUnitForSelectStage() {
        int textureUnitConstTobeUsed = GLES20.GL_TEXTURE1;
        int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;      // return value

        int textureUnitCount = 1;
        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int imageID = selectLevel_ImageID;
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

    private int initTextureUnitForNextLevel() {
        int textureUnitConstTobeUsed = GLES20.GL_TEXTURE2;
        int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;      // return value

        int textureUnitCount = 1;
        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int imageID = this.nextLevel_ImageID;
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

    private int initTextureUnitForPlay() {
        int textureUnitConstTobeUsed = GLES20.GL_TEXTURE3;
        int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;      // return value

        int textureUnitCount = 1;
        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int imageID = play_ImageID;
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

    private int initTextureUnitForSound() {
        int textureUnitConstTobeUsed = GLES20.GL_TEXTURE4;
        int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;      // return value

        int textureUnitCount = 1;
        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int imageID = sound_ImageID;
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

    private int initTextureUnitForSoundToggleButton() {
        int textureUnitConstTobeUsed = GLES20.GL_TEXTURE5;
        int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;      // return value

        int textureUnitCount = 1;
        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int imageID = sndToggle;
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

    private int initTextureUnitForResume() {
        int textureUnitConstTobeUsed = GLES20.GL_TEXTURE6;
        int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;      // return value

        int textureUnitCount = 1;
        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int imageID = resume_ImageID;
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

    final class ButtonManager {

        private Sounder sounder;

        private ImageButtoner11 nextLevelButton;
        private ImageButtoner11 prevLevelButton;
        private LevelNumbererManager levelNumbererManager;
        private ImageButtoner11 playButton;
        private GLToggleButton soundToggleButton;
        private ImageButtoner11 resumeButton;

        private int stageNumber;

        private float snXPos;
        private float snYPos;
        private float fontSize;

        private static final long buttonPresentationTime = 15000000000l;
        private static final long buttonPresentationTimeInSec = buttonPresentationTime / 1000000000l;
        private static final float buttonColorAlpha = 0.5f;        // At start, we draw direction buttons at the color alpha of 0.5f

        public ButtonManager() {
            float alpha = buttonColorAlpha;

            // ImageButtoner11's x, y coords means the center of the object
            float yPos_start =0.6f;
            float gapV = 0.05f;    // space between letter lines
            float gapH = 0.02f;     // space between buttons or labels or whatsoever

            float ssXPos = 0.0f;        // stage select label
            float ssYPos = yPos_start;
            float ssWidth = 1.2f;
            float ssHeight = 0.3f;

            float levelBttnsXPos = -0.7f;
            float pbXPos = levelBttnsXPos + 0.0f;       // previous level button
            float pbYPos = ssYPos - ssHeight - gapV;
            float pbWidth = 0.3f;
            float pbHeight = 0.3f;

            fontSize = 0.3f;        // stage number
            //snXPos = -(fontSize / 2);
            snXPos = pbXPos + (pbWidth / 2) + 0.05f;      // 0.05f : gap between previous button and stage number
            snYPos = ssYPos - (ssHeight / 2) - 0.05f;

            float nbXPos = snXPos + fontSize * 1.6f;       // next level button
            float nbYPos = ssYPos - ssHeight - gapV;
            float nbWidth = 0.3f;
            float nbHeight = 0.3f;

            float playXpos = nbXPos + nbWidth + 0.2f;     // play button, 0.05f : gap
            float playYPos = ssYPos - ssHeight - gapV;
            float playWidth = 0.45f;
            float playHeight = 0.35f;

            float sndXPos = -0.3f;       // sound label
            float sndYPos = pbYPos - pbHeight - gapV;
            float sndWidth = 0.5f;
            float sndHeight = 0.3f;

            float sndTXPos = 0.3f;       // sound toggle button
            float sndTYPos = sndYPos;
            float sndTWidth = 0.5f;
            float sndTHeight = 0.3f;

            float rsmXPos = 0.0f;       // resume label
            float rsmYPos = sndTYPos - sndTHeight - gapV;
            float rsmWidth = 0.6f;
            float rsmHeight = 0.3f;

            /* button */
            textureUnitNumber = initTextureUnitForNextLevel();
            nextLevelButton = new ImageButtoner11(glContext, nbXPos, nbYPos, nbWidth, nbHeight, textureUnitNumber);

            prevLevelButton = new ImageButtoner11(glContext, pbXPos, pbYPos, pbWidth, pbHeight, textureUnitNumber);
            prevLevelButton.rotate(180);

            levelNumbererManager = new LevelNumbererManager();

            textureUnitNumber = initTextureUnitForPlay();
            playButton = new ImageButtoner11(glContext, playXpos, playYPos, playWidth, playHeight, textureUnitNumber);

            textureUnitNumber = initTextureUnitForSoundToggleButton();
            soundToggleButton = new GLToggleButton(glContext, sndTXPos, sndTYPos, sndTWidth, sndTHeight, textureUnitNumber);
            soundToggleButton.setToggleOn(soundOn);

            textureUnitNumber = initTextureUnitForResume();
            resumeButton = new ImageButtoner11(glContext, rsmXPos, rsmYPos, rsmWidth, rsmHeight, textureUnitNumber);

            sounder = new Sounder(soundPlayer.getSoundPool(), soundPlayer.getButtonClickID());
            nextLevelButton.attachClickSounder(sounder);
            prevLevelButton.attachClickSounder(sounder);
            soundToggleButton.attachClickSounder(sounder);
            resumeButton.attachClickSounder(sounder);


            /* label */
            textureUnitNumber = initTextureUnitForSelectStage();
            selectStageTRR = new TextureRectRenderer(glContext, ssXPos, ssYPos, ssWidth, ssHeight, textureUnitNumber);
            selectStageTRR.setRectDrawingInfo(getUvData());

            textureUnitNumber = initTextureUnitForSound();
            soundTRR = new TextureRectRenderer(glContext, sndXPos, sndYPos, sndWidth, sndHeight, textureUnitNumber);
            soundTRR.setRectDrawingInfo(getUvData());

        }

        public void setStageNumber(int stageNumber) {
            this.stageNumber = stageNumber;
            levelNumbererManager.setNumberToDraw(stageNumber, snXPos, snYPos, fontSize);
        }

        public void draw() {
            selectStageTRR.draw();
            nextLevelButton.draw();
            prevLevelButton.draw();
            levelNumbererManager.draw();
            playButton.draw();
            soundTRR.draw();
            soundToggleButton.draw();
            resumeButton.draw();
        }

        /* True if the listener has consumed the event, false otherwise*/
        public boolean onTouchEvent(MotionEvent motionEvent) {
            nextLevelButton.onTouchEvent(motionEvent);
            prevLevelButton.onTouchEvent(motionEvent);
            playButton.onTouchEvent(motionEvent);
            soundToggleButton.onTouchEvent(motionEvent);
            resumeButton.onTouchEvent(motionEvent);

            return true;
        }

    }

    /**
     * Basic Concept :
     *          prepare 11 TexturePrimitiveData instance in array
     *          (10 letters for 0 ~ 9, and 1 letter for when same letter appears at the same time (11, 22, 33 ...)
     *          managing the 11 instances using ArrayList
     *
     *  Function :
     *          setting a number to draw
     *          draw the number
     */
    class LevelNumbererManager {
        private static final String TAG = "LevelNumberManager";

        private TexturePrimitiveData [] numberDataList;     // retains objects
        private TextManager textManager;

        private final int totalLetterDataReserved = 11;     // 10 letters for 0 ~ 9, and 1 letter for when same letter appears at the same time (11, 22, 33 ...)

        public LevelNumbererManager() {
            textManager = new TextManager(glContext);
            numberDataList = new TexturePrimitiveData[totalLetterDataReserved];
        }

        public void setNumberToDraw(int number, float x, float y, float fontSize) {
            numberPositioning(number, x, y, fontSize);
        }

        public void draw() {
            textManager.drawTexts();
        }

        // preparing TextManager
        private void numberPositioning(int number, float x, float y, float fontSize) {
            String logMessage;
            Integer integer = new Integer(number);
            int digitCounter = integer.toString().length();
            int divider = (int) Math.pow(10, (digitCounter - 1));
            int digit = 0;
            int digitBefore = 0;
            float xPos = x;
            float yPos  = y;
            float digitGap = 0.05f;
            TexturePrimitiveData texturePrimitiveData;

            logMessage = "got number : " + number;
            Log.i(TAG, logMessage);
            logMessage = "divider : " + divider + ", number length : " + digitCounter;
            Log.i(TAG, logMessage);
            textManager.clear();

            /* iterates the digits of a number while getting the most significant digit of number and add it*/
            for (int i = 0; i < digitCounter; i++) {

                digit = number / divider;       // the most significant digit of number
                logMessage = "digit to add to textManager: " + digit;
                Log.i(TAG, logMessage);

                // get and add data
                if (i == 1 && (digitBefore == digit)) {     //** when second digit and first are same, we copies object data
                    texturePrimitiveData = getDigitCopied(digit, xPos, yPos, fontSize);
                } else texturePrimitiveData = getDigit(digit, xPos, yPos, fontSize);
                textManager.addTextData(texturePrimitiveData);

                number = number % divider;      // cut away the digit above
                divider /= 10;
                digitBefore = digit;

                // adjusting the position
                xPos = texturePrimitiveData.vertexData[6] + digitGap;
            }
        }

        private TexturePrimitiveData getDigit(int digitOfNumber, float xPos, float yPos, float fontSize) {
            if (numberDataList[digitOfNumber]  == null) {
                //String logMessage = "Integer.toString(digitOfNumber) : " + Integer.toString(digitOfNumber);
                //Log.i(TAG, logMessage);
                numberDataList[digitOfNumber] = textManager.createString(Integer.toString(digitOfNumber), xPos, yPos, fontSize);
            } else {
                TexturePrimitiveData texturePrimitiveData = numberDataList[digitOfNumber];
                adjustData(texturePrimitiveData, digitOfNumber, xPos, fontSize);
            }

            return numberDataList[digitOfNumber];
        }

        private TexturePrimitiveData getDigitCopied(int digitOfNumber, float xPos, float yPos, float fontSize) {
            TexturePrimitiveData texturePrimitiveData;
            if (numberDataList[10] == null) {
                numberDataList[10] = textManager.createString(Integer.toString(digitOfNumber), xPos, yPos, fontSize);
                texturePrimitiveData = numberDataList[10];
            } else {
                numberDataList[digitOfNumber].copy(numberDataList[10]);
                texturePrimitiveData = numberDataList[10];
                adjustData(texturePrimitiveData, digitOfNumber, xPos, fontSize);
            }

            return texturePrimitiveData;
        }

        private void adjustData(TexturePrimitiveData texturePrimitiveData, int digitOfNumber, float xPos, float fontSize) {
            texturePrimitiveData.vertexData[0] = xPos;
            texturePrimitiveData.vertexData[3] = xPos;
            texturePrimitiveData.vertexData[6] = xPos + (fontSize * TextBuilder.getRealLetterSize(Integer.toString(digitOfNumber).charAt(0)));
            texturePrimitiveData.vertexData[9] = xPos + (fontSize * TextBuilder.getRealLetterSize(Integer.toString(digitOfNumber).charAt(0)));
        }
    }
}