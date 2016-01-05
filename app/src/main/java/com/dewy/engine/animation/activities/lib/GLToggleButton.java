package com.dewy.engine.animation.activities.lib;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.dewy.engine.animation.activities.view.TexAlphaRectRenderer;
import com.dewy.engine.data.VertexBuffer;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.primitives.ObjectBuilder;
import com.dewy.engine.primitives.PrimitiveData;
import com.dewy.engine.util.Geometry;
import com.dewy.engine.util.MatrixHelper;

import java.nio.FloatBuffer;

/**
 * Created by dewyone on 2015-11-04.
 */
public class GLToggleButton {
    private static final String TAG = "GLToggleButton";

    public static final int POSITION_COMPONENT_COUNT = 3;
    public final int UV_COMPONENT_COUNT = 2;
    private final GLContext glContext;
    private final Context context;

    int totalRndrr = 3;       // total renderer used
    private final TexAlphaRectRenderer onRenderer;
    private final TexAlphaRectRenderer offRenderer;
    private final TexAlphaRectRenderer pressedRenderer;

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
    private static final int OnRendererID = 0;
    private static final int OffRendererID = 1;
    private static final int PressedRendererID = 2;

    private float[][] uvData = new float[totalRndrr][];
    private float TextWidth = 1.0f / totalRndrr;
    private float colorAlpha = 1.0f;

    // control variables
    private boolean toggleOn = true;
    public boolean isToggleOn() {
        return toggleOn;
    }
    public void setToggleOn(boolean toggleOn) {
        this.toggleOn = toggleOn;
    }
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

    public GLToggleButton(GLContext glContext, float x, float y, float rectWidth, float rectHeight, int textureUnitNumber) {

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

        rectanglePrimi = new PrimitiveData[totalRndrr];
        rectanglePrimi[OnRendererID] = ObjectBuilder.createRectangle(new Geometry.Point(x, y, 0), new Geometry.Vector(0, 0, 1), rectWidth, rectHeight);
        rectanglePrimi[OffRendererID] = ObjectBuilder.createRectangle(new Geometry.Point(x, y, 0), new Geometry.Vector(0, 0, 1), rectWidth, rectHeight);
        rectanglePrimi[PressedRendererID] = ObjectBuilder.createRectangle(new Geometry.Point(x, y, 0), new Geometry.Vector(0, 0, 1), rectWidth, rectHeight);

        // get Vertex
        vertexBuffer = new FloatBuffer[totalRndrr];
        vertexBuffer[OnRendererID] = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi[OnRendererID].vertexData);
        vertexBuffer[OffRendererID] = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi[OffRendererID].vertexData);
        vertexBuffer[PressedRendererID] = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi[PressedRendererID].vertexData);

        // get UVs, read two texts from a texture unit
        uvData[OnRendererID] = getUvData(1);        // according to image info ( onofftoggle.png)
        uvData[OffRendererID] = getUvData(2);
        uvData[PressedRendererID] = getUvData(3);

        uvBuffer = new FloatBuffer[totalRndrr];
        uvBuffer[OnRendererID] = VertexBuffer.arrayAsVertexBuffer(uvData[OnRendererID]);
        uvBuffer[OffRendererID] = VertexBuffer.arrayAsVertexBuffer(uvData[OffRendererID]);
        uvBuffer[PressedRendererID] = VertexBuffer.arrayAsVertexBuffer( uvData[PressedRendererID] );

        onRenderer = new TexAlphaRectRenderer(glContext, textureUnitNumber);
        offRenderer = new TexAlphaRectRenderer(glContext, textureUnitNumber);
        pressedRenderer = new TexAlphaRectRenderer(glContext, textureUnitNumber);

        onRenderer.setRectDrawingInfo(rectanglePrimi[OnRendererID], uvBuffer[OnRendererID], colorAlpha);
        offRenderer.setRectDrawingInfo(rectanglePrimi[OffRendererID], uvBuffer[OffRendererID], colorAlpha);
        pressedRenderer.setRectDrawingInfo(rectanglePrimi[PressedRendererID], uvBuffer[PressedRendererID], colorAlpha);
    }

    public void setColorAlpha(float colorAlpha) {
        this.colorAlpha = colorAlpha;

        onRenderer.setRectDrawingInfo(colorAlpha);
        pressedRenderer.setRectDrawingInfo(colorAlpha);
    }

    public float getColorAlpha() {
        return colorAlpha;
    }

    public void attachColorAlphaGradientor(ColorAlphaGradientor colorAlphaGradientor) {
        //colorAlphaGradientor.setImageButtoner(this);
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
        MatrixHelper.rotateRectVertexData(rectanglePrimi[OnRendererID].vertexData, centerX, centerY, angle);
        MatrixHelper.rotateRectVertexData(rectanglePrimi[PressedRendererID].vertexData, centerX, centerY, angle);

        vertexBuffer[OnRendererID] = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi[OnRendererID].vertexData);
        vertexBuffer[PressedRendererID] = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi[PressedRendererID].vertexData);

        onRenderer.setRectDrawingInfo(rectanglePrimi[OnRendererID]);
        pressedRenderer.setRectDrawingInfo(rectanglePrimi[PressedRendererID]);
    }

    public void draw() {

        //if (colorAlphaGradientor != null) colorAlphaGradientor.gradient();

        if (! buttonDown) {
            if (isToggleOn()) onRenderer.draw();
            else offRenderer.draw();
        } else {
            pressedRenderer.draw();
        }
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

                    toggleOn = ! toggleOn;
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

                        //toggleOn = ! toggleOn;
                    }
                }

                if (buttonDown) buttonDown = false;

                break;
        }
    }

    private float[] getUvData(int order) {
        float startX = (order - 1) * TextWidth;
        float[] uvData = {
                startX, 0f,
                startX, 1f,
                startX + TextWidth, 1f,
                startX + TextWidth, 0f
        };

        return uvData;
    }
}
