package com.dewy.engine.animation.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.dewy.engine.platform.GLContext;
import com.dewy.engine.data.IndexBufferObject;
import com.dewy.engine.data.VertexBufferObject;
import com.dewy.engine.renderer.BaseRenderer20;
import com.dewy.engine.shader_programs.TextureShaderProgram;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glDrawElements;
import static com.dewy.engine.platform.GLContext.BYTES_PER_FLOAT;
import static com.dewy.engine.platform.GLContext.BYTES_PER_SHORT;

/**
 * Created by dewyone on 2015-08-27.
 *
 *  makes buffer objects from the data given and then draws
 *
 *  Receive : vertex data, drawOrder data, uv data
 */
public class StaticUnitRenderer extends BaseRenderer20{
    private static final String TAG = "StaticUnitRenderer";
    public static final int POSITION_COMPONENT_COUNT = 3;
    private final int UV_COMPONENT_COUNT = 2;

    private final Context context;
    private final TextureShaderProgram textureShaderProgram;

    private VertexBufferObject vertexBufferObject;
    private IndexBufferObject drawOrderBufferObject;
    private VertexBufferObject uvBufferObject;

    private int drawOrderDataLength;

    private int textureUnitNumber;

    public StaticUnitRenderer(GLContext GLContext) {
        super(GLContext);

        context = GLContext.getContext();
        textureShaderProgram = new TextureShaderProgram(context);
    }

    public void setBufferData(float [] vertexDatas, short [] drawOrderDatas, float [] uvDatas) {
        // make buffer objects

        int totalSizeOfVertexData = 0;
        int totalSizeOfDrawOrderData = 0;
        int totalSizeOfUVData = 0;;
        totalSizeOfVertexData = vertexDatas.length;
        totalSizeOfDrawOrderData = drawOrderDatas.length;
        totalSizeOfUVData = uvDatas.length;
        drawOrderDataLength = drawOrderDatas.length;

        // get Vertex
        vertexBufferObject = new VertexBufferObject(totalSizeOfVertexData * BYTES_PER_FLOAT);
        String logMessage = "vertexBufferObject size : " + totalSizeOfVertexData * BYTES_PER_FLOAT;
        //Log.i(TAG, logMessage);
        // get Draw Order
        drawOrderBufferObject = new IndexBufferObject(totalSizeOfDrawOrderData * BYTES_PER_SHORT);
        // get UVs
        uvBufferObject = new VertexBufferObject(totalSizeOfUVData * BYTES_PER_FLOAT);
        //uvBuffer.position(0);
        textureUnitNumber = initTextureUnit();


        vertexBufferObject.bufferSubData(vertexDatas, 0);
        drawOrderBufferObject.bufferSubData(drawOrderDatas, 0);
        uvBufferObject.bufferSubData(uvDatas, 0);
    }

    public void draw() {
        int stride = 0;
        int dataOffset = 0;
        transitModel();

        textureShaderProgram.useProgram();
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, textureUnitNumber);
        // bindData;
        vertexBufferObject.setVertexAttribPointer(
                textureShaderProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, stride, dataOffset);
        uvBufferObject.setVertexAttribPointer(
                textureShaderProgram.getaTextureCoordinates(), UV_COMPONENT_COUNT, stride, dataOffset);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, drawOrderBufferObject.getBufferId());
        glDrawElements(GL_TRIANGLES, drawOrderDataLength, GL_UNSIGNED_SHORT, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glDisableVertexAttribArray(textureShaderProgram.getPositionAttributeLocation());
        GLES20.glDisableVertexAttribArray(textureShaderProgram.getaTextureCoordinates());
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

    public void deleteBuffer() {
        vertexBufferObject.deleteBuffer();
        drawOrderBufferObject.deleteBuffer();
        uvBufferObject.deleteBuffer();
    }
}