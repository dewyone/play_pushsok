package com.dewy.engine.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glBufferSubData;
import static android.opengl.GLES20.glDeleteBuffers;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.dewy.engine.platform.GLContext.BYTES_PER_FLOAT;

/**
 * Created by dewyone on 2015-08-12.
 */
public class VertexBufferObject {

    private final int bufferId;

    public VertexBufferObject(float[] vertexData) {
        /* *** Basic Usage ***
        // The type used for names in OpenGL is GLuint
        GLuint buffer;
        // Generate a name for the buffer
        glGenBuffers(1, &buffer);
        // Now bind it to the context using the GL_ARRAY_BUFFER binding point
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        // Specify the amount of storage we want to use for the buffer
        glBufferData(GL_ARRAY_BUFFER, 1024 * 1024, NULL, GL_STATIC_DRAW);
        */

        final int buffers[] = new int[1];
        glGenBuffers(buffers.length, buffers, 0);       // reserving a name for a buffer // create a new buffer object
        if (buffers[0] == 0) { throw new RuntimeException("Could not create a new vertex buffer object."); }
        bufferId = buffers[0];

        // binding the name of a buffer to a buffer binding point (targets)
        // GL_ARRAY_BUFFER : purpose - vertex attributes
        // GL_ELEMENT_ARRAY_BUFFER : purpose - vertex array indices
        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
        // Transfer data to native memory.
        FloatBuffer vertexArray = ByteBuffer.allocateDirect(vertexData.length * BYTES_PER_FLOAT) .
                order(ByteOrder.nativeOrder()) .asFloatBuffer() .put(vertexData); vertexArray.position(0);
        // Transfer data from native memory to the GPU buffer.
        glBufferData(GL_ARRAY_BUFFER, vertexArray.capacity() * BYTES_PER_FLOAT, vertexArray, GL_STATIC_DRAW);
        // IMPORTANT: Unbind from the buffer when we're done with it.
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public VertexBufferObject(int sizeInByte) {
        final int buffers[] = new int[1];
        glGenBuffers(buffers.length, buffers, 0);       // reserving a name for a buffer // create a new buffer object
        if (buffers[0] == 0) { throw new RuntimeException("Could not create a new vertex buffer object."); }
        bufferId = buffers[0];

        // binding the name of a buffer to a buffer binding point (targets)
        // GL_ARRAY_BUFFER : purpose - vertex attributes
        // GL_ELEMENT_ARRAY_BUFFER : purpose - vertex array indices
        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
        glBufferData(GL_ARRAY_BUFFER, sizeInByte, null, GL_STATIC_DRAW);
        // IMPORTANT: Unbind from the buffer when we're done with it.
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void bufferSubData(float [] vertexData, int offsetInBytes) {
        // Put the data into the buffer at offset
        // Transfer data from native memory to the GPU buffer.
        FloatBuffer vertexArray = ByteBuffer.allocateDirect(vertexData.length * BYTES_PER_FLOAT) .
                order(ByteOrder.nativeOrder()) .asFloatBuffer() .put(vertexData); vertexArray.position(0);
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferSubData(GL_ARRAY_BUFFER, offsetInBytes, vertexData.length * BYTES_PER_FLOAT, vertexArray);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void setVertexAttribPointer(int attributeLocation, int componentCount, int stride, int dataOffset) {
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT, false, stride, dataOffset);
        glEnableVertexAttribArray(attributeLocation);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void deleteBuffer() {
        final int buffers[] = new int[1];
        buffers[0] = bufferId;
        glDeleteBuffers(buffers.length, buffers, 0);
    }

}
