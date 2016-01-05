package com.dewy.engine.primitives;

import java.util.List;

/**
 * Created by dewyone on 2015-08-15.
 */
public class PrimitiveData {
    public final float[] vertexData;
    public final List<Drawable> drawableList;

    PrimitiveData(float[] vertexData, List<Drawable> drawableList) {
        this.vertexData = vertexData;
        this.drawableList = drawableList;
    }
}
