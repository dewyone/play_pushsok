package com.dewy.engine.text.util;

import com.dewy.engine.primitives.Drawable;

import java.util.List;

/**
 * Created by dewyone on 2015-08-20.
 */
public class TextDataList {
    public float [] vertexData;
    public short [] drawOrderData;
    public float [] uvData;

    public final List<Drawable> drawableList;

    public TextDataList(float[] vertexData, short[] drawOrderData, float[] uvData, List<Drawable> drawableList) {
        this.vertexData = vertexData;
        this.drawOrderData = drawOrderData;
        this.uvData = uvData;
        this.drawableList = drawableList;
    }
}
