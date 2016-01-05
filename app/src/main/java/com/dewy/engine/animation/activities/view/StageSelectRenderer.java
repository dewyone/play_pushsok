package com.dewy.engine.animation.activities.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.dewy.engine.R;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.primitives.Drawable;
import com.dewy.engine.primitives.PrimitiveData;
import com.dewy.engine.renderer.BaseRenderer20;
import com.dewy.engine.shader_programs.TextureShaderProgram;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

/**
 * Created by dewyone on 2015-08-30.
 */
public class StageSelectRenderer extends BaseRenderer20 {
    private static final String TAG = "StageSelectRenderer";

    public static final int POSITION_COMPONENT_COUNT = 3;
    private final int UV_COMPONENT_COUNT = 2;
    private final Context context;
    private final TextureShaderProgram textureShaderProgram;

    private static final int TotalRect = 4;
    private static final int FirstRect = 0;
    private static final int SecRect = 1;
    private static final int ThirdRect = 2;
    private static final int FourthRect = 3;

    private PrimitiveData[] rectanglePrimi;
    private FloatBuffer[] vertexBuffer;
    private FloatBuffer[] uvBuffer;

    public StageSelectRenderer(GLContext GLContext) {
        super(GLContext);

        context = GLContext.getContext();
        textureShaderProgram = new TextureShaderProgram(context);

        rectanglePrimi = new PrimitiveData[TotalRect];
        vertexBuffer = new FloatBuffer[TotalRect];
        uvBuffer = new FloatBuffer[TotalRect];

        initTextureUnit();
    }

    public void setRectDrawingInfo(int rectNumber, PrimitiveData primitiveData, FloatBuffer vertexBuffer, FloatBuffer uvBuffer) {
        rectanglePrimi[rectNumber] = primitiveData;
        this.vertexBuffer[rectNumber] = vertexBuffer;
        this.uvBuffer[rectNumber] = uvBuffer;
    }

    public void draw() {
        transitModel();

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        textureShaderProgram.useProgram();
        // number 0 - textureUnitNumber 0, number 1 - textureUnitNumber 1....
        //textureShaderProgram.setUniforms(modelViewProjectionMatrix, firstDrawNumber);     // 2 : draw number 2
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, 0);
        bindData(FirstRect);
        draw(rectanglePrimi[FirstRect].drawableList);

        textureShaderProgram.useProgram();
        //textureShaderProgram.setUniforms(modelViewProjectionMatrix, lastDrawNumber);     // 3 : draw number 3
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, 0);
        bindData(SecRect);
        draw( rectanglePrimi[SecRect].drawableList);

        textureShaderProgram.useProgram();
        // number 0 - textureUnitNumber 0, number 1 - textureUnitNumber 1....
        //textureShaderProgram.setUniforms(modelViewProjectionMatrix, firstDrawNumber);     // 2 : draw number 2
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, 0);
        bindData(ThirdRect);
        draw(rectanglePrimi[ThirdRect].drawableList);

        textureShaderProgram.useProgram();
        //textureShaderProgram.setUniforms(modelViewProjectionMatrix, lastDrawNumber);     // 3 : draw number 3
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, 0);
        bindData(FourthRect);
        draw( rectanglePrimi[FourthRect].drawableList);

        GLES20.glDisableVertexAttribArray(textureShaderProgram.getPositionAttributeLocation());
        GLES20.glDisableVertexAttribArray(textureShaderProgram.getaTextureCoordinates());
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    private void bindData(int drawNumber) {
        textureShaderProgram.setVertexAttribPointer(
                textureShaderProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, 0, vertexBuffer[drawNumber], 0);
        textureShaderProgram.setVertexAttribPointer(
                textureShaderProgram.getaTextureCoordinates(), UV_COMPONENT_COUNT, 0, uvBuffer[drawNumber], 0);
    }

    private void draw(List<Drawable> drawableList) {
        for (Drawable drawable : drawableList) {
            drawable.draw();
        }
    }

    private void initTextureUnit() {
        int [] maxTextureNumber = new int[1];
        IntBuffer maxTextureNumberBuffer = IntBuffer.wrap(maxTextureNumber);
        GLES20.glGetIntegerv( GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS , maxTextureNumberBuffer);
        String logMessage = " GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS : " + maxTextureNumberBuffer.get(0);
        Log.i(TAG, logMessage);

        int textureUnitCount = 1;
        int baseTextureUnitConst = GLES20.GL_TEXTURE0;

        int imageID = R.drawable.number_atlas_rasterized_1280_128;

        //int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);



        int textureUnitConstTobeUsed = baseTextureUnitConst;
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

    }

    protected void transitModel() {
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
    }
}