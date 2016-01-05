package com.dewy.engine.animation.view.unitimagerenderer_test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.env.Skbl;
import com.dewy.engine.animation.view.UnitImageBuilder;
import com.dewy.engine.animation.view.unit.TexturePrimitiveData;
import com.dewy.engine.data.IndexBufferObject;
import com.dewy.engine.data.VertexBufferObject;
import com.dewy.engine.primitives.Drawable;
import com.dewy.engine.renderer.BaseRenderer20;
import com.dewy.engine.shader_programs.TextureShaderProgram;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glDrawElements;
import static com.dewy.engine.platform.GLContext.BYTES_PER_FLOAT;
import static com.dewy.engine.platform.GLContext.BYTES_PER_SHORT;

/**
 * Created by dewyone on 2015-08-26.
 */
public class UnitImageRenderer05 extends BaseRenderer20 {
    private static final String TAG = "UnitImageRenderer05";
    public static final int POSITION_COMPONENT_COUNT = 3;
    private final int UV_COMPONENT_COUNT = 2;

    private final Context context;
    private final TextureShaderProgram textureShaderProgram;

    private TexturePrimitiveData workerPrimitiveData;
    private TexturePrimitiveData spacePrimitiveData;
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawOrderBuffer;
    private FloatBuffer uvBuffer;

    private VertexBufferObject vertexBufferObject;
    private IndexBufferObject drawOrderBufferObject;
    private VertexBufferObject uvBufferObject;

    private int drawOrderDataLength;

    private int textureUnitNumber;

    public UnitImageRenderer05(GLContext GLContext) {
        super(GLContext);

        context = GLContext.getContext();
        textureShaderProgram = new TextureShaderProgram(context);

        float unitSize = 0.3f;
        workerPrimitiveData = UnitImageBuilder.createImage(Skbl.WORKER, -0.5f, 0.5f, unitSize);
        spacePrimitiveData = UnitImageBuilder.createImage(Skbl.EMPTYSPACE, -0.5f + unitSize + unitSize, 0.5f - unitSize, unitSize);

        int totalSizeOfVertexData = 0;
        int totalSizeOfDrawOrderData = 0;
        int totalSizeOfUVData = 0;
        drawOrderDataLength = 0;
        totalSizeOfVertexData += workerPrimitiveData.vertexData.length;
        totalSizeOfVertexData += spacePrimitiveData.vertexData.length;
        totalSizeOfDrawOrderData += workerPrimitiveData.drawOrderData.length;
        totalSizeOfDrawOrderData += spacePrimitiveData.drawOrderData.length;
        totalSizeOfUVData += workerPrimitiveData.uvData.length;
        totalSizeOfUVData += spacePrimitiveData.uvData.length;

        // get Vertex
        vertexBufferObject = new VertexBufferObject(totalSizeOfVertexData * BYTES_PER_FLOAT);
        String logMessage = "vertexBufferObject size : " + totalSizeOfVertexData * BYTES_PER_FLOAT;
        Log.i(TAG, logMessage);
        // get Draw Order
        drawOrderBufferObject = new IndexBufferObject(totalSizeOfDrawOrderData * BYTES_PER_SHORT);
        // get UVs
        uvBufferObject = new VertexBufferObject(totalSizeOfUVData * BYTES_PER_FLOAT);
        //uvBuffer.position(0);
        textureUnitNumber = initTextureUnit();

        int offsetVertexData = 12;
        int offsetDrawOrderData = 6;
        vertexBufferObject.bufferSubData(workerPrimitiveData.vertexData, offsetVertexData * BYTES_PER_FLOAT);
        short [] drawOrderData = new short[workerPrimitiveData.drawOrderData.length];
        for (int i = 0; i < workerPrimitiveData.drawOrderData.length; i++) {
            //drawOrderData[i] = (short) (workerPrimitiveData.drawOrderData[i] + offsetVertexData);
            drawOrderData[i] = (short) (spacePrimitiveData.drawOrderData[i]);
        }
        drawOrderBufferObject.bufferSubData(drawOrderData, offsetDrawOrderData * BYTES_PER_SHORT);
        drawOrderBufferObject.bufferSubData(workerPrimitiveData.drawOrderData, offsetDrawOrderData * BYTES_PER_SHORT);
        offsetDrawOrderData += workerPrimitiveData.drawOrderData.length;
        offsetVertexData += workerPrimitiveData.vertexData.length;


        logMessage = "offsetVertexData : " + offsetVertexData;
        Log.i(TAG, logMessage);
        logMessage = "offsetDrawOrderData : " + offsetDrawOrderData;
        Log.i(TAG, logMessage);

        offsetVertexData = 0;
        offsetDrawOrderData = 0;
        vertexBufferObject.bufferSubData(spacePrimitiveData.vertexData, offsetVertexData * BYTES_PER_FLOAT);
        short [] drawOrderData2 = new short[spacePrimitiveData.drawOrderData.length];
        for (int i = 0; i < spacePrimitiveData.drawOrderData.length; i++) {
            drawOrderData2[i] = (short) (spacePrimitiveData.drawOrderData[i] + offsetVertexData);
            //drawOrderData[i] = (short) (spacePrimitiveData.drawOrderData[i]);
        }
        drawOrderBufferObject.bufferSubData(drawOrderData2, offsetDrawOrderData * BYTES_PER_SHORT);
        offsetDrawOrderData += spacePrimitiveData.drawOrderData.length;
        offsetVertexData += spacePrimitiveData.vertexData.length;

        logMessage = "offsetVertexData : " + offsetVertexData;
        Log.i(TAG, logMessage);
        logMessage = "offsetDrawOrderData : " + offsetDrawOrderData;
        Log.i(TAG, logMessage);

        drawOrderDataLength = offsetDrawOrderData;
        drawOrderDataLength = 12;
        logMessage = "drawOrderDataLength : " + drawOrderDataLength;
        Log.i(TAG, logMessage);


        int offsetUVData = 8;
        uvBufferObject.bufferSubData(workerPrimitiveData.uvData, offsetUVData * BYTES_PER_FLOAT);
        offsetUVData += workerPrimitiveData.uvData.length;

        offsetUVData = 0;
        uvBufferObject.bufferSubData(spacePrimitiveData.uvData, offsetUVData * BYTES_PER_FLOAT);
        offsetUVData += spacePrimitiveData.uvData.length;

    }

