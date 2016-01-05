package com.dewy.engine.text;

import android.opengl.GLES20;

import com.dewy.engine.R;
import com.dewy.engine.animation.view.unit.TexturePrimitiveData;
import com.dewy.engine.data.VertexBuffer;
import com.dewy.engine.primitives.Drawable;

import java.nio.ShortBuffer;

/**
 * Created by dewyone on 2015-08-18.
 */
public class TextBuilder {

    // 1. get Text
    // 2. make VertexData
    // 3. make UVs

    private static final String TAG = "TextBuilder";
    /*
            transforms the string given to the string which can be drawable to the screen
    */
    public static TexturePrimitiveData createString(String string, float x, float y, float fontSize) {
        float [] vertexData;
        final short [] drawOrderData;
        float [] uvData;
        Drawable drawable;

        short [] ids;
        final ShortBuffer drawOrderBuffer;

        ids  = translateStringToIDs(string);
        vertexData = getVertexData(ids, x, y, fontSize);
        drawOrderData = getDrawOrderData(string.length());
        uvData = getUVs(ids);

        drawOrderBuffer = VertexBuffer.arrayAsShortBuffer(drawOrderData);
        drawOrderBuffer.position(0);
        drawable = new Drawable() {
            @Override
            public void draw() {
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrderData.length, GLES20.GL_UNSIGNED_SHORT, drawOrderBuffer);
            }
        };

