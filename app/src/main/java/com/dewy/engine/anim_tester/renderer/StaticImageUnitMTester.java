package com.dewy.engine.anim_tester.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.dewy.engine.R;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.activities.lib.ImageButtoner;
import com.dewy.engine.animation.env.Skbl;
import com.dewy.engine.animation.game_map.MapManager;
import com.dewy.engine.animation.unit.Transaction;
import com.dewy.engine.animation.view.StaticUnitImageManager;
import com.dewy.engine.animation.view.UnitImageManager;
import com.dewy.engine.animation.view.unit.TexturePrimitiveData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dewyone on 2015-10-08.
 */
public class StaticImageUnitMTester {
    private static final String TAG = "StaticImageUnitMTester";

    private final GLContext GLContext;
    private final Context context;
    private UnitImageManager um;
    private StaticUnitImageManager sum;

    private float screenWidth;
    private float screenHeight;
    private final float [] viewMatrix = new float[16];
    private final float [] orthographicMatrix = new float[16];
    private final float [] viewProjectionMatrix = new float[16];

    private int [] backgroundMap;
    private int [] unitMap;
    private TexturePrimitiveData[] backgroundUnits;        // for walls, goals, empty space
    private TexturePrimitiveData[] units;   // for boxes, worker..
    private List<MapManager.GoalUnits> goalUnitsList;       // for boxInGoals, workerInGoals (second background)

    private final List<Transaction> transactionList = new ArrayList<>();

    private int stageNumber;

    private float boardStartX;
    private float boardStartY;

    private int mapColumnInUnit;
    private int mapRowInUnit;
    private int totalGoalCount;
    private float mapUnitSize;

    private ImageButtoner nextLevelButton;
    private float nextLevelXpos = 0.45f;
    private float nextLevelYpos = 0.80f;

    private int playerColumn;
    private int playerRow;

    private boolean reInstanciate = false;

    public StaticImageUnitMTester(GLContext GLContext) {
        this.GLContext = GLContext;
        this.context = GLContext.getContext();

        um = new UnitImageManager(GLContext);
        sum = new StaticUnitImageManager(GLContext);
        mapUnitSize = 0.05f;

        int textureUnitNumber = initTextureUnitForNextLevelButton();
        nextLevelButton = new ImageButtoner(GLContext,
                nextLevelXpos, nextLevelYpos, textureUnitNumber);

        onCreate();
        onStart();
    }

    private void reInstanciate(Bundle stageNumberBundle) {
        reInstanciate = true;

        sum.deleteBuffer();

        um = new UnitImageManager(GLContext);
        sum = new StaticUnitImageManager(GLContext);

        onCreate();
        onStart();
        onResume();

        reInstanciate = false;
    }

    public void onCreate() {
        stageNumber = 1 - 1;
    }

    public void onStart() {

        screenWidth = GLContext.getScreenWidth();
        screenHeight = GLContext.getScreenHeight();

        play(stageNumber);
    }

    public void onSurfaceChangedHandler(int width, int height) {
        String logMessage;
        screenWidth = (float) width;
        screenHeight = (float) height;
        float aspectRatio = screenWidth / screenHeight;
/*
        //String logMessage = "-aspectRatio : " + (-aspectRatio) + ", aspectRation : " + aspectRatio;
        //Log.i(TAG, logMessage);
        if ( aspectRatio < 1 ) {        // height > width
            logMessage = "screen height > width, setting orthoMatrix";
            Log.i(TAG, logMessage);
            //Matrix.orthoM(orthographicMatrix, 0, -1, 1, -1 / aspectRatio, 1 / aspectRatio, -1, 1);
            Matrix.orthoM(orthographicMatrix, 0, -1, 1, -aspectRatio, aspectRatio, -1, 1);      // shrink height before adapted to screen
        } else {        // width > height,
            logMessage = "screen height < width, setting orthoMatrix";
            Log.i(TAG, logMessage);
            //Matrix.orthoM(orthographicMatrix, 0, -1, 1, -1 / (aspectRatio - 0.3f), 1 / (aspectRatio - 0.3f), -1, 1);      expand height before adapted to screen
            Matrix.orthoM(orthographicMatrix, 0, -aspectRatio, aspectRatio, -1 , 1, -1, 1);     // shrink width before adapted to screen
        }
        //Matrix.orthoM(orthographicMatrix, 0, -1, 1, -aspectRatio, aspectRatio, -1, 1);
*/
        float shrinkRatio;

        //String logMessage = "-aspectRatio : " + (-aspectRatio) + ", aspectRation : " + aspectRatio;
        //Log.i(TAG, logMessage);
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

        sum.setViewProjectionMatrix(orthographicMatrix);
        um.setViewProjectionMatrix(orthographicMatrix);

        //prepareDrawing();
    }

    public void onResume() {
    }

    private void play(int stageNumber) {
        prepareMapData(stageNumber);
        setPlayerColumnRow();
        prepareDrawing();    //  --> to onSurfaceChanged()
    }

    private void prepareMapData(int mapNumber) {
        // prepare background map
        // prepare unit map
        MapManager.MapData mapData = MapManager.getMapData(mapNumber);
        backgroundMap = mapData.getBackgroundMap();
        unitMap = mapData.getUnitMap();

        mapColumnInUnit = mapData.getColumn();
        mapRowInUnit = mapData.getRow();
        totalGoalCount = mapData.getGoalCount();

        backgroundUnits = new TexturePrimitiveData[mapColumnInUnit * mapRowInUnit];
        units = new TexturePrimitiveData[mapColumnInUnit * mapRowInUnit];
        goalUnitsList = new ArrayList<>();
    }

