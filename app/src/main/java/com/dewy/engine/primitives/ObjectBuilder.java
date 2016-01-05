package com.dewy.engine.primitives;

import android.opengl.Matrix;
import android.util.FloatMath;

import com.dewy.engine.data.VertexBuffer;
import com.dewy.engine.util.Geometry;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glDrawElements;

/**
 * Created by dewyone on 2015-07-27.
 */
public class ObjectBuilder {

    private final float [] vertexData;
    private final List<Drawable> drawList = new ArrayList<>();

    private static final int FLOATS_PER_VERTEX = 3;
    private int offset = 0;

    private ObjectBuilder(int sizeInVertex) {
        vertexData = new float[sizeInVertex * FLOATS_PER_VERTEX];
    }

    private static int sizeOfCircleInVertices(int numPoints) {
        return 1 + (numPoints + 1);
    }

    public static PrimitiveData createTriangle(Geometry.Point point1, Geometry.Point point2, Geometry.Point point3) {
        ObjectBuilder triangleBuilder = new ObjectBuilder(3);

        triangleBuilder.appendTriangle(new Geometry.Triangle(point1, point2, point3));

        return triangleBuilder.buildPrimitive();
    }

    public static PrimitiveData createRectangle(Geometry.Point point, Geometry.Vector vector, float width, float height) {
        ObjectBuilder rectangleBuilder = new ObjectBuilder(4);

        rectangleBuilder.appendRectangle(new Geometry.Rectangle(point, vector, width, height));

        return rectangleBuilder.buildPrimitive();
    }

    public static PrimitiveData createSquare(Geometry.Point point, Geometry.Vector vector, float length) {
        ObjectBuilder squareBuilder = new ObjectBuilder(4);

        squareBuilder.appendSquare(new Geometry.Square(point, vector, length));

        return squareBuilder.buildPrimitive();
    }

    public static PrimitiveData createCircle(float radius, int numPoints) {
        int vertexCount = 1 + numPoints + 1;
        ObjectBuilder builder = new ObjectBuilder(vertexCount);
        builder.appendCircle(new Geometry.Circle(new Geometry.Point(0.0f, 0.0f, 0.0f), radius), numPoints);

        return builder.buildPrimitive();
    }

    public static PrimitiveData createCube(float length) {
        ObjectBuilder cubeBuilder = new ObjectBuilder(24);
        float hLength = length / 2;
        cubeBuilder.appendSquare(new Geometry.Square(new Geometry.Point(0, hLength, 0), new Geometry.Vector(0, 1, 0), length));   // top
        cubeBuilder.appendSquare(new Geometry.Square(new Geometry.Point(-hLength, 0, 0), new Geometry.Vector(-1, 0, 0), length));   // left
        cubeBuilder.appendSquare(new Geometry.Square(new Geometry.Point(0, -hLength, 0), new Geometry.Vector(0, -1, 0), length));   // bottom
        cubeBuilder.appendSquare(new Geometry.Square(new Geometry.Point(hLength, 0, 0), new Geometry.Vector(1, 0, 0), length));   // right
        cubeBuilder.appendSquare(new Geometry.Square(new Geometry.Point(0, 0, hLength), new Geometry.Vector(0, 0, 1), length));   // front, near
        cubeBuilder.appendSquare(new Geometry.Square(new Geometry.Point(0, 0, -hLength), new Geometry.Vector(0, 0, -1), length));   // back, far

        //cubeBuilder.appendCube(length);
        return cubeBuilder.buildPrimitive();
    }

    public static PrimitiveData createInwardCube(float length) {
        ObjectBuilder inwardCubeBuilder = new ObjectBuilder(8);

        inwardCubeBuilder.appendInwardCube(length);

        return inwardCubeBuilder.buildPrimitive();
    }

