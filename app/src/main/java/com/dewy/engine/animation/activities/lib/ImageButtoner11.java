package com.dewy.engine.animation.activities.lib;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

import com.dewy.engine.animation.activities.view.TexAlphaRectRenderer;
import com.dewy.engine.data.VertexBuffer;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.primitives.ObjectBuilder;
import com.dewy.engine.primitives.PrimitiveData;
import com.dewy.engine.util.Geometry;
import com.dewy.engine.util.MatrixHelper;

import java.nio.FloatBuffer;

/**
 * Created by dewyone on 2015-10-23.
 */
public class ImageButtoner11 {
    private static final String TAG = "ImageButtoner11";

    public static final int POSITION_COMPONENT_COUNT = 3;
    public final int UV_COMPONENT_COUNT = 2;
    private final GLContext glContext;
    private final Context context;

    private final TexAlphaRectRenderer rectFirst;
    private final TexAlphaRectRenderer rectSec;

    private ColorAlphaGradientor colorAlphaGradientor;
    private Sounder sounder;

    // maybe in the future, this value could be properties
    public float rectWidth;
    public float rectHeight;
    private final float centerX;
    private final float centerY;
    private float leftX;
    private float rightX;
    private float topY;
    private float bottomY;
    private static final int FirstRect = 0;
    private static final int SecRect = 1;

    private float[][] uvData = new float[2][];
    private float TextWidth = 0.5f;
    private float colorAlpha = 1.0f;

    // control variables
    private boolean buttonDown = false;
    public boolean isButtonDown() {
        return buttonDown;
    }
    private boolean buttonClicked = false;
    public boolean isButtonClicked() {
        return buttonClicked;
    }
    public void setButtonClicked(boolean buttonClicked) {
        this.buttonClicked = buttonClicked;
    }

    private PrimitiveData[] rectanglePrimi;
    private FloatBuffer[] vertexBuffer;
    private FloatBuffer[] uvBuffer;

    public ImageButtoner11(GLContext glContext, float x, float y, float rectWidth, float rectHeight, int textureUnitNumber) {

        this.glContext = glContext;
        context = glContext.getContext();
        centerX = x;
        centerY = y;
        this.rectWidth = rectWidth;
        this.rectHeight = rectHeight;
        leftX = centerX - (rectWidth / 2);      // normalized coords
        rightX = centerX + (rectWidth / 2);
        topY = centerY + (rectWidth / 2);
        bottomY = centerY - (rectWidth / 2);

        rectanglePrimi = new PrimitiveData[2];
        rectanglePrimi[FirstRect] = ObjectBuilder.createRectangle(new Geometry.Point(x, y, 0), new Geometry.Vector(0, 0, 1), rectWidth, rectHeight);
        rectanglePrimi[SecRect] = ObjectBuilder.createRectangle(new Geometry.Point(x, y, 0), new Geometry.Vector(0, 0, 1), rectWidth, rectHeight);

        // get Vertex
        vertexBuffer = new FloatBuffer[2];
        vertexBuffer[FirstRect] = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi[FirstRect].vertexData);
        vertexBuffer[SecRect] = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi[SecRect].vertexData);

        // get UVs, read two texts from a texture unit
        uvData[FirstRect] = getUvDataFirst();
        uvData[SecRect] = getUvDataSec();

        uvBuffer = new FloatBuffer[2];
        uvBuffer[FirstRect] = VertexBuffer.arrayAsVertexBuffer(uvData[FirstRect]);
        uvBuffer[SecRect] = VertexBuffer.arrayAsVertexBuffer( uvData[SecRect] );

        rectFirst = new TexAlphaRectRenderer(glContext, textureUnitNumber);
        rectSec = new TexAlphaRectRenderer(glContext, textureUnitNumber);

        rectFirst.setRectDrawingInfo(rectanglePrimi[FirstRect], uvBuffer[FirstRect], colorAlpha);
        rectSec.setRectDrawingInfo(rectanglePrimi[SecRect], uvBuffer[SecRect], colorAlpha);
    }

    public void setColorAlpha(float colorAlpha) {
        this.colorAlpha = colorAlpha;

        rectFirst.setRectDrawingInfo(colorAlpha);
        rectSec.setRectDrawingInfo(colorAlpha);
    }

    public float getColorAlpha() {
        return colorAlpha;
    }

    public void attachColorAlphaGradientor(ColorAlphaGradientor colorAlphaGradientor) {
        colorAlphaGradientor.setImageButtoner(this);
        this.colorAlphaGradientor = colorAlphaGradientor;
    }

    public void detachColorAlphaGradientor() {
        colorAlphaGradientor = null;
    }

    public void attachClickSounder(Sounder sounder) {
        this.sounder = sounder;
    }

    public void detachClickSounder() { sounder = null;}

    public void rotate(float angle) {
        MatrixHelper.rotateRectVertexData(rectanglePrimi[FirstRect].vertexData, centerX, centerY, angle);
        MatrixHelper.rotateRectVertexData(rectanglePrimi[SecRect].vertexData, centerX, centerY, angle);

        vertexBuffer[FirstRect] = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi[FirstRect].vertexData);
        vertexBuffer[SecRect] = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi[SecRect].vertexData);

        rectFirst.setRectDrawingInfo(rectanglePrimi[FirstRect]);
        rectSec.setRectDrawingInfo(rectanglePrimi[SecRect]);
    }

    public void draw() {

        if (colorAlphaGradientor != null) colorAlphaGradientor.gradient();

        //rectSec.draw();
        if (!buttonDown) rectFirst.draw();
        else rectSec.draw();
    }

    public void onTouchEvent(MotionEvent motionEvent) {
        onTouchEvent01(motionEvent);
    }

    private void onTouchEvent01(MotionEvent motionEvent) {
        float screenX = motionEvent.getX();
        float screenY = motionEvent.getY();
        float x = (screenX / glContext.getScreenWidth()) * 2 - 1;
        float y = -((screenY / glContext.getScreenHeight()) * 2) + 1;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE :
                break;
            case  MotionEvent.ACTION_DOWN :
                String logMessage;
                if ( x > leftX && x < rightX && y < topY && y > bottomY) {
                    logMessage = "button is clicked (ACTION_DOWN)..";
                    Log.i(TAG, logMessage);

                    buttonDown = true;

                    if (buttonClicked) buttonClicked = false;

                    if (colorAlphaGradientor != null) colorAlphaGradientor.activate();
                    if (sounder != null) {
                        logMessage = "sounder plays sound..";
                        Log.i(TAG, logMessage);
                        sounder.play();
                    }
                }
                break;
            case MotionEvent.ACTION_UP :

                if ( x > leftX && x < rightX && y < topY && y > bottomY) {
                    logMessage = "button is clicked (ACTION_UP)..";
                    Log.i(TAG, logMessage);

                    if (buttonDown) {
                        buttonClicked = true;
                        logMessage = "buttonClicked is true";
                        Log.i(TAG, logMessage);
                    }
                }

                if (buttonDown) buttonDown = false;

                break;
        }
    }

    private float[] getUvDataFirst() {
        float startX = 0;
        float[] uvData = {
                startX, 0f,
                startX, 1f,
                startX + TextWidth, 1f,
                startX + TextWidth, 0f
        };

        return uvData;
    }

    private float[] getUvDataSec() {
        float startX = TextWidth;
        float[] uvData = {
                startX, 0f,
                startX, 1f,
                startX + TextWidth, 1f,
                startX + TextWidth, 0f
        };

        return uvData;
    }
}