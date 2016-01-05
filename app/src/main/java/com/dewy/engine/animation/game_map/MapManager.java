package com.dewy.engine.animation.game_map;

import android.util.Log;

import com.dewy.engine.animation.env.Skbl;
import com.dewy.engine.animation.view.unit.TexturePrimitiveData;

import java.util.Map;

import static com.dewy.engine.animation.env.Skbl.B;
import static com.dewy.engine.animation.env.Skbl.BG;
import static com.dewy.engine.animation.env.Skbl.BOX;
import static com.dewy.engine.animation.env.Skbl.EMPTYSPACE;
import static com.dewy.engine.animation.env.Skbl.G;
import static com.dewy.engine.animation.env.Skbl.S;
import static com.dewy.engine.animation.env.Skbl.W;
import static com.dewy.engine.animation.env.Skbl.WK;
import static com.dewy.engine.animation.env.Skbl.WORKER;

/**
 * Created by dewyone on 2015-08-23.
 */
public class MapManager {
    private final static String TAG = "MapManager";

    public static final int MAX_STAGE_NUMBER = 6;

    /*  Retaining map data. These datums are stored in "heap-area"
        Even though activitities could be destroyed by Dalvic vm, these datums "survive"
        Acts as if these are global variables in Dalvic vm contexts
        and activities member methods
    */
    private static final MapData [] mapData = new MapData[MAX_STAGE_NUMBER];


    /* Loading maps into the "static" array mapData. */
    static {
        setMapData();
    }

    public static class MapData {
        private int column;
        private int row;
        private int goalCount;

        private int [] backgroundMap;       // for wall, goal, empty space
        private int [] unitMap;                   // for worker, box

        public MapData(int column, int row, int goalCount, int [] backgroundMap, int [] unitMap) {
            this.column = column;
            this.row = row;
            this.goalCount = goalCount;     // the total of goals
            this.backgroundMap = backgroundMap;
            this.unitMap = unitMap;
        }

        public int getColumn() {
            return column;
        }

        public int getRow() {
            return row;
        }

        public int getGoalCount() { return goalCount;}

        public int[] getBackgroundMap() {
            return backgroundMap;
        }

        public int []  getUnitMap() { return unitMap;}
    }

    public static class GoalUnits {
        private int serial;     // heightInUnit * columnInUnit + column

        private TexturePrimitiveData boxInGoal;
        private TexturePrimitiveData workerInGoal;

        public GoalUnits(int serial) {
            this.serial = serial;
        }

        public GoalUnits(int serial, TexturePrimitiveData boxInGoal, TexturePrimitiveData workerInGoal) {
            this.serial = serial;
            this.boxInGoal = boxInGoal;
            this.workerInGoal = workerInGoal;
        }

        public int getSerial() {
            return serial;
        }

        public TexturePrimitiveData getBoxInGoal() {
            return boxInGoal;
        }

        public void setBoxInGoal(TexturePrimitiveData boxInGoal) {
            this.boxInGoal = boxInGoal;
        }

        public TexturePrimitiveData getWorkerInGoal() {
            return workerInGoal;
        }

        public void setWorkerInGoal(TexturePrimitiveData workerInGoal) {
            this.workerInGoal = workerInGoal;
        }
    }