    public static PrimitiveData[] createCubeSixSide(float length) {
        final PrimitiveData[] primitiveDatas = new PrimitiveData[6];
        float hLength = length / 2;

        primitiveDatas[0] = createSquare(new Geometry.Point(0, hLength, 0), new Geometry.Vector(0, 1, 0), length);
        primitiveDatas[1] = createSquare(new Geometry.Point(-hLength, 0, 0), new Geometry.Vector(-1, 0, 0), length);
        primitiveDatas[2] = createSquare(new Geometry.Point(0, -hLength, 0), new Geometry.Vector(0, -1, 0), length);
        primitiveDatas[3] = createSquare(new Geometry.Point(hLength, 0, 0), new Geometry.Vector(1, 0, 0), length);
        primitiveDatas[4] = createSquare(new Geometry.Point(0, 0, hLength), new Geometry.Vector(0, 0, 1), length);       // front, near
        primitiveDatas[5] = createSquare(new Geometry.Point(0, 0, -hLength), new Geometry.Vector(0, 0, -1), length);     // back, far

        return primitiveDatas;
    }

    public void appendTriangle(Geometry.Triangle triangle) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int vertexCount = 3;

        for (int i = 0; i < vertexCount; i++) {
            vertexData[0 + i * vertexCount] = triangle.points[i].x;
            vertexData[1 + i * vertexCount] = triangle.points[i].y;
            vertexData[2 + i * vertexCount] = triangle.points[i].z;
        }

