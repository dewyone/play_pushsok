package com.dewy.engine.animation.activities.view;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.dewy.engine.data.VertexBuffer;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.primitives.Drawable;
import com.dewy.engine.primitives.ObjectBuilder;
import com.dewy.engine.primitives.PrimitiveData;
import com.dewy.engine.renderer.BaseRenderer20;
import com.dewy.engine.shader_programs.TextureAlphaShaderProgram;
import com.dewy.engine.shader_programs.TextureShaderProgram;
import com.dewy.engine.util.Geometry;

import java.nio.FloatBuffer;
import java.util.List;

/**
 * Created by dewyone on 2015-10-23.
 */
public class TexAlphaRectRenderer extends BaseRenderer20 {
    private static final String TAG = "TexAlphaRectRenderer";

    public static final int POSITION_COMPONENT_COUNT = 3;
    private final int UV_COMPONENT_COUNT = 2;
    private final Context context;
    private final TextureAlphaShaderProgram textureAlphaShaderProgram;

    private PrimitiveData rectanglePrimi;
    private FloatBuffer vertexBuffer;
    private FloatBuffer uvBuffer;

    private final int textureUnitNumber;
    private float colorAlpha = 1.0f;

    /**
     * Renders a rectangle with texture given, this constructor is usually used
     * in making a dynamic rectangle ( a rectangle that moves around, that changes its size..etc)
     * @param GLContext
     * @param textureUnitNumber
     */
    public TexAlphaRectRenderer(GLContext GLContext, int textureUnitNumber) {
        super(GLContext);

        context = GLContext.getContext();
        textureAlphaShaderProgram = new TextureAlphaShaderProgram(context);
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
    public TexAlphaRectRenderer(GLContext GLContext, float posX, float posY, float rectWidth, float rectHeight, int textureUnitNumber, float colorAlpha) {
        super(GLContext);

        context = GLContext.getContext();
        textureAlphaShaderProgram = new TextureAlphaShaderProgram(context);
        this.textureUnitNumber = textureUnitNumber;
        this.colorAlpha = colorAlpha;

        rectanglePrimi = ObjectBuilder.createRectangle(new Geometry.Point(posX, posY, 0), new Geometry.Vector(0, 0, 1), rectWidth, rectHeight);

        vertexBuffer = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi.vertexData);
    }

    public void setRectDrawingInfo(PrimitiveData rectanglePrimi, FloatBuffer uvBuffer, float colorAlpha) {
        this.rectanglePrimi = rectanglePrimi;
        this.vertexBuffer = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi.vertexData);
        this.uvBuffer = uvBuffer;
        this.colorAlpha = colorAlpha;
    }

    public void setRectDrawingInfo(PrimitiveData rectanglePrimi, float [] uvData) {
        this.rectanglePrimi = rectanglePrimi;
        this.vertexBuffer = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi.vertexData);
        this.uvBuffer = VertexBuffer.arrayAsVertexBuffer(uvData);
    }

    public void setRectDrawingInfo(PrimitiveData rectanglePrimi) {
        this.rectanglePrimi = rectanglePrimi;
        this.vertexBuffer = VertexBuffer.arrayAsVertexBuffer(rectanglePrimi.vertexData);
    }

    public void setRectDrawingInfo(FloatBuffer uvBuffer) {
        this.uvBuffer = uvBuffer;
    }

    public void setRectDrawingInfo(float [] uvData) {
        this.uvBuffer = VertexBuffer.arrayAsVertexBuffer( uvData);
    }

    public void setRectDrawingInfo(float colorAlpha) {
        this.colorAlpha = colorAlpha;
    }

    public void draw() {
        transitModel();

        textureAlphaShaderProgram.useProgram();
        textureAlphaShaderProgram.setUniforms(modelViewProjectionMatrix, textureUnitNumber, colorAlpha);
        bindData();
        draw(rectanglePrimi.drawableList);

        GLES20.glDisableVertexAttribArray(textureAlphaShaderProgram.getPositionAttributeLocation());
        GLES20.glDisableVertexAttribArray(textureAlphaShaderProgram.getaTextureCoordinates());
    }

    private void bindData() {
        textureAlphaShaderProgram.setVertexAttribPointer(
                textureAlphaShaderProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, 0, vertexBuffer, 0);
        textureAlphaShaderProgram.setVertexAttribPointer(
                textureAlphaShaderProgram.getaTextureCoordinates(), UV_COMPONENT_COUNT, 0, uvBuffer, 0);
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
