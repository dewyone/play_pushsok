package com.dewy.engine.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_SHORT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glBufferSubData;
import static android.opengl.GLES20.glDeleteBuffers;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.dewy.engine.platform.GLContext.BYTES_PER_SHORT;

/**
 * Created by dewyone on 2015-08-12.
 */
public class IndexBufferObject {
    private final int bufferId;

    public IndexBufferObject(short[] indexData) {

        final int buffers[] = new int[1];
        glGenBuffers(buffers.length, buffers, 0);       // reserving a name for a buffer // create a new buffer object
        if (buffers[0] == 0) { throw new RuntimeException("Could not create a new vertex buffer object."); }
        bufferId = buffers[0];

        // binding the name of a buffer to a buffer binding point (targets)
        // GL_ELEMENT_ARRAY_BUFFER : purpose - vertex array indices
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[0]);
        // Transfer data to native memory.
        ShortBuffer elementArray = ByteBuffer.allocateDirect(indexData.length * BYTES_PER_SHORT) .
                order(ByteOrder.nativeOrder()) .asShortBuffer() .put(indexData);
        elementArray.position(0);
        // Transfer data from native memory to the GPU buffer.
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementArray.capacity() * BYTES_PER_SHORT, elementArray, GL_STATIC_DRAW);
        // IMPORTANT: Unbind from the buffer when we're done with it.
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public IndexBufferObject(int sizeInByte) {

        final int buffers[] = new int[1];
        glGenBuffers(buffers.length, buffers, 0);       // reserving a name for a buffer // create a new buffer object
        if (buffers[0] == 0) { throw new RuntimeException("Could not create a new vertex buffer object."); }
        bufferId = buffers[0];

        // binding the name of a buffer to a buffer binding point (targets)
        // GL_ELEMENT_ARRAY_BUFFER : purpose - vertex array indices
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[0]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeInByte, null, GL_STATIC_DRAW);
        // IMPORTANT: Unbind from the buffer when we're done with it.
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void bufferSubData(short [] drawOrderData, int offset) {
        // Put the data into the buffer at offset
        // Transfer data to native memory.
        ShortBuffer drawOrderArray = ByteBuffer.allocateDirect(drawOrderData.length * BYTES_PER_SHORT) .
                order(ByteOrder.nativeOrder()) .asShortBuffer() .put(drawOrderData); drawOrderArray.position(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferId);
        glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, offset, drawOrderData.length * BYTES_PER_SHORT, drawOrderArray);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void setVertexAttribPointer(int attributeLocation, int componentCount, int stride, int dataOffset) {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferId);
        glVertexAttribPointer(attributeLocation, componentCount, GL_SHORT, false, stride, dataOffset);
        glEnableVertexAttribArray(attributeLocation);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void deleteBuffer() {
        final int buffers[] = new int[1];
        buffers[0] = bufferId;
        glDeleteBuffers(buffers.length, buffers, 0);
    }

    public int getBufferId() { return bufferId; }

}
