package com.dewy.engine.animation.view;

import android.util.Log;

import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.view.unit.TexturePrimitiveData;
import com.dewy.engine.data.VertexBuffer;
import com.dewy.engine.text.TextRenderer;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dewyone on 2015-08-27.
 */
public class StaticUnitImageManager {
    private static final String TAG = "StaticUnitImageManager";

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawOrderBuffer;
    private FloatBuffer uvBuffer;

    private float [] vertexDatas;
    private short [] drawOrderDatas;
    private float [] uvDatas;

    private final List<TexturePrimitiveData> texturePrimitiveDataList;
    private int totalCharCount;

    private StaticUnitRenderer staticUnitRenderer;

    public StaticUnitImageManager(GLContext GLContext) {
        texturePrimitiveDataList = new ArrayList<>();
        totalCharCount = 0;
        staticUnitRenderer = new StaticUnitRenderer(GLContext);
    }

    public TexturePrimitiveData createUnit(int unitImageName, float x, float y, float unitImageSize) {
        return UnitImageBuilder.createImage(unitImageName, x, y, unitImageSize);
    }

    public synchronized void addUnitData(TexturePrimitiveData unitData){
        texturePrimitiveDataList.add(unitData);
        int charCount = unitData.vertexData.length / TextRenderer.POSITION_COMPONENT_COUNT * 4;
        totalCharCount += charCount;
        //String message = "added TexturePrimitiveData" + "Char Count : " + charCount + "\n";
        //String message2 = "totalCharCount " + totalCharCount;
        //Log.i(TAG, message + message2);
    }

    public synchronized void deleteUnitData(TexturePrimitiveData unitData) {
        texturePrimitiveDataList.remove(unitData);
        int charCount = unitData.vertexData.length / TextRenderer.POSITION_COMPONENT_COUNT * 4;
        totalCharCount -= charCount;
        //String message = "deleted TexturePrimitiveData" + "Char Count : " + charCount + "\n";
        //String message2 = "totalCharCount " + totalCharCount;
        //Log.i(TAG, message + message2);
    }

    public boolean contains(TexturePrimitiveData unitData) {
        return texturePrimitiveDataList.contains(unitData);
    }

    public void prepareDrawing() {
        makeBuffersOne();      // makes vertexBuffer, drarwOrderBuffer, uvBuffer
        staticUnitRenderer.setBufferData(vertexDatas, drawOrderDatas, uvDatas);
    }

    public void drawUnits() {
        staticUnitRenderer.draw();
    }

    public void setViewProjectionMatrix(float [] viewProjectionMatrix) {
        staticUnitRenderer.setViewProjectionMatrix(viewProjectionMatrix);
    }

    private synchronized void makeBuffersOne() {
        vertexDatas = new float[totalCharCount];
        int vertexDataIndex = 0;
        drawOrderDatas = new short[totalCharCount];
        int drawOrderDataIndex = 0;
        uvDatas = new float[totalCharCount];
        int uvDataIndex = 0;
        short drawOrderDataOffset = 0;

        for (TexturePrimitiveData unitData : texturePrimitiveDataList) {

            for (Float data : unitData.vertexData) {
                vertexDatas[vertexDataIndex++] = data;
            }

            for (Short data : unitData.drawOrderData) {
                drawOrderDatas[drawOrderDataIndex++] = (short) (data + drawOrderDataOffset);
            }

            for (Float data : unitData.uvData) {
                uvDatas[uvDataIndex++] = data;
            }

            drawOrderDataOffset += (unitData.vertexData.length / TextRenderer.POSITION_COMPONENT_COUNT);
        }

        vertexBuffer = VertexBuffer.arrayAsVertexBuffer(vertexDatas);
        vertexBuffer.position(0);

        drawOrderBuffer = VertexBuffer.arrayAsShortBuffer(drawOrderDatas);
        drawOrderBuffer.position(0);

        uvBuffer = VertexBuffer.arrayAsVertexBuffer(uvDatas);
        uvBuffer.position(0);

        //String message = "Got the textData to the List";
        //Log.i(TAG, message);

        String message2 = "totalCharCount " + totalCharCount;
        Log.i(TAG, message2);
    }

    public void setTextureNumber() {}

    public void deleteBuffer() {
        staticUnitRenderer.deleteBuffer();
    }
}