    public void draw() {
        transitModel();

        textureShaderProgram.useProgram();
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, textureUnitNumber);
        //bindData();
        vertexBufferObject.setVertexAttribPointer(textureShaderProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, 0, 0);
        uvBufferObject.setVertexAttribPointer(textureShaderProgram.getaTextureCoordinates(), UV_COMPONENT_COUNT, 0, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, drawOrderBufferObject.getBufferId());
        //glDrawElements(GL_TRIANGLES, drawOrderDataLength, GL_UNSIGNED_SHORT, 0);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        vertexBufferObject.setVertexAttribPointer(textureShaderProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, 0, 48);
        uvBufferObject.setVertexAttribPointer(textureShaderProgram.getaTextureCoordinates(), UV_COMPONENT_COUNT, 0, 32);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, drawOrderBufferObject.getBufferId());
        //glDrawElements(GL_TRIANGLES, drawOrderDataLength, GL_UNSIGNED_SHORT, 0);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 12);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glDisableVertexAttribArray(textureShaderProgram.getPositionAttributeLocation());
        GLES20.glDisableVertexAttribArray(textureShaderProgram.getaTextureCoordinates());
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
        vertexBufferObject.setVertexAttribPointer(textureShaderProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, 0, 0);
    }

    private void bindUVData() {
        uvBufferObject.setVertexAttribPointer(textureShaderProgram.getaTextureCoordinates(), UV_COMPONENT_COUNT, 0, 0);
    }

    private void draw(List<Drawable> drawableList) {
        for (Drawable drawable : drawableList) {
            drawable.draw();
        }
    }

    private int initTextureUnit() {
        int textureUnitConstTobeUsed = GLES20.GL_TEXTURE0;
        int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;

        int textureUnitCount = 1;
        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int imageID = UnitImageBuilder.getUnitImageID();
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