        return new TexturePrimitiveData(vertexData, drawOrderData, uvData, drawable);
    }

    /**
     *
     * @param ch a letter of which size we will return
     * @return letter size represented in normalized coords (-1 ~ +1)
     */
    public static float getRealLetterSize(char ch) {
        short id = charToID(ch);

        return l_size[id] / 64.0f;
    }

    // have relationship with getDrawOrderData(int)
    private static float[] getVertexData(short [] ids, float x, float y, float fontSize) {
        int charCount = ids.length;
        int totalVertexColumn = charCount + 1;
        int vertexCountPerChar = 4;
        int componentCountPerVertex = 3;
        int totalVertex = charCount * vertexCountPerChar;
        float [] vertexData = new float[ totalVertex * componentCountPerVertex ];
        int vertexDataIndex = 0;
        float lastXPosition = x;
        float width = 0;

        //String message = "lastXPosition : " + lastXPosition;
        //Log.i(TAG, message);
        vertexData[vertexDataIndex++] = lastXPosition;
        vertexData[vertexDataIndex++] = y;
        vertexData[vertexDataIndex++] = 0;

        vertexData[vertexDataIndex++] = lastXPosition;
        vertexData[vertexDataIndex++] = y - fontSize;
        vertexData[vertexDataIndex++] = 0;

        for(int i = 0; i < (totalVertexColumn - 1); i++) {
            int id = ids[i];
            //message = "id : " + id; Log.i(TAG, message);
            width = (l_size[id] / 64.0f) * fontSize;
            //message = "width : " + width; Log.i(TAG, message);
            lastXPosition += width;
            //message = "lastXPosition : " + lastXPosition; Log.i(TAG, message);

            vertexData[vertexDataIndex++] = lastXPosition;
            vertexData[vertexDataIndex++] = y;
            vertexData[vertexDataIndex++] = 0;

            vertexData[vertexDataIndex++] = lastXPosition;
            vertexData[vertexDataIndex++] = y - fontSize;
            vertexData[vertexDataIndex++] = 0;

            if (i == totalVertexColumn - 2) break;
            vertexData[vertexDataIndex++] = lastXPosition;
            vertexData[vertexDataIndex++] = y;
            vertexData[vertexDataIndex++] = 0;

            vertexData[vertexDataIndex++] = lastXPosition;
            vertexData[vertexDataIndex++] = y - fontSize;
            vertexData[vertexDataIndex++] = 0;
        }

        return vertexData;
    }

    // should be modified if the order of vertex creation is changed
    private static short [] getDrawOrderData(int stringLength) {
        int vertexColumCount = stringLength + 1;
        int totalTriangles = (vertexColumCount - 1) * 2;
        short [] drawOrder = new short[ totalTriangles * 3];
        int drawOrderIndex = 0;

        for (int i = 0; i < (vertexColumCount - 1); i++ ) {
            drawOrder[drawOrderIndex++] = (short) (0 + i * 4);
            drawOrder[drawOrderIndex++] = (short) (1 + i * 4);
            drawOrder[drawOrderIndex++] = (short) (3 + i * 4);

            drawOrder[drawOrderIndex++] = (short) (0 + i * 4);
            drawOrder[drawOrderIndex++] = (short) (3 + i * 4);
            drawOrder[drawOrderIndex++] = (short) (2 + i * 4);
        }

        return drawOrder;
    }

    private static short[] translateStringToIDs(String string) {
        // string to char [] to ids[]
        char [] charData = string.toCharArray();
        short [] ids = new short[charData.length];

        for (int i = 0; i < charData.length; i++) {
            ids[i] = charToID(charData[i]);
        }

        return ids;
    }

    private static float [] getUVs(short[] ids) {

        float [] uvs = new float[ids.length * 8];       // 8 : 2 float per point, and 4 points

        int uvIndex = 0;
        float startX = 0;
        float startY = 0;
        float width = 0;
        float height = 1.0f / 8.0f;
        int columnCount = 8;
        int rowCount = 8;

        for (int i = 0; i < ids.length; i++) {
            short id = ids[i];
            startX = ( id % columnCount) / (float) columnCount;
            startY = ( id / rowCount) / (float) rowCount;

            width = (float) l_size[id] / 512.0f;

            //String message = "id : " + id + ", startX : " + startX + ", startY : " + startY + ", width : " + width;
            //Log.i(TAG, message);

            float [] uv = {
                    startX, startY,
                    startX, startY + height,
                    startX + width, startY,
                    startX + width, startY + height,
            };

            for (float uvCoord : uv) {
                uvs[uvIndex++] = uvCoord;
            }
        }

        return uvs;
    }

    private static short charToID(char ch) {

        char upperCase = Character.toUpperCase(ch);
        //System.out.println("input character and it's value : " + upperCase + ", " + (short) upperCase);
        short id = -1;

        if (upperCase >= 'A' && upperCase <= 'Z') {
            id = (short) (upperCase - 65);
        } else
        if ( upperCase >= '0' && upperCase <='9') {
            id = (short) (upperCase - 22);
        } else {
            switch (upperCase) {
                case '!' : id = (short) (upperCase + 3); break;
                case '?' : id = (short) (upperCase - 26); break;
                case '+' : id = (short) (upperCase - 5); break;
                case '-' : id = (short) (upperCase - 6); break;
                case '#' : id = (short) (upperCase + 5); break;
                case ':' : id = (short) (upperCase - 17); break;
                case '.' : id = (short) (upperCase - 4); break;
                case ',' : id = (short) (upperCase - 1); break;
                case '*' : id = (short) (upperCase + 2); break;
                case '$' : id = (short) (upperCase + 9); break;
                default : id = (short) 49;      // space
            }
        }

        //System.out.println("and it's ID : " + id);

        return id;
    }

    public static int getFontImageID() {
        return R.drawable.font;
    }

    public static int[] l_size = {
            36, 29, 30, 34, 25, 25, 34, 33,
            11, 20, 31, 24, 48, 35, 39, 29,
            42, 31, 27, 31, 34, 35, 46, 35,
            31, 27, 30, 26, 28, 26, 31, 28,
            28, 28, 29, 29, 14, 24, 30, 18,
            26, 14, 14, 14, 25, 28, 31, 0,
            0, 38, 39, 12, 36, 34, 0, 0,
            0, 38, 0, 0, 0, 0, 0, 0};
}