    private static void setMapData() {
        int mapID = 0;

        // Stage 1
        int column = 19;
        int row = 11;
        int [] data = {
                S,    S,    S,     S,   W,     W,     W,   W,     W,   S,   S,   S,   S,     S,   S,   S,    S,   S,   S,
                S,   S,     S,     S,    W,     S,     S,     S,     W,   S,   S,   S,   S,      S,   S,    S,    S,     S,    S,
                S,   S,     S,     S,    W,    B,     S,     S,     W,    S,   S,   S,   S,     S,    S,   S,    S,      S,     S,
                S,    S,   W,    W,   W,    S,     S,     B,     W,   W,   S,   S,   S,     S,    S,   S,    S,    S,     S,
                S,   S,    W,    S,    S,     B,      S,    B,     S,     W,  S,   S,    S,     S,    S,    S,   S,    S,     S,
                W,  W,   W,   S,    W,    S,     W,   W,     S,     W,   S,  S,    S,     W,   W,  W,   W,   W,   W,
                W,   S,    S,    S,    W,    S,     W,   W,     S,    W,   W, W,   W,   W,   S,    S,    G,   G,     W,
                W,   S,    B,    S,    S,     B,      S,    S,     S,    S,    S,   S,    S,     S,    S,   S,     G,    G,     W,
                W,   W,   W,   W,   W,   S,     W,    W,  W,    S,    W,  WK,  W,  W,  S,   S,     G,    G,    W,
                S,     S,    S,    S,     S,   W,    S,     S,    S,     S,    S,   W,   W,    W,   W,  W,   W,   W,   W,
                S,     S,    S,    S,     S,   W,    W,   W,   W,   W,   W,  W,   S,      S,    S,    S,    S,    S,     S
        };

        setMapData(mapID++, column, row, data);

        // Stage 2
        column = 14;
        row = 10;
        data = new int [] {
                W, W, W, W, W, W, W, W, W, W, W, W, S, S,
                W, G, G, S, S, W, S, S, S, S, S, W, W, W,
                W, G, G, S, S, W, S, B, S, S, B, S, S, W,
                W, G, G, S, S, W, B, W, W, W, W, S, S, W,
                W, G, G, S, S, S, S, WK,S, W, W, S, S, W,
                W, G, G, S, S, W, S, W, S, S, B, S, W, W,
                W, W, W, W, W, W, S, W, W, B, S, B, S, W,
                S, S, W, S, B, S, S, B, S, B, S, B, S, W,
                S, S, W, S, S, S, S, W, S, S, S, S, S, W,
                S, S, W, W, W, W, W, W, W, W, W, W, W, W
        };

        setMapData(mapID++, column, row, data);

        // Stage 3
        column = 17;
        row = 10;
        data = new int [] {
                S, S, S, S, S, S, S, S, W, W, W, W, W, W, W, W, S,
                S, S, S, S, S, S, S, S, W, S, S, S, S, S, WK, W, S,
                S, S, S, S, S, S, S, S, W, S, B, W, B, S, W, W, S,
                S, S, S, S, S, S, S, S, W, S, B, S, S, B, W, S, S,
                S, S, S, S, S, S, S, S, W, W, B, S, B, S, W, S, S,
                W, W, W, W, W, W, W, W, W, S, B, S, W, S, W, W, W,
                W, G, G, G, G, S, S, W, W, S, B, S, S, B, S, S, W,
                W, W, G, G, G, S, S ,S ,S, B, S, S, B, S, S, S, W,
                W, G, G, G, G, S, S, W, W, W, W, W, W, W, W, W, W,
                W, W, W, W, W, W, W, W, S, S, S, S, S, S, S ,S ,S
        };

        setMapData(mapID++, column, row, data);

        // Stage 4
        column = 19;
        row = 13;
        data = new int [] {
                S, S, S, S, S, S, S, S, S, S, S, W, W, W, W, W, W, W, W,
                S, S, S, S, S, S, S, S, S, S, S, W, S, S, G, G, G, G, W,
                W, W, W, W, W, W, W, W ,W, W, W, W, S, S, G, G, G, G, W,
                W, S, S, S, S, W, S, S, B, S, B, S, S, S, G, G, G, G, W,
                W, S, B, B, B, W, B, S, S, B, S, W, S, S, G, G, G, G, W,
                W, S, S, B, S, S, S, S, S, B, S, W, S, S, G, G, G, G, W,
                W, S, B, B, S, W, B, S, B, S, B, W, W, W, W, W, W, W, W,
                W, S, S, B, S, W, S, S, S, S, S, S, W, S, S, S, S, S, S,
                W, W, S, W, W, W, W, W, W, W, W, W, W, S, S, S, S, S, S,
                W, S, S, S, S, W, S, S, S, S, W, W, S, S, S, S, S, S, S,
                W, S, S, S, S, S, B, S, S, S, W, W, S, S, S, S, S, S, S,
                W, S, S, B, B, W, B, B, S, S, WK,W, S, S, S, S, S, S, S,
                W, W, W, W, W, W, W, W ,W, W, W, S, S, S, S, S, S, S, S
        };

        setMapData(mapID++, column, row, data);

        // Stage 5
        column = 8;
        row = 9;
        data = new int[] {
                S,   S,   W,   W,   W,   W,   W,    S,
                W,   W,    W,     S,    S,     S,    W,     S,
                W,   G,   WK,      B,    S,   S,  W,     S,
                W,   W,    W,     S,    B,     G,    W,     S,
                W,   G,    W,     W,    B,     S,    W,     S,
                W,   S,    W,     S,    G,     S,    W,     W,
                W,   B,    S,     BG,    B,     B,    G,     W,
                W,   S,    S,     S,    G,     S,    S,     W,
                W,   W,   W,   W,   W,   W,   W,    W,
        };

        setMapData(mapID++, column, row, data);

        // Stage 6 -- extra
        column = 6;
        row = 5;
        data = new int[] {
                W,    W,     W,     W,   S,     S,
                W,    S,     S,     W,     W,    W,
                W,    WK,  S,     B,     G,     W,
                W,     S,     S,     W,    S,    W,
                W,    W,     W,    W,    W,   W
        };

        setMapData(mapID++, column, row, data);
    }

    private static void setMapData(int mapID, int column, int row, int [] data) {
        int goalCount = 0;
        int [] backgroundData = new int[ column * row];     // for wall, goal, empty space
        int [] unitData = new int[ column * row];           // for worker, box (, empty  space)
        int id = -1;
        for (int i = 0; i < data.length; i ++) {
            id = data[i];

            if (id == Skbl.WKG || id == Skbl.BG) {      // when worker or box is in goal
                backgroundData[i] = Skbl.GOALS;
            } else if (id == BOX || id == WORKER) {            // when worker or box is in empty space
                backgroundData[i] = Skbl.EMPTYSPACE;
            } else backgroundData[i] = id;        //  background with nothing (wall, goal, empty space )

            // when worker or box is in goal
            if (id == Skbl.WKG) unitData[i] = Skbl.WORKER;
            else if (id == Skbl.BG) unitData[i] = Skbl.BOX;
            else if ( id == Skbl.WORKER || id == Skbl.BOX) unitData[i] = id;            // when worker or box is in empty space
            else { unitData[i] = EMPTYSPACE;}

            if (id == Skbl.GOALS || id == Skbl.BOXINGOAL || id == Skbl.WORKERINGOAL) goalCount++;
        }

        //String logMessage;
        mapData[mapID] = new MapData(column, row, goalCount, backgroundData, unitData);
    }

    public static MapData getMapData(int mapNumber) {
        if (mapNumber > -1 && mapNumber < MAX_STAGE_NUMBER) {
            MapData base = mapData[mapNumber];
            MapData mapDataCloned = new MapData(base.column, base.row, base.goalCount,
                    base.backgroundMap.clone(), base.unitMap.clone());
            return mapDataCloned;
        }
        else return null;
    }
}