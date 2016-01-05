package com.dewy.engine.animation.activities.lib;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.activities.view.TextureRectRenderer;
import com.dewy.engine.data.VertexBuffer;
import com.dewy.engine.primitives.ObjectBuilder;
import com.dewy.engine.primitives.PrimitiveData;
import com.dewy.engine.util.Geometry;

import java.nio.FloatBuffer;

/**
 * Created by dewyone on 2015-09-26.
 *
 * Receive : The position to place the button, ( the position is left-up corner of rectangle)
 *              The Image texture.
 */
public class ImageButtoner {
    private static final String TAG = "ImageButtoner";

    public static final int POSITION_COMPONENT_COUNT = 3;
    public final int UV_COMPONENT_COUNT = 2;
    private final GLContext glContext;
    private final Context context;

    private final TextureRectRenderer rectFirst;
    private final TextureRectRenderer rectSec;

    // maybe in the future, this value could be properties
    public static final float rectWidth = 0.5f;
    public static final float rectHeight = 0.25f;
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

    public ImageButtoner(GLContext glContext, float x, float y, int textureUnitNumber) {

        this.glContext = glContext;
        context = glContext.getContext();
        centerX = x;
        centerY = y;
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

        rectFirst = new TextureRectRenderer(glContext, textureUnitNumber);
        rectSec = new TextureRectRenderer(glContext, textureUnitNumber);

        rectFirst.setRectDrawingInfo(rectanglePrimi[FirstRect], uvBuffer[FirstRect]);
        rectSec.setRectDrawingInfo(rectanglePrimi[SecRect], uvBuffer[SecRect]);
    }

    public void draw() {
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
                String logMessage = "ImageButtoner.Action_Down";
                Log.i(TAG, logMessage);
                if ( x > leftX && x < rightX && y < topY && y > bottomY) {
                    logMessage = "button is clicked (ACTION_DOWN)..";
                    Log.i(TAG, logMessage);

                    buttonDown = true;

                    if (buttonClicked) buttonClicked = false;
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

                    buttonDown = false;
                }
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
