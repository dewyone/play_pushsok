package com.dewy.engine.text;

import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.view.unit.TexturePrimitiveData;
import com.dewy.engine.data.VertexBuffer;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dewyone on 2015-08-20.
 */
public class TextManager {
    private static final String TAG = "TextManager";

    private final List<TexturePrimitiveData> texturePrimitiveDataList;
    private int totalCharCount;

    private TextBuilder textBuilder;
    private TextRenderer textRenderer;

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawOrderBuffer;
    private FloatBuffer uvBuffer;
    private boolean updated;

    public TextManager(GLContext GLContext) {
        texturePrimitiveDataList = new ArrayList<>();
        totalCharCount = 0;
        textBuilder = new TextBuilder();
        textRenderer = new TextRenderer(GLContext);

        updated = true;
    }

    public TexturePrimitiveData createString(String string, float x, float y, float fontSize) { return textBuilder.createString(string, x, y, fontSize);}

    public synchronized void addTextData(TexturePrimitiveData texturePrimitiveData){
        texturePrimitiveDataList.add(texturePrimitiveData);
        int charCount = texturePrimitiveData.vertexData.length / TextRenderer.POSITION_COMPONENT_COUNT * 4;
        totalCharCount += charCount;
        //String message = "added TexturePrimitiveData" + "Char Count : " + charCount + "\n";
        //String message2 = "totalCharCount " + totalCharCount;
        //Log.i(TAG, message + message2);

        setUpdated(true);
    }

    public synchronized void deleteTextData(TexturePrimitiveData texturePrimitiveData) {
        texturePrimitiveDataList.remove(texturePrimitiveData);
        int charCount = texturePrimitiveData.vertexData.length / TextRenderer.POSITION_COMPONENT_COUNT * 4;
        totalCharCount -= charCount;
        //String message = "deleted TexturePrimitiveData" + "Char Count : " + charCount + "\n";
        //String message2 = "totalCharCount " + totalCharCount;
        //Log.i(TAG, message + message2);

        setUpdated(true);
    }

    public synchronized void clear() {
        texturePrimitiveDataList.clear();
        setUpdated(true);
    }

    public boolean contains(Object object) {
        return texturePrimitiveDataList.contains(object);
    }

    public void drawTexts() {
        if ( isUpdated()) makeBuffersOne2();

        textRenderer.draw(vertexBuffer, drawOrderBuffer, uvBuffer);
    }

    private void makeBuffersOne() {
        List<Float> vertexDataList = new ArrayList<>();
        List<Short> drawOrderDataList = new ArrayList<>();
        List<Float> uvDataList = new ArrayList<>();
        int drawOrderDataOffset = 0;

        for (TexturePrimitiveData texturePrimitiveData : texturePrimitiveDataList) {
            List<Float> vertexData = new ArrayList<>();
            for (float vertexCoord : texturePrimitiveData.vertexData) {
                vertexData.add(vertexCoord);
            }

            List<Short> drawOrderData = new ArrayList<>();
            for (short drawOrderElement : texturePrimitiveData.drawOrderData) {
                drawOrderData.add((short) (drawOrderElement + drawOrderDataOffset));
            }

            List<Float> uvData = new ArrayList<>();
            for (float uvElement : texturePrimitiveData.uvData) {
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

        for (TexturePrimitiveData texturePrimitiveData : texturePrimitiveDataList) {

            for (Float data : texturePrimitiveData.vertexData) {
                vertexDatas[vertexDataIndex++] = data;
            }

            for (Short data : texturePrimitiveData.drawOrderData) {
                drawOrderDatas[drawOrderDataIndex++] = (short) (data + drawOrderDataOffset);
            }

            for (Float data : texturePrimitiveData.uvData) {
                uvDatas[uvDataIndex++] = data;
            }

            drawOrderDataOffset += (texturePrimitiveData.vertexData.length / TextRenderer.POSITION_COMPONENT_COUNT);
        }

        vertexBuffer = VertexBuffer.arrayAsVertexBuffer(vertexDatas);
        vertexBuffer.position(0);

        drawOrderBuffer = VertexBuffer.arrayAsShortBuffer(drawOrderDatas);
        drawOrderBuffer.position(0);

        uvBuffer = VertexBuffer.arrayAsVertexBuffer(uvDatas);
        uvBuffer.position(0);

            //String message = "Got the textData to the List";
            //Log.i(TAG, message);

        updated = false;
    }

    public void setTextureNumber() {}

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
}