        drawList.add(new Drawable() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLES, startVertex, vertexCount);
            }
        });
    }

    public void appendInwardCube(float length) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int vertexCount = 8;

        // 1. define vertex coordinates
        // 2. define the order of drawing
        // 3. create and add a drawable

        float hLength = length / 2;
        float [] data = {
                -hLength, hLength, -hLength,    // top - left - far
                -hLength, hLength, hLength,     // top - left - near
                hLength, hLength, hLength,      // top - right - near
                hLength, hLength, -hLength,     // top - right - far

                -hLength, -hLength, -hLength,    // bottom - left - far
                -hLength, -hLength, hLength,     // bottom - left - near
                hLength, -hLength, hLength,      // bottom - right - near
                hLength, -hLength, -hLength     // bottom - right - far
        };

        for (int i = 0; i < vertexData.length; i++) {
            vertexData[i] = data[i];
        }

        final byte [] drawOrderList = {
                1, 5, 4,   1, 4, 0,     // left
                3, 7, 6,   3, 6, 2,     // right
                4, 5, 6,   4, 6, 7,     // bottom
                3, 2, 1,   3, 1, 0,     // top
                2, 6, 1,   1, 6, 5,     // near
                0, 4, 7,   0, 7, 3      // far
        };

        final ByteBuffer orderBuffer = VertexBuffer.arrayAsByteBuffer(drawOrderList);
        orderBuffer.position(0);

        drawList.add(new Drawable() {
            @Override
            public void draw() {
                glDrawElements(GL_TRIANGLES, drawOrderList.length, GL_UNSIGNED_BYTE, orderBuffer);
            }
        });
    }

    public void appendSquare(Geometry.Square square) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final float hLength = square.length / 2;
        float [] baseRectM = new float[16];
        final int vertexCount = 4;

        // when vector is (0, 1, 0)
        // top - far - left
        baseRectM[0] = -hLength;
        baseRectM[4] = 0;
        baseRectM[8] = -hLength;
        baseRectM[12] = 1.0f;

        // top - near - left
        baseRectM[1] = -hLength;
        baseRectM[5] = 0;
        baseRectM[9] = +hLength;
        baseRectM[13] = 1.0f;

        // top - near - right
        baseRectM[2] = +hLength;
        baseRectM[6] = 0;
        baseRectM[10] = +hLength;
        baseRectM[14] = 1.0f;

        // top - far - right
        baseRectM[3] = +hLength;
        baseRectM[7] = 0;
        baseRectM[11] = -hLength;
        baseRectM[15] = 1.0f;

        if (square.vector.vx == 1 && square.vector.vy == 0 &&
                square.vector.vz == 0) { Matrix.rotateM(baseRectM, 0, 90, 0, 0, 1); }
        if (square.vector.vx == -1 && square.vector.vy == 0 &&
                square.vector.vz == 0) { Matrix.rotateM(baseRectM, 0, -90, 0, 0, 1); }
        if (square.vector.vx == 0 && square.vector.vy == 1 &&
                square.vector.vz == 0) { Matrix.rotateM(baseRectM, 0, 0, 1, 0, 0); }
        if (square.vector.vx == 0 && square.vector.vy == -1 &&
                square.vector.vz == 0) { Matrix.rotateM(baseRectM, 0, 180, 1, 0, 0); }
        if (square.vector.vx == 0 && square.vector.vy == 0 &&
                square.vector.vz == 1) { Matrix.rotateM(baseRectM, 0, -90, 1, 0, 0); }
        if (square.vector.vx == 0 && square.vector.vy == 0 &&
                square.vector.vz == -1) { Matrix.rotateM(baseRectM, 0, 90, 1, 0, 0); }


        /*
        for (int i = 0; i < baseRectM.length; i++) {
            String info = i + " th value : " + String.valueOf( baseRectM[i] );
            Log.i("After rotating", info);
        } */

        for (int i = 0; i < vertexCount; i++) {

            vertexData[offset++] = baseRectM[i + 0] + square.center.x * 1.0f;
            //Log.i("vertexData[]", String.valueOf( baseRectM[i + 0] ));
            vertexData[offset++] = baseRectM[i + 4] + square.center.y * 1.0f;
            //Log.i("vertexData[]", String.valueOf( baseRectM[i + 4] ) );
            vertexData[offset++] = baseRectM[i + 8] + square.center.z * 1.0f;
            //Log.i("vertexData[]", String.valueOf( baseRectM[i + 8] ));
        }


        drawList.add(new Drawable() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, vertexCount);
            }
        });
    }

    public void appendRectangle(Geometry.Rectangle rectangle) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final float hWidth = rectangle.width / 2;
        final float hHeight = rectangle.height / 2;
        float [] baseRectM = new float[16];
        final int vertexCount = 4;

        // when vector is (0, 1, 0)
        // top - far - left
        baseRectM[0] = -hWidth;  baseRectM[4] = 0; baseRectM[8] = -hHeight;  baseRectM[12] = 1.0f;

        // top - near - left
        baseRectM[1] = -hWidth; baseRectM[5] = 0; baseRectM[9] = +hHeight; baseRectM[13] = 1.0f;

        // top - near - right
        baseRectM[2] = +hWidth; baseRectM[6] = 0; baseRectM[10] = +hHeight; baseRectM[14] = 1.0f;

        // top - far - right
        baseRectM[3] = +hWidth; baseRectM[7] = 0; baseRectM[11] = -hHeight; baseRectM[15] = 1.0f;

        if (rectangle.vector.vx == 1 && rectangle.vector.vy == 0 &&
                rectangle.vector.vz == 0) { Matrix.rotateM(baseRectM, 0, 90, 0, 0, 1); }
        if (rectangle.vector.vx == -1 && rectangle.vector.vy == 0 &&
                rectangle.vector.vz == 0) { Matrix.rotateM(baseRectM, 0, -90, 0, 0, 1); }
        if (rectangle.vector.vx == 0 && rectangle.vector.vy == 1 &&
                rectangle.vector.vz == 0) { Matrix.rotateM(baseRectM, 0, 0, 1, 0, 0); }
        if (rectangle.vector.vx == 0 && rectangle.vector.vy == -1 &&
                rectangle.vector.vz == 0) { Matrix.rotateM(baseRectM, 0, 180, 1, 0, 0); }
        if (rectangle.vector.vx == 0 && rectangle.vector.vy == 0 &&
                rectangle.vector.vz == 1) { Matrix.rotateM(baseRectM, 0, -90, 1, 0, 0); }
        if (rectangle.vector.vx == 0 && rectangle.vector.vy == 0 &&
                rectangle.vector.vz == -1) { Matrix.rotateM(baseRectM, 0, 90, 1, 0, 0); }


        /*
        for (int i = 0; i < baseRectM.length; i++) {
            String info = i + " th value : " + String.valueOf( baseRectM[i] );
            Log.i("Creating Rect,After rot", info);
        }
        */

        // Caucious : After rotation, we translate the model, not before.
        //                When you first put objects, you should consider that.
        for (int i = 0; i < vertexCount; i++) {

            vertexData[offset++] = baseRectM[i + 0] + rectangle.center.x * 1.0f;
            //Log.i("vertexData[]", String.valueOf( baseRectM[i + 0] ));
            vertexData[offset++] = baseRectM[i + 4] + rectangle.center.y * 1.0f;
            //Log.i("vertexData[]", String.valueOf( baseRectM[i + 4] ) );
            vertexData[offset++] = baseRectM[i + 8] + rectangle.center.z * 1.0f;
            //Log.i("vertexData[]", String.valueOf( baseRectM[i + 8] ));
        }


        drawList.add(new Drawable() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, vertexCount);
            }
        });
    }

    private void appendCircle(Geometry.Circle circle, int numPoints) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfCircleInVertices(numPoints);
        // Center point of fan
        vertexData[offset++] = circle.center.x;
        vertexData[offset++] = circle.center.y;
        vertexData[offset++] = circle.center.z;
        // Fan around center point. <= is used because we want to generate
        // the point at the starting angle twice to complete the fan.
        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);
            vertexData[offset++] = circle.center.x + circle.radius * (float) Math.cos(angleInRadians);
            vertexData[offset++] = circle.center.y;
            vertexData[offset++] = circle.center.z + circle.radius * (float) Math.sin(angleInRadians);
        }

        drawList.add(new Drawable() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertices);
            }
        });
    }

    private void appendCube(float length){
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final float hLength = length / 2;
        final int numVertices = 8;

        // top - far - left
        vertexData[offset++] = -hLength;
        vertexData[offset++] = hLength;
        vertexData[offset++] = -hLength;

        // top - near - left
        vertexData[offset++] = -hLength;
        vertexData[offset++] = hLength;
        vertexData[offset++] = hLength;

        // top - near - right
        vertexData[offset++] = hLength;
        vertexData[offset++] = hLength;
        vertexData[offset++] = hLength;

        // top - far - right
        vertexData[offset++] = hLength;
        vertexData[offset++] = hLength;
        vertexData[offset++] = -hLength;

        // bottom - far - right
        vertexData[offset++] = hLength;
        vertexData[offset++] = -hLength;
        vertexData[offset++] = -hLength;

        // bottom - far - left
        vertexData[offset++] = -hLength;
        vertexData[offset++] = -hLength;
        vertexData[offset++] = -hLength;

        // bottom - near - right
        vertexData[offset++] = -hLength;
        vertexData[offset++] = -hLength;
        vertexData[offset++] = hLength;

        // top - near - left
        vertexData[offset++] = -hLength;
        vertexData[offset++] = hLength;
        vertexData[offset++] = hLength;

        drawList.add(new Drawable() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertices);
            }
        });

        final int startVertex2 = offset / FLOATS_PER_VERTEX;

        // bottom - near - right
        vertexData[offset++] = hLength;
        vertexData[offset++] = -hLength;
        vertexData[offset++] = hLength;

        // bottom - near - left
        vertexData[offset++] = -hLength;
        vertexData[offset++] = -hLength;
        vertexData[offset++] = hLength;

        // bottom - far - left
        vertexData[offset++] = -hLength;
        vertexData[offset++] = -hLength;
        vertexData[offset++] = -hLength;

        // bottom - far - right
        vertexData[offset++] = hLength;
        vertexData[offset++] = -hLength;
        vertexData[offset++] = -hLength;

        // top - far - right
        vertexData[offset++] = hLength;
        vertexData[offset++] = hLength;
        vertexData[offset++] = -hLength;

        // top - near - right
        vertexData[offset++] = hLength;
        vertexData[offset++] = hLength;
        vertexData[offset++] = hLength;

        // top - near - left
        vertexData[offset++] = -hLength;
        vertexData[offset++] = hLength;
        vertexData[offset++] = hLength;

        // bottom - near - left
        vertexData[offset++] = -hLength;
        vertexData[offset++] = -hLength;
        vertexData[offset++] = hLength;

        drawList.add(new Drawable() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex2, numVertices);
            }
        });
    }

    private PrimitiveData buildPrimitive() {
        return new PrimitiveData(vertexData, drawList);
    }
}
