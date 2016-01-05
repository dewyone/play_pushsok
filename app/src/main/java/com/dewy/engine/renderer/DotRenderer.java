package com.dewy.engine.renderer;

import android.content.Context;
import android.opengl.Matrix;

import com.dewy.engine.primitives.Dot;
import com.dewy.engine.shader_programs.PointShaderProgram;

/**
 * Created by dewyone on 2015-07-28.
 */
public class DotRenderer extends BaseRenderer {

    private Dot dot;
    //private TextureShaderProgram textureProgram;
    private PointShaderProgram pointShaderProgram;

    float pointSizeDelta = 2.0f;
    float pointSize = 50.0f;

    public DotRenderer(Context context) {
        super(context);

        dot = new Dot();
        pointShaderProgram = new PointShaderProgram(context);
    }

    public void draw() {
        // Draw a Dot
        transitModel();
        pointShaderProgram.useProgram();
        pointShaderProgram.setUniforms(modelViewProjectionMatrix, pointSize);
        dot.bindData(pointShaderProgram);
        dot.draw();
    }

    final protected void transitModel() {
        if (pointSize > 256 || pointSize < 50) pointSizeDelta = -pointSizeDelta;
        pointSize += pointSizeDelta;
        //pointSize = 50f;

        // determine the position of point x, y, z
        if (yP > 1 || yP < -1) yPDelta = -yPDelta;
        yP += yPDelta;

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, xP, yP, zP);
        //Matrix.rotateM(modelMatrix, 0, angle, xAxis, yAxis, zAxis);   // Rotates matrix m in place by angle a (in degrees) around the axis (x, y, z)

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
    }
}
