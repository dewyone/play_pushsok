package com.dewy.engine.animation.unit;

/**
 * Created by dewyone on 2015-08-28.
 */
public class UnitVector {
    private int column;
    private int row;
    private int direction;

    public UnitVector(int column, int row, int direction) {
        this.column = column;
        this.row = row;
        this.direction = direction;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public int getDirection() {
        return direction;
    }
}
