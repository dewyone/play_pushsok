package com.dewy.engine.animation.view.unit;

import com.dewy.engine.primitives.Drawable;

/**
 * Created by dewyone on 2015-08-20.
 */
public class TexturePrimitiveData {
    public float [] vertexData;
    public short [] drawOrderData;
    public float [] uvData;

    public final Drawable drawable;

    public TexturePrimitiveData(float[] vertexData, short[] drawOrderData, float[] uvData, Drawable drawable) {
        this.vertexData = vertexData;
        this.drawOrderData = drawOrderData;
        this.uvData = uvData;
        this.drawable = drawable;
    }

    // !!       should be tested      !!
    @Override
    public TexturePrimitiveData clone() throws CloneNotSupportedException {
        TexturePrimitiveData texturePrimitiveData = new TexturePrimitiveData(vertexData, drawOrderData, uvData, drawable);
        return texturePrimitiveData;
    }

    /**
     *
     * @param targetOfCopy
     * @return targetOfCopy which is the same object that we got as param
     */
    public TexturePrimitiveData copy(TexturePrimitiveData targetOfCopy) {
        for (int i = 0; i < vertexData.length; i++) {
            targetOfCopy.vertexData[i] = vertexData[i];
        }
        for (int i = 0; i < drawOrderData.length; i++) {
            targetOfCopy.drawOrderData[i] = drawOrderData[i];
        }
        for (int i = 0; i < uvData.length; i++) {
            targetOfCopy.uvData[i] = uvData[i];
        }

        return targetOfCopy;
    }
}
