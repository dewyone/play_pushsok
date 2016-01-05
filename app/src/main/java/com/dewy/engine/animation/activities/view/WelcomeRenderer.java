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
import com.dewy.engine.data.VertexBuffer;
import com.dewy.engine.primitives.Drawable;
import com.dewy.engine.primitives.ObjectBuilder;
import com.dewy.engine.primitives.PrimitiveData;
import com.dewy.engine.renderer.BaseRenderer20;
import com.dewy.engine.shader_programs.TextureShaderProgram;
import com.dewy.engine.util.Geometry;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

/**
 * Created by dewyone on 2015-09-01.
 */
public class WelcomeRenderer extends BaseRenderer20 {
    private static final String TAG = "WelcomeRenderer";

    public static final int POSITION_COMPONENT_COUNT = 3;
    private final int UV_COMPONENT_COUNT = 2;

    private final Context context;
    private final TextureShaderProgram textureShaderProgram;
    private final int imageID = R.drawable.push_push_title;

    private PrimitiveData rectangleData;
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawOrderBuffer;
    private FloatBuffer uvBuffer;

    private int textureUnitNumber;

    public WelcomeRenderer(GLContext GLContext) {
        super(GLContext);

        context = GLContext.getContext();
        String logMessage = "is context null : " + (context == null);
        Log.i(TAG, logMessage);
        textureShaderProgram = new TextureShaderProgram(context);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageID);
        float divider = Math.max( bitmap.getWidth(), bitmap.getHeight());
        float imageWidth = (float) bitmap.getWidth() /divider;
        float imageHeight = (float) bitmap.getHeight() / divider;
        bitmap.recycle();
        logMessage = "imageWidth : " + imageWidth + ",   imageHeight : " + imageHeight;
        Log.i(TAG, logMessage);
        rectangleData = ObjectBuilder.createRectangle( new Geometry.Point(0, 0, 0), new Geometry.Vector(0, 0, 1), imageWidth, imageHeight);

        // get Vertex
        vertexBuffer = VertexBuffer.arrayAsVertexBuffer(rectangleData.vertexData);
        //vertexBuffer.position(0);

        // get UVs
        uvBuffer = VertexBuffer.arrayAsVertexBuffer( getUvData());
        //uvBuffer.position(0);
        textureUnitNumber = initTextureUnit();
    }

    public void draw() {
        //String logMessage = "drawing..";
        //Log.i(TAG, logMessage);

        transitModel();

        // For transparent background and others
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        textureShaderProgram.useProgram();
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, textureUnitNumber);
        bindData();
        draw(rectangleData.drawableList);

        GLES20.glDisableVertexAttribArray(textureShaderProgram.getPositionAttributeLocation());
        GLES20.glDisableVertexAttribArray(textureShaderProgram.getaTextureCoordinates());
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    private void bindData() {
        textureShaderProgram.setVertexAttribPointer(
                textureShaderProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, 0, vertexBuffer, 0);
        textureShaderProgram.setVertexAttribPointer(
                textureShaderProgram.getaTextureCoordinates(), UV_COMPONENT_COUNT, 0, uvBuffer, 0);
    }

    private void draw(List<Drawable> drawableList) {
        for (Drawable drawable : drawableList) {
            drawable.draw();
        }
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

    protected void transitModel() {
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
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