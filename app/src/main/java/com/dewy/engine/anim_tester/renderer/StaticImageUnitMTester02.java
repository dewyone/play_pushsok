package com.dewy.engine.anim_tester.renderer;

import android.content.Context;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.view.StaticUnitImageManager;
import com.dewy.engine.animation.view.UnitImageManager;
import com.dewy.engine.animation.view.unit.TexturePrimitiveData;

/**
 * Created by dewyone on 2015-10-09.
 */
public class StaticImageUnitMTester02 {
    private static final String TAG = "StaticImageUnitMTester";

    private final GLContext glContext;
    private final Context context;
    private UnitImageManager um;
    private StaticUnitImageManager sum;
    private StaticUnitImageManager sum02;

    private float screenWidth;
    private float screenHeight;
    private final float [] viewMatrix = new float[16];
    private final float [] orthographicMatrix = new float[16];
    private final float [] viewProjectionMatrix = new float[16];

    private float boardStartX;
    private float boardStartY;

    private float mapUnitSize;

    private boolean reInstanciate = false;

    public StaticImageUnitMTester02(GLContext glContext) {
        this.glContext = glContext;
        this.context = glContext.getContext();

        um = new UnitImageManager(glContext);
        sum = new StaticUnitImageManager(glContext);

        onStart();
    }

    private void reInstanciate(Bundle stageNumberBundle) {
        String logMessage = "reInstanciate() is called...";
        Log.i(TAG, logMessage);

        reInstanciate = true;

        sum.deleteBuffer();

        //sum = new StaticUnitImageManager(glContext);
        sum02 = new StaticUnitImageManager(glContext);

        onStart02();

        reInstanciate = false;
    }

    public void onStart() {

        screenWidth = glContext.getScreenWidth();
        screenHeight = glContext.getScreenHeight();

        prepareDrawing();
    }

    public void onStart02() {

        screenWidth = glContext.getScreenWidth();
        screenHeight = glContext.getScreenHeight();

        prepareDrawing02();
    }

    public void onSurfaceChangedHandler(int width, int height) {
        String logMessage;
        screenWidth = (float) width;
        screenHeight = (float) height;
        float aspectRatio = screenWidth / screenHeight;

        float shrinkRatio;

        if ( height > width ) {        // width < height
            shrinkRatio = screenHeight / screenWidth;
            logMessage = "screen height > width, setting orthoMatrix";
            Log.i(TAG, logMessage);
            //Matrix.orthoM(orthographicMatrix, 0, -1, 1, -1 / aspectRatio, 1 / aspectRatio, -1, 1);
            Matrix.orthoM(orthographicMatrix, 0, -1, 1, -shrinkRatio, shrinkRatio, -1, 1);      // shrink height before adapted to screen
        } else {        // height < width,
            shrinkRatio = screenWidth / screenHeight;
            logMessage = "screen height < width, setting orthoMatrix";
            Log.i(TAG, logMessage);
            //Matrix.orthoM(orthographicMatrix, 0, -1, 1, -1 / (aspectRatio - 0.3f), 1 / (aspectRatio - 0.3f), -1, 1);      expand height before adapted to screen
            Matrix.orthoM(orthographicMatrix, 0, -shrinkRatio, shrinkRatio, -1 , 1, -1, 1);     // shrink width before adapted to screen
        }

        //sum.setViewProjectionMatrix(orthographicMatrix);
        //um.setViewProjectionMatrix(orthographicMatrix);

        //prepareDrawing();
    }

    private void prepareDrawing() {
        float x = 0;
        float y = 0;

        mapUnitSize = 0.1f;

        boardStartX = -mapUnitSize / 2.0f;
        boardStartY = mapUnitSize / 2.0f;

        TexturePrimitiveData unitData = null;
        int unitID = 1;

        x = boardStartX;
        y = boardStartY;

        unitData = um.createUnit(unitID, x, y, mapUnitSize);

        sum.addUnitData(unitData);
        sum.prepareDrawing();
    }

    private void prepareDrawing02() {
        float x = 0;
        float y = 0;

        mapUnitSize = 0.1f;

        boardStartX = -mapUnitSize / 2.0f;
        boardStartY = mapUnitSize / 2.0f;

        TexturePrimitiveData unitData = null;
        int unitID = 1;

        x = boardStartX;
        y = boardStartY;

        unitData = um.createUnit(unitID, x, y, mapUnitSize);

        //sum.addUnitData(unitData);
        //sum.prepareDrawing();

        sum02.addUnitData(unitData);
        sum02.prepareDrawing();
    }

    public void draw() {
        String logMessage;

        if (reInstanciate) {
            if (sum02 == null) {
                logMessage = "reInstanciating..  sum02 is null";
                Log.i(TAG, logMessage);
            }
            return;
        }

        sum.drawUnits();
        if (sum02 != null) sum02.drawUnits();
    }

    public void onTouchEvent(MotionEvent motionEvent) {     // move worker

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            reInstanciate(null);
        }
    }
}
