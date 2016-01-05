package com.dewy.engine.text;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.view.unit.TexturePrimitiveData;
import com.dewy.engine.data.VertexBuffer;
import com.dewy.engine.primitives.Drawable;
import com.dewy.engine.renderer.BaseRenderer20;
import com.dewy.engine.shader_programs.TextureShaderProgram;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

/**
 * Created by dewyone on 2015-08-18.
 */
public class TextRenderer extends BaseRenderer20 {

    public static final int POSITION_COMPONENT_COUNT = 3;
    private final int UV_COMPONENT_COUNT = 2;

    private final Context context;
    private final TextureShaderProgram textureShaderProgram;

    private TexturePrimitiveData texturePrimitiveData;
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawOrderBuffer;
    private FloatBuffer uvBuffer;

    private int textureUnitNumber;

    public TextRenderer(GLContext GLContext) {
        super(GLContext);

        context = GLContext.getContext();
        textureShaderProgram = new TextureShaderProgram(context);

        String message = "ABCDE";
        texturePrimitiveData = TextBuilder.createString(message, -0.5f, 0.5f, 0.1f);

        // get Vertex
        vertexBuffer = VertexBuffer.arrayAsVertexBuffer(texturePrimitiveData.vertexData);
        //vertexBuffer.position(0);

        // get Draw Order
        drawOrderBuffer = VertexBuffer.arrayAsShortBuffer(texturePrimitiveData.drawOrderData);
        drawOrderBuffer.position(0);

        // get UVs
        uvBuffer = VertexBuffer.arrayAsVertexBuffer(texturePrimitiveData.uvData);
        //uvBuffer.position(0);
        textureUnitNumber = initTextureUnit();
    }

    public void draw() {
        transitModel();

        //GLES20.glEnable(GLES20.GL_BLEND);
        //GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        textureShaderProgram.useProgram();
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, textureUnitNumber);
        bindData();
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrderBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, drawOrderBuffer);

        GLES20.glDisableVertexAttribArray(textureShaderProgram.getPositionAttributeLocation());
        GLES20.glDisableVertexAttribArray(textureShaderProgram.getaTextureCoordinates());
        //GLES20.glDisable(GLES20.GL_BLEND);
    }

    public void draw(FloatBuffer vertexBuffer, ShortBuffer drawOrderBuffer, FloatBuffer uvBuffer) {
        this.vertexBuffer = vertexBuffer;
        this.drawOrderBuffer = drawOrderBuffer;
        this.uvBuffer = uvBuffer;
        draw();
    }

    private void bindData() {
        bindPositionData();
        bindUVData();
    }

    private void bindPositionData() {
        textureShaderProgram.setVertexAttribPointer(
                textureShaderProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, 0, vertexBuffer, 0);
    }

    private void bindUVData() {
        textureShaderProgram.setVertexAttribPointer(
                textureShaderProgram.getaTextureCoordinates(), UV_COMPONENT_COUNT, 0, uvBuffer, 0);
    }

    private void draw(List<Drawable> drawableList) {
        for (Drawable drawable : drawableList) {
            drawable.draw();
        }
    }

    private int initTextureUnit() {
        int textureUnitConstTobeUsed = GLES20.GL_TEXTURE7;
        int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;

        int textureUnitCount = 1;
        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int imageID = TextBuilder.getFontImageID();
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
}