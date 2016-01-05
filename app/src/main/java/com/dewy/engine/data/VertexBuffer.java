package com.dewy.engine.data;

import com.dewy.engine.util.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by dewyone on 2015-08-06.
 */
public class VertexBuffer {
    private final FloatBuffer vertexBuffer;
    public FloatBuffer getVertexBuffer() { return vertexBuffer;}

    public VertexBuffer(float[] vertexData) {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * Constants.BYTES_PER_FLOAT) .
                order(ByteOrder.nativeOrder()) .asFloatBuffer() .put(vertexData);
    }

    public static FloatBuffer arrayAsVertexBuffer(float [] array) {
        return ByteBuffer.allocateDirect(array.length * Constants.BYTES_PER_FLOAT) .
                order(ByteOrder.nativeOrder()) .asFloatBuffer() .put(array);
    }

    public static ShortBuffer arrayAsShortBuffer(short [] array) {
        return ByteBuffer.allocateDirect(array.length * Constants.BYTES_PER_SHORT).
                order(ByteOrder.nativeOrder()).asShortBuffer().put(array);
    }

    public static ByteBuffer arrayAsByteBuffer(byte [] array) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(6 * 6).put(array);

        return byteBuffer;
    }
}
