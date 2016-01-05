package com.dewy.engine.animation.unit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dewyone on 2015-08-28.
 */
public class Transaction {
    private final List<UnitVector> unitVectorList;

    public Transaction() {
        unitVectorList = new ArrayList<>();
    }

    public List<UnitVector> getUnitVectorList() {
        return unitVectorList;
    }
}
