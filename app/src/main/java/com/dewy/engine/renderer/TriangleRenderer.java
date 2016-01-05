package com.dewy.engine.renderer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.dewy.engine.data.IndexBufferObject;
import com.dewy.engine.data.VertexBufferObject;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.primitives.Drawable;
import com.dewy.engine.primitives.ObjectBuilder;
import com.dewy.engine.primitives.PrimitiveData;
import com.dewy.engine.shader_programs.ColorShaderProgram;
import com.dewy.engine.util.Geometry;

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
public class TriangleRenderer extends BaseRenderer20 {
    private static final String TAG = "Triangle Renderer";

    public static final int POSITION_COMPONENT_COUNT = 3;
    private final int UV_COMPONENT_COUNT = 2;

    private PrimitiveData triangle;

    private final Context context;
    private final ColorShaderProgram colorShaderProgram;
    private float [] colorList;

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawOrderBuffer;

    private VertexBufferObject vertexBufferObject;
    private IndexBufferObject drawOrderBufferObject;

    public TriangleRenderer(GLContext GLContext) {
        super(GLContext);

        context = GLContext.getContext();
        colorShaderProgram = new ColorShaderProgram(context);
        colorList = new float[]{ 0.2f, 0.2f, 0.2f};

        int triangleCount = 1;
        float unitSize = 0.3f;

        // create primitives
        triangle = ObjectBuilder.createTriangle(new Geometry.Point(0, unitSize, 0), new Geometry.Point(-unitSize, -0.3f, 0),
                new Geometry.Point(unitSize, -unitSize, 0));

        // calculate totalVertexData, totalSizeOfDrawOrderData, totalSizeOfUVData

        // create Buffer Objects
        vertexBufferObject = new VertexBufferObject(triangleCount * 3 * 3 * BYTES_PER_FLOAT);
        drawOrderBufferObject = new IndexBufferObject(triangleCount * 3 * BYTES_PER_SHORT);

        // feed sub data
        vertexBufferObject.bufferSubData(triangle.vertexData, 0);
        drawOrderBufferObject.bufferSubData(new short[]{0, 1, 2}, 0);

    }

    public void draw() {
        transitModel();

        colorShaderProgram.useProgram();
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, colorList[0], colorList[1], colorList[2]);
        //bindData();
        vertexBufferObject.setVertexAttribPointer(colorShaderProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, 0, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, drawOrderBufferObject.getBufferId());
        glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_SHORT, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glDisableVertexAttribArray(colorShaderProgram.getPositionAttributeLocation());
    }

    public void draw(FloatBuffer vertexBuffer, ShortBuffer drawOrderBuffer) {
        this.vertexBuffer = vertexBuffer;
        this.drawOrderBuffer = drawOrderBuffer;
        draw();
    }

    private void bindData() {
        bindPositionData();
    }

    private void bindPositionData() {
        vertexBufferObject.setVertexAttribPointer(colorShaderProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, 0, 0);
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