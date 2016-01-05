package com.dewy.engine.animation.view;

import android.util.Log;

import com.dewy.engine.R;
import com.dewy.engine.animation.env.Skbl;
import com.dewy.engine.animation.view.unit.TexturePrimitiveData;
import com.dewy.engine.data.VertexBuffer;

import java.nio.ShortBuffer;

/**
 * Created by dewyone on 2015-08-23.
 */
public class UnitImageBuilder {
    // Makes one box image from the given image atlas

    private static final String TAG = "UnitImageBuilder";

    private static final int WORKER = 0;
    private static final int WALL = 1;
    private static final int BOX = 2;
    private static final int BOXINGOAL = 3;
    private static final int GOAL = 4;
    private static final int EMPTYSPACE = 5;
    private static final int WORKERINGOAL = 6;

    /*
            creates an image unit which can be drawable to the screen
    */
    public static TexturePrimitiveData createImage(int unitName, float x, float y, float unitImageSize) {
        float [] vertexData;
        final short [] drawOrderData;
        float [] uvData;
        final ShortBuffer drawOrderBuffer;

        int unitImageID = unitNameToUnitImageID(unitName);
        vertexData = getVertexData(x, y, unitImageSize);
        drawOrderData = getDrawOrderData();
        uvData = getUVs(unitImageID);

        drawOrderBuffer = VertexBuffer.arrayAsShortBuffer(drawOrderData);
        drawOrderBuffer.position(0);

        return new TexturePrimitiveData(vertexData, drawOrderData, uvData, null);
    }

    private static float [] getVertexData(float x, float y, float unitImageSize) {
        int totalVertex = 4;
        int componentCountPerVertex = 3;
        int vertexDataIndex = 0;

        float [] vertexData = new float[ totalVertex * componentCountPerVertex ];

        vertexData[vertexDataIndex++] = x;
        vertexData[vertexDataIndex++] = y;
        vertexData[vertexDataIndex++] = 0;

        vertexData[vertexDataIndex++] = x;
        vertexData[vertexDataIndex++] = y - unitImageSize;
        vertexData[vertexDataIndex++] = 0;

        vertexData[vertexDataIndex++] = x + unitImageSize;
        vertexData[vertexDataIndex++] = y - unitImageSize;
        vertexData[vertexDataIndex++] = 0;

        vertexData[vertexDataIndex++] = x + unitImageSize;
        vertexData[vertexDataIndex++] = y;
        vertexData[vertexDataIndex++] = 0;

        return vertexData;
    }

    private static short [] getDrawOrderData() {
        short [] drawOrder = new short[6];      // two triangles
        int drawOrderIndex = 0;


            drawOrder[drawOrderIndex++] = (short) (0 );
            drawOrder[drawOrderIndex++] = (short) (1);
            drawOrder[drawOrderIndex++] = (short) (2);

            drawOrder[drawOrderIndex++] = (short) (0);
            drawOrder[drawOrderIndex++] = (short) (2);
            drawOrder[drawOrderIndex++] = (short) (3);


        return drawOrder;
    }

    private static float [] getUVs(int unitImageID) {

        float [] uvs = new float[8];       // 8 : 2 float per point, and 4 points

        int uvIndex = 0;
        float startX = 16.0f / 288.0f;       // margin of the image file
        float startY = 16.0f / 320.0f;
        //float imageUnitWidth = (288.0f - startX * 2) / 8.0f;    // the image file has 8 columns
        //float imageUnitHeight = (320.0f - startY * 2) / 9.0f;    // has 9 rows
        float imageUnitWidth = 32 / 288.0f;    // the image file has 8 columns
        float imageUnitHeight = 32 / 320.0f;    // has 9 rows

        switch (unitImageID) {
            case WALL :
                startX = startX + imageUnitWidth * 0;
                startY = startY + imageUnitHeight * 1;
                break;
            case  WORKER :
                startX = startX + imageUnitWidth * 2;
                startY = startY + imageUnitHeight * 2;
                break;
            case  WORKERINGOAL :
                startX = startX + imageUnitWidth * 2;
                startY = startY + imageUnitHeight * 2;
                break;
            case  BOX :
                startX = startX + imageUnitWidth * 3;
                startY = startY + imageUnitHeight * 2;
                break;
            case  BOXINGOAL :
                startX = startX + imageUnitWidth * 3;
                startY = startY + imageUnitHeight * 6;
                break;
            case  GOAL :
                startX = startX + imageUnitWidth * 1;
                startY = startY + imageUnitHeight * 2;
                break;
            case  EMPTYSPACE :
                startX = startX + imageUnitWidth * 3;
                startY = startY + imageUnitHeight * 1;
                break;
            default:
                //throw new Exception(TAG + ", No such unitImageID");
                String logMessage = "No such unitImageID : " + unitImageID;
                Log.i(TAG, logMessage);
                break;
        }

        float [] uv = {
                startX, startY,
                startX, startY + imageUnitHeight,
                startX + imageUnitWidth, startY + imageUnitHeight,
                startX + imageUnitWidth, startY
        };

        for (float uvCoord : uv) {
            uvs[uvIndex++] = uvCoord;
        }

        return uvs;
    }

    private static int unitNameToUnitImageID(int unitName) {
        // gives the unit image position of the image file given
        int unitImageID = -1;

        switch (unitName) {
            case Skbl.WORKER :
                unitImageID = WORKER;
                break;
            case Skbl.WORKERINGOAL :
                unitImageID = WORKERINGOAL;
                break;
            case Skbl.WALL :
                unitImageID = WALL;
                break;
            case Skbl.BOX :
                unitImageID = BOX;
                break;
            case Skbl.BOXINGOAL :
                unitImageID = BOXINGOAL;
                break;
            case Skbl.EMPTYSPACE :
                unitImageID = EMPTYSPACE;
                break;
            case Skbl.GOALS :
                unitImageID = GOAL;
                break;
            default:
                unitImageID = -1;
                break;
        }

        return unitImageID;
    }

    public static int getUnitImageID() {
        return R.drawable.sokoban_frame_png;
    }
}