package com.dewy.engine.primitives;

import com.dewy.engine.data.VertexArray;
import com.dewy.engine.shader_programs.PointShaderProgram;
import com.dewy.engine.util.Constants;

import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.glDrawArrays;

/**
 * Created by dewyone on 2015-07-28.
 */
public class Dot {

    private static final int POSITION_COMPONENT_COUNT = 4;
    private static final int COLOR_COMPONENT_COUNT = 4;
    private static final int TotalComponent = POSITION_COMPONENT_COUNT +
            COLOR_COMPONENT_COUNT;
    private static final int STRIDE = (TotalComponent) * Constants.BYTES_PER_FLOAT;
    private final int vertexCount;

    private final VertexArray vertexArray;

    public Dot() {
        vertexCount = VERTEX_DATA.length / TotalComponent;
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(PointShaderProgram pointShaderProgram) {
        // void setVertexAttribPointer(int dataOffset, int attributeLocation, int componentCount, int stride)
        vertexArray.setVertexAttribPointer( 0, pointShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, STRIDE);
        vertexArray.setVertexAttribPointer( POSITION_COMPONENT_COUNT,
                pointShaderProgram.getColorAttributeLocation(), COLOR_COMPONENT_COUNT, STRIDE);
    }

    public void draw() {
        glDrawArrays(GL_POINTS, 0, vertexCount);
    }

    private static final float[] VERTEX_DATA = {
            // Order of coordinates: X, Y, Z, W,     R, G, B, A
            0.0f, 0f, 0f, 1f,    0f, 0.2f, 1f, 1.0f
    };
}
