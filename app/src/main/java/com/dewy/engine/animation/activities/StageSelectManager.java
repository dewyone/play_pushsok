package com.dewy.engine.animation.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.dewy.engine.R;
import com.dewy.engine.animation.env.Skbl;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.SkbIntent;
import com.dewy.engine.animation.activities.lib.ImageButtoner;
import com.dewy.engine.animation.activities.lib.ImageSpinner;
import com.dewy.engine.animation.activities.view.TextureRectRenderer;

import java.nio.IntBuffer;

/**
 * Created by dewyone on 2015-09-24.
 */
public class StageSelectManager {
    private static final String TAG = "StageSelectManager";

    private final GLActivity platform;
    private final GLContext GLContext;
    private final Context context;

    public final ImageSpinner imageSpinnerSec;
    public final ImageSpinner imageSpinnerFirst;
    //public final TextureRectRenderer playRectRenderer;
    public final ImageButtoner playButtoner;
    private final TextureRectRenderer selectStageTxRenderer;

    private int textureUnitNumberForNumber;
    private int textureUnitNumberForPlay;
    private int txUnitNForSelectSage;

    private final float SSFontSize = 0.1f;
    private final float SelectStageTextPosX = 0f;
    private final float SelectStageTextPosY = 0.2f + (ImageSpinner.rectHeight / 2);
    private final float SelectStageTextWidth = 12 * SSFontSize + 0.02f;
    private final float SelectStageHeight = 1 * SSFontSize + 0.02f;

    private final float PlayTextPosX = 0.5f;
    private final float PlayTextPosY = -0.6f;
    private final float PlayTextWidth = 0.5f;
    private final float PlayTextHeight = 0.25f;
    private final float PlayTextLeftX = PlayTextPosX;
    private final float PlayTextRightX = PlayTextPosX + PlayTextWidth;
    private final float PlayTextTopY = PlayTextPosY;
    private final float PlayTextBottomY = PlayTextPosY - PlayTextHeight;

    public StageSelectManager(GLContext GLContext, GLActivity platform) {

        this.platform = platform;
        this.GLContext = GLContext;
        context = GLContext.getContext();

        textureUnitNumberForNumber = initTextureUnit();
        textureUnitNumberForPlay = initTextureUnitForPlay();
        txUnitNForSelectSage = initTextureUnitForSelectStage();

        imageSpinnerSec = new ImageSpinner(GLContext, -(ImageSpinner.rectWidth / 2), 0, textureUnitNumberForNumber);
        imageSpinnerFirst = new ImageSpinner(GLContext, ImageSpinner.rectWidth / 2, 0, textureUnitNumberForNumber);
        //playRectRenderer = new TextureRectRenderer(glContext, PlayTextPosX, PlayTextPosY,
        //        PlayTextWidth, PlayTextHeight, textureUnitNumberForPlay);
        //playRectRenderer.setRectDrawingInfo(getUvDataForPlay());
        playButtoner = new ImageButtoner(GLContext, PlayTextPosX, PlayTextPosY, textureUnitNumberForPlay);

        selectStageTxRenderer = new TextureRectRenderer(GLContext, SelectStageTextPosX, SelectStageTextPosY,
                SelectStageTextWidth, SelectStageHeight, txUnitNForSelectSage);
        selectStageTxRenderer.setRectDrawingInfo(getUvDataForSelectStage());

        String logMessage = "PlayText Left : " + PlayTextLeftX + ", Right : " + PlayTextRightX;
        Log.i(TAG, logMessage);
        logMessage = "PlayText Top : " + PlayTextTopY + ", Bottom : " + PlayTextBottomY;
        Log.i(TAG, logMessage);
    }

    public void draw() {
        GLES20.glClearColor(0, 0.1f, 0.2f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        //GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        imageSpinnerSec.draw();
        imageSpinnerFirst.draw();
        playButtoner.draw();
        selectStageTxRenderer.draw();

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    public void onTouchEvent(MotionEvent motionEvent) {
        String logMessage;

        // spinner
        imageSpinnerSec.onTouchEvent(motionEvent);
        imageSpinnerFirst.onTouchEvent(motionEvent);

        // play button
        playButtoner.onTouchEvent(motionEvent);

        if (playButtoner.isButtonClicked()) {
            playButtoner.setButtonClicked(false);

            int secNumber = imageSpinnerSec.getRepresentingNumber();
            int firstNumber = imageSpinnerFirst.getRepresentingNumber();
            int stageNumber = secNumber * 10 + firstNumber;

            logMessage = "***   play is clicked (ACTION_UP)...   ***";
            Log.i(TAG, logMessage);
            if (stageNumber > 0 && stageNumber <= StageSelector.MAXSTAGE) {
                //startPlay
                logMessage = "got stage number : " + stageNumber;
                Log.i(TAG, logMessage);

                SkbIntent intent = new SkbIntent(null, PlaySokoban.class);
                Bundle bundle = new Bundle();
                bundle.putInt(Skbl.STAGENUMBER, stageNumber);
                bundle.putBoolean(Skbl.NEWSTAGE, true);
                intent.setBundle(bundle);
                platform.startActivity(intent);
            }
        }

    }

    private int initTextureUnit() {
        int [] maxTextureNumber = new int[1];
        IntBuffer maxTextureNumberBuffer = IntBuffer.wrap(maxTextureNumber);
        GLES20.glGetIntegerv(GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, maxTextureNumberBuffer);
        String logMessage = " GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS : " + maxTextureNumberBuffer.get(0);
        Log.i(TAG, logMessage);

        int textureUnitCount = 1;
        int textureUnitConst = GLES20.GL_TEXTURE0;
        int textureUnitNumber = textureUnitConst - GLES20.GL_TEXTURE0;

        int imageID = R.drawable.number_atlas_rasterized_1280_128;

        //int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int textureUnitConstTobeUsed = textureUnitConst;
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
        int [] maxTextureNumber = new int[1];
        IntBuffer maxTextureNumberBuffer = IntBuffer.wrap(maxTextureNumber);
        GLES20.glGetIntegerv(GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, maxTextureNumberBuffer);
        String logMessage = " GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS : " + maxTextureNumberBuffer.get(0);
        Log.i(TAG, logMessage);

        int textureUnitCount = 1;
        int textureUnitConst = GLES20.GL_TEXTURE1;
        int textureUnitNumber = textureUnitConst - GLES20.GL_TEXTURE0;

        int imageID = R.drawable.play_clickable02;

        //int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int textureUnitConstTobeUsed = textureUnitConst;
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

    private int initTextureUnitForSelectStage() {
        int [] maxTextureNumber = new int[1];
        IntBuffer maxTextureNumberBuffer = IntBuffer.wrap(maxTextureNumber);
        GLES20.glGetIntegerv(GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, maxTextureNumberBuffer);
        String logMessage = " GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS : " + maxTextureNumberBuffer.get(0);
        Log.i(TAG, logMessage);

        int textureUnitCount = 1;
        int textureUnitConst = GLES20.GL_TEXTURE2;
        int textureUnitNumber = textureUnitConst - GLES20.GL_TEXTURE0;

        int imageID = R.drawable.choose_stage;

        //int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int textureUnitConstTobeUsed = textureUnitConst;
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

    private float [] getUvDataForPlay() {
        float [] uvData = new float[]{
                0, 0,
                0, 1,
                1, 1,
                1, 0
        };

        return uvData;
    }

    private float [] getUvDataForSelectStage() {
        float [] uvData = new float[]{
                0, 0,
                0, 1,
                1, 1,
                1, 0
        };

        return uvData;
    }
}