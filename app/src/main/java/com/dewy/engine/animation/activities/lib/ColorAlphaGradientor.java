package com.dewy.engine.animation.activities.lib;

import android.util.Log;

/**
 * Created by dewyone on 2015-10-27.
 */
public class ColorAlphaGradientor {
    private static final String TAG  = "ColorAlphaGradientor";
    private ImageButtoner11 imageButtoner;

    private boolean activated = false;

    private float colorAlpha;

    private float currentColorAlpha;
    private boolean colorAlphaGradient = false;
    private float colorAlphaDelta;       // delta per a second
    private float startColorAlpha;
    private float endColorAlpha;
    private long colorAlphaLastTime;

    private boolean drawOnClick_colorAlphaGradient = false;
    private float drawOnClick_currentColorAlpha;
    private float drawOnClick_colorAlphaDelta;       // delta per a second
    private float drawOnClick_startColorAlpha;
    private float drawOnClick_endColorAlpha;
    private long drawOnClick_colorAlphaLastTime;

    public ColorAlphaGradientor() {
    }

    public void activate() {
        activated = true;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setImageButtoner(ImageButtoner11 imageButtoner) {
        this.imageButtoner = imageButtoner;
        colorAlpha = imageButtoner.getColorAlpha();
    }
    public void setDrawInfo(float startColorAlpha, float endColorAlpha, float durationInSecond) {

        //if (imageButtoner == null) return;

        // start alpha, end alpha, duration
        colorAlphaGradient = true;
        this.startColorAlpha = startColorAlpha;
        this.currentColorAlpha = startColorAlpha;
        this.endColorAlpha = endColorAlpha;
        colorAlphaDelta = (startColorAlpha - endColorAlpha) / durationInSecond;     // alpha delta of color per a second
        colorAlphaLastTime = System.nanoTime();

        //imageButtoner.setColorAlpha(startColorAlpha);
    }

    public void setDrawInfoOnClick(float startColorAlpha, float endColorAlpha, float durationInSecond) {
        // start alpha, end alpha, duration
        drawOnClick_colorAlphaGradient = true;
        this.drawOnClick_startColorAlpha = startColorAlpha;
        this.drawOnClick_currentColorAlpha =startColorAlpha;
        this.drawOnClick_endColorAlpha = endColorAlpha;
        drawOnClick_colorAlphaDelta = (startColorAlpha - endColorAlpha) / durationInSecond;     // alpha delta of color per a second
        drawOnClick_colorAlphaLastTime = System.nanoTime();
    }

    public void gradient() {
        String logMessage;
        //logMessage = "gradient() is called";
        //Log.i(TAG, logMessage);

        // assume that colorAlpha decreases as time goes
        //if (colorAlpha == 0 && ! drawOnClick_colorAlphaGradient) return;
        if (colorAlpha == 0) return;

        float colorAlpha = imageButtoner.getColorAlpha();       // set default color alpha

        /* if we need to change color alpha, change it on the circumstance*/
        if (drawOnClick_colorAlphaGradient) {
            colorAlpha = drawOnClick_currentColorAlpha;

            if (drawOnClick_currentColorAlpha <= drawOnClick_endColorAlpha) drawOnClick_colorAlphaGradient = false;

            float elapsedTime = System.nanoTime() - drawOnClick_colorAlphaLastTime;
            // elapsed time per a second * variation of alpha of color per a second
            drawOnClick_currentColorAlpha -= (elapsedTime / 1000000000l) * drawOnClick_colorAlphaDelta;
            drawOnClick_colorAlphaLastTime = System.nanoTime();
        }
        else if (colorAlphaGradient) {
            //logMessage = "colorAlphaGradienting..";
            //Log.i(TAG, logMessage);
            //colorAlpha = this.colorAlpha;
            colorAlpha = currentColorAlpha;

            if (currentColorAlpha <= endColorAlpha) colorAlphaGradient = false;

            float elapsedTime = System.nanoTime() - colorAlphaLastTime;
            // elapsed time per a second * variation of alpha of color per a second
            currentColorAlpha -= (elapsedTime / 1000000000l) * colorAlphaDelta;
            colorAlphaLastTime = System.nanoTime();
        }

        this.colorAlpha = colorAlpha;
        imageButtoner.setColorAlpha(colorAlpha);
    }
}