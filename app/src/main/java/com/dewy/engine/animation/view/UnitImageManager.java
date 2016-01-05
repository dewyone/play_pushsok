package com.dewy.engine.animation.view;

import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.view.unit.TexturePrimitiveData;
import com.dewy.engine.data.VertexBuffer;
import com.dewy.engine.text.TextRenderer;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dewyone on 2015-08-23.
 */
public class UnitImageManager {
    private static final String TAG = "UnitImageManager";

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawOrderBuffer;
    private FloatBuffer uvBuffer;

    private final List<TexturePrimitiveData> texturePrimitiveDataList;
    private int totalCharCount;

    private UnitImageBuilder unitImageBuilder;
    private UnitImageRenderer unitImageRenderer;

    public UnitImageManager(GLContext GLContext) {
        texturePrimitiveDataList = new ArrayList<>();
        totalCharCount = 0;
        unitImageBuilder = new UnitImageBuilder();
        unitImageRenderer = new UnitImageRenderer(GLContext);
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

    public void drawUnits() {
        makeBuffersOne2();      // makes vertexBuffer, drarwOrderBuffer, uvBuffer

        unitImageRenderer.draw(vertexBuffer, drawOrderBuffer, uvBuffer);
    }

    public void setViewProjectionMatrix(float [] viewProjectionMatrix) {
        unitImageRenderer.setViewProjectionMatrix(viewProjectionMatrix);
    }

    private void makeBuffersOne() {
        List<Float> vertexDataList = new ArrayList<>();
        List<Short> drawOrderDataList = new ArrayList<>();
        List<Float> uvDataList = new ArrayList<>();
        int drawOrderDataOffset = 0;

        for (TexturePrimitiveData unitData : texturePrimitiveDataList) {
            List<Float> vertexData = new ArrayList<>();
            for (float vertexCoord : unitData.vertexData) {
                vertexData.add(vertexCoord);
            }

            List<Short> drawOrderData = new ArrayList<>();
            for (short drawOrderElement : unitData.drawOrderData) {
                drawOrderData.add((short) (drawOrderElement + drawOrderDataOffset));
            }

            List<Float> uvData = new ArrayList<>();
            for (float uvElement : unitData.uvData) {
                uvData.add(uvElement);
            }

            vertexDataList.addAll(vertexData);
            drawOrderDataList.addAll(drawOrderData);
            uvDataList.addAll(uvData);

            drawOrderDataOffset += vertexData.size();

            //String message = "Got the texturePrimitiveData to the List";
            //Log.i(TAG, message);
        }

        float [] vertexDatas = new float[vertexDataList.size()];
        int index = 0;
        for (Float data : vertexDataList) {
            vertexDatas[index++] = data;
        }
        vertexBuffer = VertexBuffer.arrayAsVertexBuffer(vertexDatas);
        vertexBuffer.position(0);

        short [] drawOrderDatas = new short[drawOrderDataList.size()];
        index = 0;
        for (Short data : drawOrderDataList) {
            drawOrderDatas[index++] = data;
        }
        drawOrderBuffer = VertexBuffer.arrayAsShortBuffer(drawOrderDatas);
        drawOrderBuffer.position(0);

        float [] uvDatas = new float[uvDataList.size()];
        index = 0;
        for (Float data : uvDataList) {
            uvDatas[index++] = data;
        }
        uvBuffer = VertexBuffer.arrayAsVertexBuffer(uvDatas);
        uvBuffer.position(0);
    }

    private synchronized void makeBuffersOne2() {
        float [] vertexDatas = new float[totalCharCount];
        int vertexDataIndex = 0;
        short [] drawOrderDatas = new short[totalCharCount];
        int drawOrderDataIndex = 0;
        float [] uvDatas = new float[totalCharCount];
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
    }

    public void setTextureNumber() {}
}
