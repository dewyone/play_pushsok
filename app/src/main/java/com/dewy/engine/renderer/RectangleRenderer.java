package com.dewy.engine.renderer;

import android.content.Context;
import android.opengl.Matrix;

import com.dewy.engine.data.VertexArray;
import com.dewy.engine.primitives.Drawable;
import com.dewy.engine.primitives.ObjectBuilder;
import com.dewy.engine.primitives.PrimitiveData;
import com.dewy.engine.shader_programs.ColorShaderProgram;
import com.dewy.engine.util.Geometry;

import java.util.List;

/**
 * Created by dewyone on 2015-08-02.
 */
public class RectangleRenderer extends BaseRenderer {

    private final int POSITION_COMPONENT_COUNT = 3;

    private final ColorShaderProgram colorShaderProgram;
    private float [] colorList;
    private final VertexArray vertexArray;
    private final PrimitiveData primitiveData;

    public RectangleRenderer(Context context) {
        super(context);

        colorShaderProgram = new ColorShaderProgram(context);
        colorList = new float[]{0.0f, 0.8f, 0.2f};


        primitiveData = ObjectBuilder.createRectangle(new Geometry.Point(0, 0, 0), new Geometry.Vector(0, 0, 1), 2.0f, 0.05f);

        vertexArray = new VertexArray(primitiveData.vertexData);
        bindData(vertexArray);
    }

    private void bindData(VertexArray vertexArray) {
        vertexArray.setVertexAttribPointer(0, colorShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, 0);
    }

    @Override
    public void draw() {
        transitModel();

        colorShaderProgram.useProgram();
        colorShaderProgram.setMatrixUniform(modelViewProjectionMatrix);
        colorShaderProgram.setColorUniform(colorList[0], colorList[1], colorList[2]);
        bindData(vertexArray);
        drawSquare(primitiveData.drawableList);
    }

    private void drawSquare(List<Drawable> drawList) {
        for (Drawable drawable : drawList) {
            drawable.draw();
        }
    }

    @Override
    protected void transitModel() {
        //translateModel();
        //rotateModel();

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
    }

    // @ should be called from transitModel()
    private void translateModel(){
        // determine the position of point x, y, z
        if (yP > 1 || yP < -1) yPDelta = -yPDelta;
        yP += yPDelta;

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, xP, yP, zP);
    }

    //  @ should be called from transitModel()
    private void rotateModel(){
        // set Angle Values
        angle += angleDelta;
        xAxis += xAxisDelta;
        yAxis += yAxisDelta;
        zAxis += zAxisDelta;

        Matrix.rotateM(modelMatrix, 0, angle, xAxis, yAxis, zAxis);   // Rotates matrix m in place by angle a (in degrees) around the axis (x, y, z)
    }
}
