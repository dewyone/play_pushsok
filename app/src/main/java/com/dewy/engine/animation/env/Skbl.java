package com.dewy.engine.animation.env;

/**
 * Created by dewyone on 2015-08-23.
 * Sokoban Language
 */
public class Skbl {     // Sokoban Language

    public static final int EMPTYSPACE = 1; public static final int S = 1;

    public static final int WORKER = 0; public static final int WK = 0;

    public static final int WALL = 2; public static final int W = 2;

    public static final int GOALS = 5; public static final int G = 5;

    public static final int BOX = 3; public static final int B = 3;

    public static final int BOXINGOAL = 4; public static final int BG = 4;

    public static final int WORKERINGOAL = 6; public static final int WKG =6;

    public static final String STAGENUMBER = "Stage Number";

    public static final String NEWSTAGE = "New Stage";

    public static final String  SOUNDON = "Sound On";

    public static final String LASTACTIVITYCLASSNAME = "ActivityClassName";

    public static String getNameOfUnitID(int unitID) {
        switch (unitID) {
            case EMPTYSPACE : return "emptySpace";
            case WORKER : return "worker";
            case WALL : return "wall";
            case GOALS : return "goals";
            case BOX : return "box";
            case BOXINGOAL : return "boxInGoal";
            case WORKERINGOAL : return "workerInGoal";
            default: return null;
        }
    }

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int UP = 2;
    public static final int DOWN = 3;

    public static String getNameOfDirectionID(int directionID) {
        switch (directionID) {
            case LEFT : return "Left";
            case RIGHT : return "Right";
            case UP : return "Up";
            case DOWN : return "Down";
            default: return null;
        }
    }
}