    private void setPlayerColumnRow() {

        for (int i = 0; i < unitMap.length; i++) {
            if (unitMap[i] == Skbl.WORKER) {
                playerColumn = i % mapColumnInUnit;
                playerRow = i / mapColumnInUnit;

                return;
            }
        }

        playerColumn = -1;
        playerRow = -1;
    }

    private void prepareDrawing() {
        float x = 0;
        float y = 0;

        setMapUnitSize();

        boardStartX = -(( (float) mapColumnInUnit / 2) * mapUnitSize);
        boardStartY = (( (float) mapRowInUnit / 2) * mapUnitSize);

        TexturePrimitiveData unitData = null;
        int column = 0;
        int row = 0;
        int unitID = -1;

        // for background, create background units
        for (int i = 0; i < backgroundMap.length; i++) {
            unitID = backgroundMap[i];
            column = i % mapColumnInUnit;
            row = i / mapColumnInUnit;
            x = boardStartX +column * mapUnitSize;
            y = boardStartY - row * mapUnitSize;

            unitData = um.createUnit(unitID, x, y, mapUnitSize);

            backgroundUnits[i] = unitData;
            sum.addUnitData(unitData);
        }

        // for units, create units which moves (box, worker)
        for (int i = 0; i < unitMap.length; i++) {

            unitID = unitMap[i];
            if ( unitID == Skbl.EMPTYSPACE) {
                units[i] = null;
                continue;
            }

            column = i % mapColumnInUnit;
            row = i / mapColumnInUnit;
            x = boardStartX +column * mapUnitSize;
            y = boardStartY - row * mapUnitSize;

            unitData = um.createUnit(unitID, x, y, mapUnitSize);

            units[i] = unitData;
            um.addUnitData(unitData);
        }

        // for goal units ( workerInGoal, boxInGoal )
        for (int i = 0; i < backgroundMap.length; i++) {
            unitID = backgroundMap[i];

            if ( unitID == Skbl.WALL || unitID == Skbl.EMPTYSPACE) continue;

            column = i % mapColumnInUnit;
            row = i / mapColumnInUnit;
            x = boardStartX +column * mapUnitSize;
            y = boardStartY - row * mapUnitSize;

            if ( unitID == Skbl.GOALS) {
                MapManager.GoalUnits goalUnits = new MapManager.GoalUnits(
                        i, um.createUnit(Skbl.BOXINGOAL, x, y, mapUnitSize), um.createUnit(Skbl.WORKERINGOAL, x, y, mapUnitSize) );
                goalUnitsList.add(goalUnits);
            }
        }

        sum.prepareDrawing();
    }

    public void draw() {

        if (reInstanciate) return;

        nextLevelButton.draw();

        sum.drawUnits();
        um.drawUnits();
    }

    public void onTouchEvent(MotionEvent motionEvent) {     // move worker

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            reInstanciate(null);
        }
    }

    // gets unitID of moving unit
    // returns unitID of background unit
    private TexturePrimitiveData getGoalUnits(int unitID, int serial) {

        for(MapManager.GoalUnits goalUnits : goalUnitsList) {
            if ( goalUnits.getSerial() == serial) {
                if (unitID == Skbl.WORKER) {
                    return goalUnits.getWorkerInGoal();
                } else if ( unitID == Skbl.BOX) {
                    return goalUnits.getBoxInGoal();
                }
            }
        }

        return null;
    }

    private void setMapUnitSize() {
        //float screenWidth = glContext.getScreenWidth();
        //float screenHeight = glContext.getScreenHeight();
        //String logMessage = "screenWidth() : " + glContext.getScreenWidth() + ", screenHeight : " + glContext.getScreenHeight();
        //Log.i(TAG, logMessage);

        String logMessage = "mapColumnInUnit : " + mapColumnInUnit + ", mapRowInUnit : " + mapRowInUnit;
        Log.i(TAG, logMessage);

        float margin = 0;
        float widthUnitSize = 2.0f / (mapColumnInUnit + margin );
        float heightUnitSize = 2.0f / (mapRowInUnit + margin );

        logMessage = "widthUnitSize : " + widthUnitSize + ", heightUnitSize : " + heightUnitSize;
        Log.i(TAG, logMessage);

        mapUnitSize = Math.min(widthUnitSize, heightUnitSize);

        // ****    Should be considered more    ****
        if ( screenWidth > screenHeight && mapRowInUnit < mapColumnInUnit)
            mapUnitSize *= ( ( ( (float) screenWidth / screenHeight) * ( (float) mapColumnInUnit / mapRowInUnit)) / 2);
        logMessage = "mapUnitSize : " + mapUnitSize;
        Log.i(TAG, logMessage);
        //if (mapUnitSize > 0.15f) mapUnitSize = 0.15f;
    }

    private int initTextureUnitForNextLevelButton() {
        int [] maxTextureNumber = new int[1];

        int textureUnitCount = 1;
        int textureUnitConst = GLES20.GL_TEXTURE1;
        int textureUnitNumber = textureUnitConst - GLES20.GL_TEXTURE0;

        int imageID = R.drawable.next_level;

        //int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int textureUnitConstTobeUsed = textureUnitConst;
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
}
