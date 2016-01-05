package com.dewy.engine.animation.activities.view;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.dewy.engine.platform.GLContext;
import com.dewy.engine.data.VertexBuffer;
import com.dewy.engine.primitives.Drawable;
import com.dewy.engine.primitives.ObjectBuilder;
import com.dewy.engine.primitives.PrimitiveData;
import com.dewy.engine.renderer.BaseRenderer20;
import com.dewy.engine.shader_programs.TextureShaderProgram;
import com.dewy.engine.util.Geometry;

import java.nio.FloatBuffer;
import java.util.List;

/**
 * Created by dewyone on 2015-09-25.
 *
 * receive : rectangle info, uv info and texture unit number
 * show or draw : an image according to the data above
 * application : could be used in rendering rectangle with texture - example : sprite, texting, etc
 *                  could be thought of as an atomic unit in rendering objects with texture
 */
public class TextureRectRenderer extends BaseRenderer20{
    private static final String TAG = "TextureRectRenderer";

    public static final int POSITION_COMPONENT_COUNT = 3;
    private final int UV_COMPONENT_COUNT = 2;
    private final Context context;
    private final TextureShaderProgram textureShaderProgram;

    private PrimitiveData rectanglePrimi;
    private FloatBuffer vertexBuffer;
    private FloatBuffer uvBuffer;

    private final int textureUnitNumber;

    /**
     * Renders a rectangle with texture given, this constructor is usually used
     * in making a dynamic rectangle ( a rectangle that moves around, that changes its size..etc)
     * @param GLContext
     * @param textureUnitNumber
     */
    public TextureRectRenderer(GLContext GLContext, int textureUnitNumber) {
        super(GLContext);

        context = GLContext.getContext();
        textureShaderProgram = new TextureShaderProgram(context);
        this.textureUnitNumber = textureUnitNumber;
    }

    /**
     * Renders a rectangle with texture given. this constructor usually used in making a static rectangle ( a rectangle that doesn't move)
     * @param GLContext
     * @param posX
     * @param posY
     * @param rectWidth
     * @param rectHeight
     * @param textureUnitNumber
     */
    public TextureRectRenderer(GLContext GLContext, float posX, float posY, float rectWidth, float rectHeight, int textureUnitNumber) {
        super(GLContext);

        context = GLContext.getContext();
        textureShaderProgram = new TextureShaderProgram(context);
        this.textureUnitNumber = textureUnitNumber;

        rectanglePrimi = ObjectBuilder.createRectangle(new Geometry.Point(posX, posY, 0), new Geometry.Vector(0, 0, 1), rectWidth, rectHeight);

        vertexBuffer = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi.vertexData);
    }

    public void setRectDrawingInfo(PrimitiveData rectanglePrimi, FloatBuffer uvBuffer) {
        this.rectanglePrimi = rectanglePrimi;
        this.vertexBuffer = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi.vertexData);
        this.uvBuffer = uvBuffer;
    }

    public void setRectDrawingInfo(PrimitiveData rectanglePrimi, float [] uvData) {
        this.rectanglePrimi = rectanglePrimi;
        this.vertexBuffer = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi.vertexData);
        this.uvBuffer = VertexBuffer.arrayAsVertexBuffer(uvData);
    }

    public void setRectDrawingInfo(FloatBuffer uvBuffer) {
        this.uvBuffer = uvBuffer;
    }

    public void setRectDrawingInfo(float [] uvData) {
        this.uvBuffer = VertexBuffer.arrayAsVertexBuffer( uvData);
    }

    public void draw() {
        transitModel();

        /* we should enable blending func when absolutely necessary*/
        //GLES20.glEnable(GLES20.GL_BLEND);
        //GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        textureShaderProgram.useProgram();
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, textureUnitNumber);
        bindData();
        draw(rectanglePrimi.drawableList);

        GLES20.glDisableVertexAttribArray(textureShaderProgram.getPositionAttributeLocation());
        GLES20.glDisableVertexAttribArray(textureShaderProgram.getaTextureCoordinates());
        //GLES20.glDisable(GLES20.GL_BLEND);
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

    protected void transitModel() {
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
    }
}