package com.dewy.engine.animation.activities.lib;

/**
 * Created by dewyone on 2015-10-27.
 */
public class ImageButtonAlphaGradientor {

    private final ImageButtoner11 imageButtoner;

    private float colorAlpha;
    private boolean colorAlphaGradient = false;
    private float colorAlphaDelta;       // delta per a second
    private float startColorAlpha;
    private float endColorAlpha;
    private long colorAlphaLastTime;

    private boolean drawOnClick_colorAlphaGradient = false;
    private float drawOnClick_colorAlpha;
    private float drawOnClick_colorAlphaDelta;       // delta per a second
    private float drawOnClick_startColorAlpha;
    private float drawOnClick_endColorAlpha;
    private long drawOnClick_colorAlphaLastTime;

    public ImageButtonAlphaGradientor(ImageButtoner11 imageButtoner) {
        this.imageButtoner = imageButtoner;
        colorAlpha = imageButtoner.getColorAlpha();
    }

    public void setDrawInfo(float startColorAlpha, float endColorAlpha, float durationInSecond) {
        // start alpha, end alpha, duration
        colorAlphaGradient = true;
        this.startColorAlpha = startColorAlpha;
        this.endColorAlpha = endColorAlpha;
        colorAlphaDelta = (startColorAlpha - endColorAlpha) / durationInSecond;     // alpha delta of color per a second
        colorAlphaLastTime = System.nanoTime();
    }

    public void setDrawInfoOnClick(float startColorAlpha, float endColorAlpha, float durationInSecond) {
        // start alpha, end alpha, duration
        drawOnClick_colorAlphaGradient = true;
        this.drawOnClick_startColorAlpha = startColorAlpha;
        this.drawOnClick_endColorAlpha = endColorAlpha;
        drawOnClick_colorAlphaDelta = (startColorAlpha - endColorAlpha) / durationInSecond;     // alpha delta of color per a second
        drawOnClick_colorAlphaLastTime = System.nanoTime();
    }

    public void draw() {

        if (colorAlpha == 0 && ! drawOnClick_colorAlphaGradient) return;

        float colorAlpha = imageButtoner.getColorAlpha();       // set default color alpha

        /* if we need to change color alpha, change it on the circumstance*/
        if (drawOnClick_colorAlphaGradient) {
            colorAlpha = drawOnClick_colorAlpha;

            if (drawOnClick_endColorAlpha <= drawOnClick_colorAlpha) drawOnClick_colorAlphaGradient = false;

            float elapsedTime = System.nanoTime() - drawOnClick_colorAlphaLastTime;
            // elapsed time per a second * variation of alpha of color per a second
            drawOnClick_colorAlpha -= (elapsedTime / 1000000000l) * drawOnClick_colorAlphaDelta;
        }
        else if (colorAlphaGradient) {
            colorAlpha = this.colorAlpha;

            if (endColorAlpha <= this.colorAlpha) colorAlphaGradient = false;

            float elapsedTime = System.nanoTime() - colorAlphaLastTime;
            // elapsed time per a second * variation of alpha of color per a second
            this.colorAlpha -= (elapsedTime / 1000000000l) * colorAlphaDelta;
        }

        /* set the result alpha*/
        imageButtoner.setColorAlpha(colorAlpha);
        imageButtoner.draw();
    }
}