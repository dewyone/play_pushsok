package com.dewy.engine.text.unit;

/**
 * Created by dewyone on 2015-11-03.
 */
public class GLCharacter {
    private short id;
    private float xPos;
    private float yPos;
    private float size;     // font size

    /**
     * vertex data could be get by calculation
     * uv data could be get by id
     * drawOrder is same to all rectangles
     * drawable also can be made same to all rectangles
     * so, we need id, position of x and y, size of font
     */

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public float getxPos() {
        return xPos;
    }

    public void setxPos(float xPos) {
        this.xPos = xPos;
    }

    public float getyPos() {
        return yPos;
    }

    public void setyPos(float yPos) {
        this.yPos = yPos;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }
}
