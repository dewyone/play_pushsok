package com.dewy.engine.renderer;

import android.opengl.Matrix;

import com.dewy.engine.platform.GLContext;

/**
 * Created by dewyone on 2015-08-11.
 */
public abstract class BaseRenderer20 {
    protected final GLContext GLContext;

    protected final float[] modelMatrix = new float[16];
    //protected final float [] viewProjectionMatrix = new float[16];
    protected float [] viewProjectionMatrix = new float[16];
    protected final float[] modelViewProjectionMatrix = new float[16];

    // Defauult Position and Angle values
    protected float xP = 0, yP = 0, zP = 0.0f;   // the point position x, y, z
    protected float xPDelta = 0, yPDelta = 0.01f, zPDelta = 0;
    protected float angle = 0.0f, xAxis = 1.0f, yAxis = 0.0f, zAxis = 0.0f;
    protected float angleDelta = 0.0f, xAxisDelta = 0f, yAxisDelta = 0, zAxisDelta = 0;

    protected BaseRenderer20(GLContext GLContext) {
        this.GLContext = GLContext;

        float [] viewMatrix = new float[16];
        float [] projectionMatrix = new float[16];

        /* *** set default ViewProjection Matrix*** */
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.setIdentityM(projectionMatrix, 0);
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        setModelViewProjectionMatrix();
    }

    public void setViewProjectionMatrix(float[] viewProjectionM) {
        this.viewProjectionMatrix = viewProjectionM;
        setModelViewProjectionMatrix();
        /*
        if (viewProjectionM.length != 16) { Log.w("Index Out of Bound", " array length must be 16");}
        for (int i = 0; i < viewProjectionM.length; i++) {
            viewProjectionMatrix[i] = viewProjectionM[i];
        }
        */
    }

    protected void setModelViewProjectionMatrix() {
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
    }

    public abstract void draw();

    protected abstract void transitModel();
}
