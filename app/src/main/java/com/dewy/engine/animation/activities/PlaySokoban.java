package com.dewy.engine.animation.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dewy.engine.R;
import com.dewy.engine.animation.activities.lib.Sounder;
import com.dewy.engine.animation.activities.lib.ColorAlphaGradientor;
import com.dewy.engine.animation.activities.lib.ImageButtoner11;
import com.dewy.engine.animation.media.SoundPlayer;
import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.SkbIntent;
import com.dewy.engine.animation.env.Skbl;
import com.dewy.engine.animation.game_map.MapManager;
import com.dewy.engine.animation.unit.Transaction;
import com.dewy.engine.animation.unit.UnitVector;
import com.dewy.engine.animation.view.StaticUnitImageManager;
import com.dewy.engine.animation.view.UnitImageManager;
import com.dewy.engine.animation.view.unit.TexturePrimitiveData;
import com.dewy.engine.text.TextManager;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dewyone on 2015-09-04.
 */
public class PlaySokoban extends GLActivity {

    private static final String TAG = "PlaySokoban";
    private static final boolean DEBUG_LIFECYCLE = true;

    private boolean autoMode = false;
    private boolean artificialIntelliMode = false;

    private final GLActivity platform;
    private final GLContext glContext;
    private final Context context;

    private PowerManager.WakeLock wakeLock;
    private long activityStartTime;

    private SoundPlayer soundPlayer;
    private StageComplete stageComplete;

    private UnitImageManager um;
    private StaticUnitImageManager sum;

    private float screenWidth;
    private float screenHeight;
    private final float [] viewMatrix = new float[16];
    private final float [] orthographicMatrix = new float[16];
    private final float [] viewProjectionMatrix = new float[16];

    private int [] backgroundMap;
    private int [] unitMap;
    private TexturePrimitiveData[] backgroundUnits;        // for walls, goals, empty space
    private TexturePrimitiveData[] units;   // for boxes, worker..
    private List<MapManager.GoalUnits> goalUnitsList;       // for boxInGoals, workerInGoals (second background)

    private final List<Transaction> transactionList = new ArrayList<>();

    private Bundle psBundle;        // Play Sokoban bundle
    private int stageNumPlay = -1;      // stage number   /*  caution    :    this number starts from 0 */
    private boolean newStage = true;

    private float boardStartX;
    private float boardStartY;

    private int mapColumnInUnit;
    private int mapRowInUnit;
    private int totalGoalCount;
    private float mapUnitSize;

    private ButtonManager bttnMngr;
    //private ImageButtoner11 nextLevelButton;
    //private ImageButtoner11 previousLevelButton;

    //private float nextLevelXpos = 0.45f;
    //private float nextLevelYpos = 0.80f;
    //private float levelButtonWidth = 0.5f;
    //private float levelButtonHeight = 0.25f;
    //private float previousLevelXpos = -nextLevelXpos;
    //private float previousLevelYpos = nextLevelYpos;

    private int playerColumn;
    private int playerRow;

    /*  controllers*/
    private boolean allBoxInGoals = false;

    public PlaySokoban(GLActivity platform) {
        String logMessage = "PlaySokoban is instanciated..";
        Log.i(TAG, logMessage);
        this.platform = platform;
        this.glContext = platform.glContext;
        this.context = glContext.getContext();
    }

    @Override
    public void onCreate(Bundle psBundle) {     // Play Sokoban bundle
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onCreate() is called...";
            Log.i(TAG, logMessage);
        }
        super.onCreate(psBundle);

        this.psBundle = psBundle;
        activityStartTime = System.nanoTime();

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        // Most applications should use FLAG_KEEP_SCREEN_ON instead of this type of wake lock
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");

        soundPlayer = new SoundPlayer(context);
        stageComplete = new StageComplete();
    }

    /* re-creates, and recreating is a kind of policy */
    /* usually when pre or next button is clicked.. */
    @Override
    public void onRestart() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onRestart() is called...";
            Log.i(TAG, logMessage);
        }
        super.onRestart();
    }

    @Override
    public void onStart(){
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStart() is called...";
            Log.i(TAG, logMessage);
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onResume() is called...";
            Log.i(TAG, logMessage);
        }
        super.onResume();

        wakeLock.acquire();
    }

    @Override
    public void onPause() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onPause() is called...";
            Log.i(TAG, logMessage);
        }
        super.onPause();

        wakeLock.release();
    }

    @Override
    public void onStop() {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onStop() is called...";
            Log.i(TAG, logMessage);
        }
        super.onStop();

        // save the game state
        saveState();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSurfaceCreated() is called...";
            Log.i(TAG, logMessage);
        }

        //* if we could retain EGL Context, the following code is not necessary
        int textureUnitNumber = initTextureUnitForNextLevelButton();
        //nextLevelButton = new ImageButtoner11(glContext,
        //        nextLevelXpos, nextLevelYpos, levelButtonWidth, levelButtonHeight, textureUnitNumber);
        //textureUnitNumber = initTextureUnitForPreviousLevelButton();
        //previousLevelButton = new ImageButtoner11(glContext,
        //        previousLevelXpos, previousLevelYpos, levelButtonWidth, levelButtonHeight, textureUnitNumber);
        //previousLevelButton.setColorAlpha(0.2f);
        //nextLevelButton.setColorAlpha(0.2f);

        bttnMngr = new ButtonManager();
        //*

        boolean activityCreated = ! isRestarted();
        if ( activityCreated) {         // renew the stage when game first starts or replay button is clicked

            // loading game info (stage number) if we doesn't have one
            if (psBundle == null) psBundle = getPSBundle();     // when game first starts and when player have history of playing

            /* at this point, we should have a psBundle */
            stageNumPlay = psBundle.getInt(Skbl.STAGENUMBER) - 1;
            newStage = psBundle.getBoolean(Skbl.NEWSTAGE);
            psBundle = null;
            logMessage = "from bundle, stageNumPlay : " + stageNumPlay;
            Log.i(TAG, logMessage);

            if (stageNumPlay == 6) artificialIntelliMode = true;

            logMessage = "isRestarted() : " + isRestarted();
            Log.i(TAG, logMessage);

            prepareMapData(stageNumPlay);
            setPlayerColumnRow();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        String logMessage;
        if (DEBUG_LIFECYCLE) {
            logMessage = "onSurfaceChanged() is called...";
            Log.i(TAG, logMessage);
        }

        screenWidth = (float) width;
        screenHeight = (float) height;
        float aspectRatio = screenWidth / screenHeight;
/*
        //String logMessage = "-aspectRatio : " + (-aspectRatio) + ", aspectRation : " + aspectRatio;
        //Log.i(TAG, logMessage);
        if ( aspectRatio < 1 ) {        // height > width
            logMessage = "screen height > width, setting orthoMatrix";
            Log.i(TAG, logMessage);
            //Matrix.orthoM(orthographicMatrix, 0, -1, 1, -1 / aspectRatio, 1 / aspectRatio, -1, 1);
            Matrix.orthoM(orthographicMatrix, 0, -1, 1, -aspectRatio, aspectRatio, -1, 1);      // shrink height before adapted to screen
        } else {        // width > height,
            logMessage = "screen height < width, setting orthoMatrix";
            Log.i(TAG, logMessage);
            //Matrix.orthoM(orthographicMatrix, 0, -1, 1, -1 / (aspectRatio - 0.3f), 1 / (aspectRatio - 0.3f), -1, 1);      expand height before adapted to screen
            Matrix.orthoM(orthographicMatrix, 0, -aspectRatio, aspectRatio, -1 , 1, -1, 1);     // shrink width before adapted to screen
        }
        //Matrix.orthoM(orthographicMatrix, 0, -1, 1, -aspectRatio, aspectRatio, -1, 1);
*/
        float shrinkRatio;

        //String logMessage = "-aspectRatio : " + (-aspectRatio) + ", aspectRation : " + aspectRatio;
        //Log.i(TAG, logMessage);
        if ( height > width ) {        // width < height
            shrinkRatio = screenHeight / screenWidth;
            logMessage = "screen height > width, setting orthoMatrix";
            Log.i(TAG, logMessage);
            //Matrix.orthoM(orthographicMatrix, 0, -1, 1, -1 / aspectRatio, 1 / aspectRatio, -1, 1);
            Matrix.orthoM(orthographicMatrix, 0, -1, 1, -shrinkRatio, shrinkRatio, -1, 1);      // shrink height before adapted to screen
        } else {        // height < width,
            shrinkRatio = screenWidth / screenHeight;
            logMessage = "screen height < width, setting orthoMatrix";
            Log.i(TAG, logMessage);
            //Matrix.orthoM(orthographicMatrix, 0, -1, 1, -1 / (aspectRatio - 0.3f), 1 / (aspectRatio - 0.3f), -1, 1);      expand height before adapted to screen
            Matrix.orthoM(orthographicMatrix, 0, -shrinkRatio, shrinkRatio, -1, 1, -1, 1);     // shrink width before adapted to screen
        }

        um = new UnitImageManager(glContext);
        sum = new StaticUnitImageManager(glContext);

        sum.setViewProjectionMatrix(orthographicMatrix);
        um.setViewProjectionMatrix(orthographicMatrix);

        prepareDrawing();
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        sum.drawUnits();
        um.drawUnits();

        // For transparent background and others
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //previousLevelButton.draw();
        //nextLevelButton.draw();

        // draw buttons
        bttnMngr.draw();

        if (allBoxInGoals) {
            /* show some congratulation screen and turn to next level if this level isn't the last level*/
            // do something ( dancing, explosion, ...)
            stageComplete.draw();
            allBoxInGoals = false;

            // go to next level
            gotoNextLevel();
        }

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    private void play(int stageNumber) {
        String logMessage = "play() is called..";
        Log.i(TAG, logMessage);

        prepareMapData(stageNumber);
        setPlayerColumnRow();
        prepareDrawing();    //  --> to onSurfaceChanged()
    }

    // require implementation
    private int getStageNumPlay() { return 0;}

    private void saveState() {
        String logMessage = "saveState() is called..";
        Log.i(TAG, logMessage);

        // save the stage number
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.play_sokoban_bundle), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.stage_number), stageNumPlay + 1);
        editor.commit();

        /* save the game states, in case user comes back */
        //saveGameStates();
    }

    /* called usually when hardware configuration is changed (e.g. screen orientation is changed)*/
    private void restoreGameStates() {
        String logMessage = "restoreState() is called..";
        Log.i(TAG, logMessage);

        /* load the game states if it is not new stage*/
    }

    private Bundle getPSBundle() {
        String logMessage = "getPSBundle() is called..";
        Log.i(TAG, logMessage);

        // load the stage number
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.play_sokoban_bundle), Context.MODE_PRIVATE);
        int stageNumber = sharedPref.getInt(context.getString(R.string.stage_number), 1);

        Bundle bundle = new Bundle();
        bundle.putInt(context.getString(R.string.stage_number), stageNumber);
        bundle.putBoolean(context.getString(R.string.new_stage), false);

        return bundle;
    }

    private void prepareMapData(int mapNumber) {
        String logMessage = "prepareMapData() is called..";
        Log.i(TAG, logMessage);
        // prepare background map
        // prepare unit map
        MapManager.MapData mapData = MapManager.getMapData(mapNumber);
        backgroundMap = mapData.getBackgroundMap();
        unitMap = mapData.getUnitMap();

        mapColumnInUnit = mapData.getColumn();
        mapRowInUnit = mapData.getRow();
        totalGoalCount = mapData.getGoalCount();

        backgroundUnits = new TexturePrimitiveData[mapColumnInUnit * mapRowInUnit];
        units = new TexturePrimitiveData[mapColumnInUnit * mapRowInUnit];
        goalUnitsList = new ArrayList<>();
    }

    private void setPlayerColumnRow() {

        for (int i = 0; i < unitMap.length; i++) {
            if (unitMap[i] == Skbl.WORKER) {
                playerColumn = i % mapColumnInUnit;
                playerRow = i / mapColumnInUnit;

                return;
            }
        }

        playerColumn = -1;
        playerRow = -1;
    }

    private void prepareDrawing() {
        float x = 0;
        float y = 0;

        setMapUnitSize();

        boardStartX = -(( (float) mapColumnInUnit / 2) * mapUnitSize);
        boardStartY = (( (float) mapRowInUnit / 2) * mapUnitSize);

        TexturePrimitiveData unitData = null;
        int column = 0;
        int row = 0;
        int unitID = -1;

        // for background, create background units
        for (int i = 0; i < backgroundMap.length; i++) {
            unitID = backgroundMap[i];
            column = i % mapColumnInUnit;
            row = i / mapColumnInUnit;
            x = boardStartX +column * mapUnitSize;
            y = boardStartY - row * mapUnitSize;

            unitData = um.createUnit(unitID, x, y, mapUnitSize);

            backgroundUnits[i] = unitData;
            sum.addUnitData(unitData);
        }

        // for units, create units which moves (box, worker)
        for (int i = 0; i < unitMap.length; i++) {

            unitID = unitMap[i];
            if ( unitID == Skbl.EMPTYSPACE) {
                units[i] = null;
                continue;
            }

            column = i % mapColumnInUnit;
            row = i / mapColumnInUnit;
            x = boardStartX +column * mapUnitSize;
            y = boardStartY - row * mapUnitSize;

            unitData = um.createUnit(unitID, x, y, mapUnitSize);

            units[i] = unitData;
            um.addUnitData(unitData);
        }

        /* for goal units ( workerInGoal, boxInGoal )
            Every goal has a goal unit and Every goal unit retains two units
             We override the drawings beneath with this unit
        */
        for (int i = 0; i < backgroundMap.length; i++) {
            int destIndex = i;
            unitID = backgroundMap[i];

            if ( unitID == Skbl.WALL || unitID == Skbl.EMPTYSPACE) continue;

            column = i % mapColumnInUnit;
            row = i / mapColumnInUnit;
            x = boardStartX +column * mapUnitSize;
            y = boardStartY - row * mapUnitSize;

            if ( unitID == Skbl.GOALS) {
                MapManager.GoalUnits goalUnits = new MapManager.GoalUnits(
                        i, um.createUnit(Skbl.BOXINGOAL, x, y, mapUnitSize), um.createUnit(Skbl.WORKERINGOAL, x, y, mapUnitSize) );
                goalUnitsList.add(goalUnits);
            }

            // we draw a goal unit on top of a box or a worker unit, so the order of adding units to the um matters
            if ( unitID == Skbl.GOALS) {
                if ( unitMap[i] == Skbl.BOX) {
                    um.addUnitData( getGoalUnits(Skbl.BOX, destIndex));
                }
                else if( unitID == Skbl.WORKER) {
                    um.addUnitData( getGoalUnits(Skbl.WORKER, destIndex));
                }
            }
        }

        sum.prepareDrawing();
    }

    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {       // move worker

        if (allBoxInGoals) return false;

        String logMessage;
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        int direction = -1;

        bttnMngr.onTouchEvent(motionEvent);

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

            logMessage = "onTouchEvent, point of touch " + ", x : " + x + ", y : " + y;
            Log.i(TAG, logMessage);

            if ( autoMode) { moveAuto2(); return true;}
            if (artificialIntelliMode) { artificialIntelliMove(); artificialIntelliMode = false; return true;}

            if ( bttnMngr.rightDirKeyGroup.right.isButtonDown() || bttnMngr.leftDirKeyGroup.right.isButtonDown()) {     // <-- button ( right button )
                // get player position in map data
                //  find what is in player's right position
                //  find whether it is movable
                // if so, move it and move player to the place
                direction = Skbl.RIGHT;

                movePlayer(direction);
            }
            else if (bttnMngr.rightDirKeyGroup.left.isButtonDown() || bttnMngr.leftDirKeyGroup.left.isButtonDown()) {      // left button
                direction = Skbl.LEFT;

                movePlayer(direction);
            }
            else if ( bttnMngr.rightDirKeyGroup.up.isButtonDown() || bttnMngr.leftDirKeyGroup.up.isButtonDown()) {     // up button
                direction = Skbl.UP;

                movePlayer(direction);
            }
            else if ( bttnMngr.rightDirKeyGroup.down.isButtonDown() || bttnMngr.leftDirKeyGroup.down.isButtonDown()) {       // down button
                direction = Skbl.DOWN;

                movePlayer(direction);
            }
            else if ( bttnMngr.setupButton.isButtonDown()) {
                logMessage = "Setup Button Clicked.. ";
                Log.i(TAG, logMessage);

                SkbIntent intent = new SkbIntent(this, SetupScreen.class);
                Bundle bundle = new Bundle();
                bundle.putInt(Skbl.STAGENUMBER, stageNumPlay + 1);      // sends the stage number which is for a player
                intent.setBundle(bundle);
                platform.startActivity(intent);
            }
            else if ( bttnMngr.stepBackButton.isButtonDown()) {
                logMessage = "Stepback Button Clicked.. ";
                Log.i(TAG, logMessage);

                if ( ! stepBack()) trembleShort();
            }
            else if ( bttnMngr.replayButton.isButtonDown()) {
                logMessage = "Replay button is clicked..";
                Log.i(TAG, logMessage);

                int stageNumberForPlayer = this.stageNumPlay + 1;     // 1 : because stage number starts from 1 when we get from StageSelector
                // activate next level
                if (stageNumberForPlayer > 0 && stageNumberForPlayer <= StageSelector.MAXSTAGE) {
                    //startPlay
                    logMessage = "got stage number : " + stageNumberForPlayer;
                    Log.i(TAG, logMessage);

                    SkbIntent intent = new SkbIntent(this, PlaySokoban.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(context.getString(R.string.stage_number), stageNumberForPlayer);
                    bundle.putBoolean(context.getString(R.string.new_stage), true);
                    intent.setBundle(bundle);
                    //this.psBundle = bundle;        // saves a bundle because we restarts this activity
                    platform.finishAndStartActivity(this, intent);
                } else {
                    /* something went wrong...*/
                    logMessage = "Error when replay this stage : " + stageNumberForPlayer;
                    Log.i(TAG, logMessage);
                }
            }

            if ( isAllBoxInGoal2() ) {
                logMessage = "player has completed the puzzle, let's congratulate !!";
                Log.i(TAG, logMessage);
                logMessage = "   **********              Congratulations   !!!!             **********";
                Log.i(TAG, logMessage);

                allBoxInGoals = true;
            }
        }

        // Next Level Button
        //nextLevelButton.onTouchEvent(motionEvent);

        /*
        if (nextLevelButton.isButtonClicked()) {
            logMessage = "nextLevelButton is clicked..";
            Log.i(TAG, logMessage);
            int stageNumberForPlayer = this.stageNumPlay + 2;     // 2 : because stage number starts from 1 when we get from StageSelector
            // activate next level
            if (stageNumberForPlayer > 0 && stageNumberForPlayer <= StageSelector.MAXSTAGE) {
                //startPlay
                logMessage = "got stage number : " + stageNumberForPlayer;
                Log.i(TAG, logMessage);

                SkbIntent intent = new SkbIntent(this, PlaySokoban.class);
                Bundle bundle = new Bundle();
                bundle.putInt(context.getString(R.string.stage_number), stageNumberForPlayer);
                bundle.putBoolean(context.getString(R.string.new_stage), true);
                intent.setBundle(bundle);
                //this.psBundle = bundle;        // saves a bundle because we restarts this activity
                platform.finishAndStartActivity(this, intent);
            } else {
                // tremble or warn in some way to the player
                trembleShort();
            }

            nextLevelButton.setButtonClicked(false);
        }

        // Previous Level Button
        previousLevelButton.onTouchEvent(motionEvent);

        if (previousLevelButton.isButtonClicked()) {
            logMessage = "previousLevelButton is clicked..";
            Log.i(TAG, logMessage);
            int stageNumberForPlayer = this.stageNumPlay;     // 2 : because stage number starts from 1 when we get from StageSelector
            // activate next level
            if (stageNumberForPlayer > 0 && stageNumberForPlayer <= StageSelector.MAXSTAGE) {
                //startPlay
                logMessage = "got stage number for player: " + stageNumberForPlayer;
                Log.i(TAG, logMessage);

                SkbIntent intent = new SkbIntent(this, PlaySokoban.class);
                Bundle bundle = new Bundle();
                bundle.putInt(context.getString(R.string.stage_number), stageNumberForPlayer);
                bundle.putBoolean(context.getString(R.string.new_stage), true);
                intent.setBundle(bundle);
                //this.psBundle = bundle;        // saves a bundle because we restarts this activity
                platform.finishAndStartActivity(this, intent);
            } else {
                // tremble or warn in some way to the player
                trembleShort();
            }

            previousLevelButton.setButtonClicked(false);
        } */

        return true;
    }






    private void movePlayer(int direction) {
        int [] colRowTo;
        int destColumn = -1;
        int destRow = -1;
        boolean movable = false;
        int destIndex = -1;
        int unitID = -1;

        String logMessage = "player column  : " + playerColumn + ", row : " + playerRow;
        Log.i(TAG, logMessage);
        colRowTo = getColumnRowToDirection(playerColumn, playerRow, direction);
        logMessage = "column and row to the right of player, column  : " + colRowTo[0] + ", row : " + colRowTo[1];
        Log.i(TAG, logMessage);
        destColumn = colRowTo[0]; destRow = colRowTo[1];

        movable = isMovable(destColumn, destRow, direction);       // Are you movable??
        logMessage = "movable to " + Skbl.getNameOfDirectionID(direction)+ " is " + movable;
        Log.i(TAG, logMessage);

        if (movable) {
            //if (mapData[ colRowTo[1] * mapSizeInUnit + colRowTo[0] ] == BOX) moveBoxFromTo(colRowTo[0], colRowTo[1], RIGHT);
            //movePlayerFromTo(playerColumn, playerRow, colRowTo[0], colRowTo[1]);
            Transaction transaction = new Transaction();

            destIndex = destRow * mapColumnInUnit + destColumn;
            unitID = unitMap[ destIndex];
            logMessage = "it's movable, unitName : " + Skbl.getNameOfUnitID(unitID);
            Log.i(TAG, logMessage);
            if ( unitID == Skbl.BOX) {
                logMessage = "it's movable, and we have a box to move, moving a box from column " + destColumn + ", row " + destRow;
                Log.i(TAG, logMessage);
                soundPlayer.play(SoundPlayer.BOX_DRAG);
                moveUnitFromTo(Skbl.BOX, destColumn, destRow, direction);
                transaction.getUnitVectorList().add( new UnitVector(destColumn, destRow, direction));       // logging for step back etc
            }

            moveUnitFromTo(Skbl.WORKER, playerColumn, playerRow, direction);
            transaction.getUnitVectorList().add( new UnitVector( playerColumn, playerRow, direction));

            transactionList.add(transaction);
            playerColumn = destColumn; playerRow = destRow;
        } else {
            // Let's tremble...
            logMessage = "**********         tremble !!          **************";
            Log.i(TAG, logMessage);

            trembleShort();
        }
    }

    private boolean stepBack() {
        int lastIndex = transactionList.size() - 1;

        if (lastIndex < 0) return false;

        Transaction lastTr = transactionList.get( lastIndex);

        int [] goBackVector;
        List<UnitVector> unitVectorList = lastTr.getUnitVectorList();
        UnitVector unitVector = null;
        for (int i = unitVectorList.size() -1; i >= 0; i--) {
            unitVector = unitVectorList.get(i);
            goBackVector = goBackVector( unitVector.getColumn(), unitVector.getRow(), unitVector.getDirection());
            moveUnitFromTo( -1, goBackVector[0], goBackVector[1], goBackVector[2]);
        }

        unitVector = unitVectorList.get( unitVectorList.size() - 1);
        //goBackVector = goBackVector(unitVector.getColumn(), unitVector.getRow(), unitVector.getDirection());
        playerColumn = unitVector.getColumn(); playerRow = unitVector.getRow();

        transactionList.remove(lastIndex);

        return true;
    }

    private void moveUnitFromTo(int unitID, int fromColumn, int fromRow, int direction) {
        // set the from column and row to EMPTYSPACE
        // set player column and row to the given values
        // translate vertex buffer
        int fromIndex = fromRow * mapColumnInUnit + fromColumn;
        TexturePrimitiveData unitData = units[fromIndex];
        unitID = unitMap[fromIndex];
        String idName = Skbl.getNameOfUnitID(unitID);

        int [] colRowTo = getColumnRowToDirection(fromColumn, fromRow, direction);
        int destColumn = colRowTo[0];
        int destRow = colRowTo[1];
        int destIndex = destRow * mapColumnInUnit + destColumn;

        String logMessage = "move " + idName + " from Column : " + fromColumn + ", Row : " + fromRow;
        Log.i(TAG, logMessage);

        // **  update unitMap  **
        // current position
        unitMap[fromIndex] = Skbl.EMPTYSPACE;
        logMessage = "updated the unitMap of current position to the default value - empty space, column : " + fromColumn + ", row : " + fromRow;
        Log.i(TAG, logMessage);
        // destination position, the place is one of two places, empty space or goal
        unitMap[destIndex] = unitID;
        // delete drawing BoxInGoal or WorkerInGoal when background is goal
        if ( backgroundMap[fromIndex] == Skbl.GOALS) {
            if ( unitID == Skbl.BOX) {
                um.deleteUnitData( getGoalUnits(Skbl.BOX, fromIndex));
            }
            else if ( unitID == Skbl.WORKER) {
                um.deleteUnitData( getGoalUnits( Skbl.WORKER, fromIndex));
            }
        }

        // move  a unit object
        units[destIndex] = units[fromIndex];
        units[fromIndex] = null;
        translateVertexData(destColumn, destRow, unitData.vertexData);
        // draw BoxInGoal or WorkerInGoal when background is goal
        if ( backgroundMap[destIndex] == Skbl.GOALS) {
            if ( unitID == Skbl.BOX) {
                um.addUnitData( getGoalUnits(Skbl.BOX, destIndex));
            }
            else if( unitID == Skbl.WORKER) {
                um.addUnitData( getGoalUnits(Skbl.WORKER, destIndex));
            }
        }
    }

    // gets unitID of moving unit
    // returns unitID of background unit
    private TexturePrimitiveData getGoalUnits(int unitID, int serial) {

        for(MapManager.GoalUnits goalUnits : goalUnitsList) {
            if ( goalUnits.getSerial() == serial) {
                if (unitID == Skbl.WORKER) {
                    return goalUnits.getWorkerInGoal();
                } else if ( unitID == Skbl.BOX) {
                    return goalUnits.getBoxInGoal();
                }
            }
        }

        return null;
    }

    // ask whether the object in column, row is movable to the direction ( You !, can you move over there?)
    private boolean isMovable(int column, int row, int direction) {
        int currentIndex = row * mapColumnInUnit + column;
        int backgroundUnitID = backgroundMap[currentIndex];
        int unitID = unitMap[currentIndex];

        if (backgroundUnitID == Skbl.WALL) return false;        // backgroundUnitID ( wall, goal, empty space)
        else if (unitID == Skbl.EMPTYSPACE) return true;        // unitID ( worker or box), if there is no wall and no unit, then true

        // of course, it's a box
        if (unitID == Skbl.BOX) {
            int [] colRow = getColumnRowToDirection(column, row, direction);
            int destColumn = colRow[0];
            int destRow = colRow[1];

            // box can move, if the next place at direction is movable
            return isMovableSecond(destColumn, destRow, direction);
        }

        return false;
    }

    // whether the box at column, row position is movable to the direction
    private boolean isMovableSecond(int column, int row, int direction) {
        int currentIndex = row * mapColumnInUnit + column;
        int backgroundUnitID = backgroundMap[currentIndex];
        int unitID = unitMap[currentIndex];

        //   ***   second box, it's movable when empty space or goal is next to it
        if ( ! (backgroundUnitID == Skbl.WALL || unitID == Skbl.BOX ) ) return true;

        return false;       // wall or box
    }

    // Doesn't work
    private boolean isAllBoxInGoal() {
        // goal has a worker or a box, not both of them at a time
        if (goalUnitsList.size() == totalGoalCount ) {      //  size : returns the number of elements in the list
            for (MapManager.GoalUnits goalUnits : goalUnitsList) {
                if ( goalUnits.getWorkerInGoal() != null) return false;
            }
        }

        return true;
    }

    private boolean isAllBoxInGoal2() {
        // read background map, use unit map to match a goal with a box
        for (int i = 0; i < backgroundMap.length; i++) {
            if (backgroundMap[i] == Skbl.GOALS && unitMap[i] != Skbl.BOX) return false;
        }

        return true;
    }

    private void gotoNextLevel() {
        String logMessage;

        int stageNumberForPlayer = this.stageNumPlay + 2;     // 2 : because stage number starts from 1 when we get from StageSelector
        // activate next level
        if (stageNumberForPlayer > 0 && stageNumberForPlayer <= StageSelector.MAXSTAGE) {
            //startPlay
            logMessage = "got stage number : " + stageNumberForPlayer;
            Log.i(TAG, logMessage);

            SkbIntent intent = new SkbIntent(this, PlaySokoban.class);
            Bundle bundle = new Bundle();
            bundle.putInt(Skbl.STAGENUMBER, stageNumberForPlayer);
            bundle.putBoolean(Skbl.NEWSTAGE, true);
            intent.setBundle(bundle);
            platform.finishAndStartActivity(this, intent);
        } else {
                    /* the end of game
                        show the ending screen
                    */
            trembleShort();
        }
    }

    private void setMapUnitSize() {
        /* map unit size has notihing to do with screen size*/

        String logMessage = "mapColumnInUnit : " + mapColumnInUnit + ", mapRowInUnit : " + mapRowInUnit;
        Log.i(TAG, logMessage);

        float margin = 0;
        float widthUnitSize = 2.0f / (mapColumnInUnit + margin );
        float heightUnitSize = 2.0f / (mapRowInUnit + margin );

        logMessage = "widthUnitSize : " + widthUnitSize + ", heightUnitSize : " + heightUnitSize;
        Log.i(TAG, logMessage);

        mapUnitSize = Math.min(widthUnitSize, heightUnitSize);

        // ****    Should be considered more    ****
        if ( screenWidth > screenHeight && mapRowInUnit < mapColumnInUnit)
            mapUnitSize *= ( ( ( (float) screenWidth / screenHeight) * ( (float) mapColumnInUnit / mapRowInUnit)) / 2);
        logMessage = "mapUnitSize : " + mapUnitSize;
        Log.i(TAG, logMessage);
        //if (mapUnitSize > 0.15f) mapUnitSize = 0.15f;
    }

    private void translateVertexData(int column, int row, float [] vertexData) {
        float destX = boardStartX + column * mapUnitSize;
        float destY = boardStartY - row * mapUnitSize;      // cartesian coordinates

        translateVertexData(destX, destY, vertexData);
    }

    private void translateVertexData(float destX, float destY, float [] vertexData) {
        // get x, y of origin
        // calculate the destination x, y
        // calculate the difference between two above
        // translate using matrix

        float originX = vertexData[0];
        float originY = vertexData[1];

        //String logMessage = "originX : " + originX + ", originY : " + originY;
        //Log.i(TAG, logMessage);

        //logMessage = "destX : " + destX + ", destY : " + destY;
        //Log.i(TAG, logMessage);

        float translateX = destX - originX;
        float translateY = destY - originY;


        float [] translateMatrix = new float[16];

        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.translateM(translateMatrix, 0, translateX, translateY, 0);

        float [] currentVertexCoord = new float[4];
        float [] newVertexCoord = new float[4];
        for (int i = 0; i < vertexData.length; i += 3) {
            currentVertexCoord[0] = vertexData[0 + i];
            currentVertexCoord[1] = vertexData[1 + i];
            currentVertexCoord[2] = vertexData[2 + i];
            currentVertexCoord[3] = 1;

            Matrix.multiplyMV(newVertexCoord, 0, translateMatrix, 0, currentVertexCoord, 0);

            vertexData[0 + i] = newVertexCoord[0];
            vertexData[1 + i] = newVertexCoord[1];
            vertexData[2 + i] = newVertexCoord[2];
        }
    }

    private int [] getColumnRowToDirection(int column, int row, int direction) {
        int [] colRow = new int [2];

        int newColumn = column;
        int newRow = row;
        switch (direction) {
            case Skbl.UP : newRow = row - 1; break;
            case Skbl.DOWN : newRow = row + 1; break;
            case Skbl.LEFT : newColumn = column - 1; break;
            case Skbl.RIGHT : newColumn = column + 1; break;
        }

        colRow[0] = newColumn;
        colRow[1] = newRow;

        return colRow;
    }

    private synchronized void moveAuto() {
        try {
            moveUnitFromTo(-1, 3, 2, Skbl.RIGHT);
            wait(500);
            moveUnitFromTo(-1, 2, 2, Skbl.RIGHT);
            wait(500);
            moveUnitFromTo(-1, 3, 2, Skbl.UP);
            wait(500);
            moveUnitFromTo(-1, 3, 1, Skbl.RIGHT);
            wait(500);
            moveUnitFromTo(-1, 4, 1, Skbl.RIGHT);
            wait(500);
            moveUnitFromTo(-1, 5, 1, Skbl.DOWN);
            wait(500);
            moveUnitFromTo(-1, 5, 2, Skbl.DOWN);
            wait(500);
            moveUnitFromTo(-1, 5, 3, Skbl.DOWN);
            wait(500);
            moveUnitFromTo(-1, 5, 4, Skbl.DOWN);
            wait(500);
            moveUnitFromTo(-1, 5, 5, Skbl.LEFT);
            wait(500);
            moveUnitFromTo(-1, 4, 6, Skbl.DOWN);
            wait(500);
            moveUnitFromTo(-1, 4, 5, Skbl.DOWN);
            wait(500);
            moveUnitFromTo(-1, 3, 6, Skbl.LEFT);
            wait(500);
            moveUnitFromTo(-1, 4, 6, Skbl.LEFT);
            wait(500);
            moveUnitFromTo(-1, 3, 6, Skbl.DOWN);
            wait(500);
        } catch (InterruptedException ie) {
            ie.toString();
        }
    }

    private synchronized void moveAuto2() {

        int [] moveLog = {
                3, 2, Skbl.RIGHT,
                2, 2, Skbl.RIGHT,
                3, 2, Skbl.UP,
                3, 1, Skbl.RIGHT,
                4, 1, Skbl.RIGHT,
                5, 1, Skbl.DOWN,
                5, 2, Skbl.DOWN,
                5, 3, Skbl.DOWN,
                5, 4, Skbl.DOWN,
                5, 5, Skbl.LEFT,
                4, 6, Skbl.DOWN,
                4, 5, Skbl.DOWN,
                3, 6, Skbl.LEFT,
                4, 6, Skbl.LEFT,
                3, 6, Skbl.DOWN,
                3, 7, Skbl.LEFT,
                2, 7, Skbl.LEFT,
                1, 6, Skbl.UP,
                1, 7, Skbl.UP,
        };

        try {
            for (int i = 0; i < moveLog.length; ) {
                moveUnitFromTo(-1, moveLog[i++], moveLog[i++], moveLog[i++]);
                wait(400);
            }
        } catch (InterruptedException ie) {
            ie.toString();
        }

        try {
            int [] goBackVector;
            for (int i = moveLog.length - 1; i >= 2; i -= 3) {
                goBackVector = goBackVector(moveLog[i - 2], moveLog[i - 1], moveLog[i]);
                moveUnitFromTo(-1, goBackVector[0], goBackVector[1], goBackVector[2]);
                wait(400);
            }
        } catch (InterruptedException ie) {
            ie.toString();
        }
    }

    private void artificialIntelliMove() {
        // the only box left that is movable, move it
        //if ( theOnlyBoxLeft) move(theOnlyBoxLeft);
    }

    private int [] goBackVector(int column, int row, int direction) {
        int [] goBackVector = new int [3];

        int [] destColRow = getColumnRowToDirection(column, row, direction);

        goBackVector[0] = destColRow[0];
        goBackVector[1] = destColRow[1];
        goBackVector[2] = traverseDirection(direction);

        return goBackVector;
    }

    private int traverseDirection(int direction) {
        switch (direction) {
            case Skbl.UP :
                return Skbl.DOWN;
            case Skbl.DOWN :
                return Skbl.UP;
            case Skbl.LEFT :
                return Skbl.RIGHT;
            case Skbl.RIGHT :
                return Skbl.LEFT;
            default :
                return -1;
        }
    }

    private void trembleShort() {
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) glContext.getContext().getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate for 400 milliseconds
        //v.vibrate(400);
        long vib = 50; long noVib = 50;
        long [] vibratePattern = { 0, vib, noVib, vib, noVib, vib, noVib, vib, noVib};
        v.vibrate(vibratePattern, -1);
    }

    private int initTextureUnitForNextLevelButton() {
        int [] maxTextureNumber = new int[1];

        int textureUnitCount = 1;
        int textureUnitConst = GLES20.GL_TEXTURE1;
        int textureUnitNumber = textureUnitConst - GLES20.GL_TEXTURE0;

        int imageID = R.drawable.next_level;

        //int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int textureUnitConstTobeUsed = textureUnitConst;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageID);

        GLES20.glActiveTexture(textureUnitConstTobeUsed);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);    // public static void texImage2D (int target, int level, Bitmap bitmap, int border)
        if (GLES20.glGetError() != 0) {
            Log.i("glGetError", "textImage2D()");
        }

        bitmap.recycle();

        return textureUnitNumber;
    }

    private int initTextureUnitForPreviousLevelButton() {
        int [] maxTextureNumber = new int[1];

        int textureUnitCount = 1;
        int textureUnitConst = GLES20.GL_TEXTURE2;
        int textureUnitNumber = textureUnitConst - GLES20.GL_TEXTURE0;

        int imageID = R.drawable.previous_level;

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int textureUnitConstTobeUsed = textureUnitConst;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageID);

        GLES20.glActiveTexture(textureUnitConstTobeUsed);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);    // public static void texImage2D (int target, int level, Bitmap bitmap, int border)
        if (GLES20.glGetError() != 0) {
            Log.i(TAG, "glGetError : " + "textImage2D()");
        }

        bitmap.recycle();

        return textureUnitNumber;
    }


    private int initTextureUnitForDirectionKeyButton() {
        int [] maxTextureNumber = new int[1];

        int textureUnitCount = 1;
        int textureUnitConst = GLES20.GL_TEXTURE3;
        int textureUnitNumber = textureUnitConst - GLES20.GL_TEXTURE0;

        int imageID = R.drawable.arrow_button_prac03;

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int textureUnitConstTobeUsed = textureUnitConst;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageID);

        GLES20.glActiveTexture(textureUnitConstTobeUsed);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);    // public static void texImage2D (int target, int level, Bitmap bitmap, int border)
        if (GLES20.glGetError() != 0) {
            Log.i(TAG, "glGetError : " + "textImage2D()");
        }

        bitmap.recycle();

        return textureUnitNumber;
    }

    private int initTextureUnitForStepBackButton() {
        int [] maxTextureNumber = new int[1];

        int textureUnitCount = 1;
        int textureUnitConst = GLES20.GL_TEXTURE4;
        int textureUnitNumber = textureUnitConst - GLES20.GL_TEXTURE0;

        int imageID = R.drawable.step_back02;

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int textureUnitConstTobeUsed = textureUnitConst;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageID);

        GLES20.glActiveTexture(textureUnitConstTobeUsed);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);    // public static void texImage2D (int target, int level, Bitmap bitmap, int border)
        if (GLES20.glGetError() != 0) {
            Log.i(TAG, "glGetError : " + "textImage2D()");
        }

        bitmap.recycle();

        return textureUnitNumber;
    }

    private int initTextureUnitForReplayButton() {
        int [] maxTextureNumber = new int[1];

        int textureUnitCount = 1;
        int textureUnitConst = GLES20.GL_TEXTURE5;
        int textureUnitNumber = textureUnitConst - GLES20.GL_TEXTURE0;

        int imageID = R.drawable.replay01;

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int textureUnitConstTobeUsed = textureUnitConst;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageID);

        GLES20.glActiveTexture(textureUnitConstTobeUsed);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);    // public static void texImage2D (int target, int level, Bitmap bitmap, int border)
        if (GLES20.glGetError() != 0) {
            Log.i(TAG, "glGetError : " + "textImage2D()");
        }

        bitmap.recycle();

        return textureUnitNumber;
    }

    private int initTextureUnitForSetup() {
        int [] maxTextureNumber = new int[1];

        int textureUnitCount = 1;
        int textureUnitConst = GLES20.GL_TEXTURE6;
        int textureUnitNumber = textureUnitConst - GLES20.GL_TEXTURE0;

        int imageID = R.drawable.setup02;

        //int textureUnitNumber = textureUnitConstTobeUsed - GLES20.GL_TEXTURE0;

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[textureUnitCount];

        // first parameter : Specifies the number (how many) of texture names to be generated
        // second parameter : pecifies an array in which the generated texture names are stored
        // third : offset
        GLES20.glGenTextures(textureUnitCount, texturenames, 0);

        int textureUnitConstTobeUsed = textureUnitConst;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageID);

        GLES20.glActiveTexture(textureUnitConstTobeUsed);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);    // public static void texImage2D (int target, int level, Bitmap bitmap, int border)
        if (GLES20.glGetError() != 0) {
            Log.i("glGetError", "textImage2D()");
        }

        bitmap.recycle();

        return textureUnitNumber;
    }


    final class StageComplete {
        private TextManager tm;
        private static final long secInNano = 1000000000l;
        private static final long msgRepresentTime = 5l * secInNano;

        private long startTime;
        //private long elapsedTime;

        public StageComplete() {
            tm = new TextManager(glContext);
            tm.addTextData(tm.createString("Congratulations !!", 0, 0, 0.2f));
        }

        public void draw() {

            if (System.nanoTime() - startTime < msgRepresentTime)
                tm.drawTexts();
        }

    }

    final class ButtonManager {
        private Sounder sounder;

        private ImageButtoner11 setupButton;
        //      direction key groups
        private RightDirectionKeyGroup rightDirKeyGroup;
        private LeftDirectionKeyGroup leftDirKeyGroup;

        private ImageButtoner11 stepBackButton;
        private ImageButtoner11 replayButton;

        private final float setuplXpos = 0.0f;
        private final float setuplYpos = 0.80f;
        private final float setupWidth = 0.25f;
        private final float setupHeight = 0.25f;

        private static final long directionbttnPresentTime = 20000000000l;
        private static final long directionbttnPresentTimeInSec = directionbttnPresentTime / 1000000000l;
        private static final float directionbttnColorAlpha = 0.35f;        // At start, we draw direction buttons at the color alpha of 0.5f
        private static final float colorAlpha = 0.25f;

        public ButtonManager() {
            sounder = new Sounder(soundPlayer.getSoundPool(), soundPlayer.getButtonClickID());

            int textureUnitNumber = initTextureUnitForSetup();
            setupButton = new ImageButtoner11(glContext, setuplXpos, setuplYpos, setupWidth, setupHeight, textureUnitNumber);
            setupButton.setColorAlpha(colorAlpha);
            setupButton.attachClickSounder(sounder);

            textureUnitNumber = initTextureUnitForDirectionKeyButton();
            rightDirKeyGroup = new RightDirectionKeyGroup(textureUnitNumber, directionbttnColorAlpha);
            leftDirKeyGroup = new LeftDirectionKeyGroup(textureUnitNumber, directionbttnColorAlpha);

            textureUnitNumber = initTextureUnitForStepBackButton();
            stepBackButton = new ImageButtoner11(glContext, -0.2f, -0.80f, 0.3f, 0.3f, textureUnitNumber);
            stepBackButton.setColorAlpha(colorAlpha);

            textureUnitNumber = initTextureUnitForReplayButton();
            replayButton = new ImageButtoner11(glContext, 0.2f, -0.80f, 0.3f, 0.3f, textureUnitNumber);
            replayButton.setColorAlpha(colorAlpha);
        }

        public void draw() {
            setupButton.draw();
            rightDirKeyGroup.draw();
            leftDirKeyGroup.draw();
            stepBackButton.draw();
            replayButton.draw();
        }

        public void onTouchEvent(MotionEvent motionEvent) {
            setupButton.onTouchEvent(motionEvent);
            rightDirKeyGroup.onTouchEvent(motionEvent);
            leftDirKeyGroup.onTouchEvent(motionEvent);
            stepBackButton.onTouchEvent(motionEvent);
            replayButton.onTouchEvent(motionEvent);

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if (stepBackButton.isButtonDown()) soundPlayer.play(SoundPlayer.BUTTON_CLICK);
                if (replayButton.isButtonDown()) soundPlayer.play(SoundPlayer.BUTTON_CLICK);
            }
        }
    }

    final class RightDirectionKeyGroup {

        private ImageButtoner11 right;
        private ImageButtoner11 up;
        private ImageButtoner11 left;
        private ImageButtoner11 down;

        private ColorAlphaGradientor rightColorAlphaGradientor;
        private ColorAlphaGradientor upColorAlphaGradientor;
        private ColorAlphaGradientor leftColorAlphaGradientor;
        private ColorAlphaGradientor downColorAlphaGradientor;

        private Sounder sounder;

        private final float onClick_startColorAlpha = ButtonManager.directionbttnColorAlpha * 1.0f;
        private final float onClick_endColorAlpha = ButtonManager.directionbttnColorAlpha * 0.1f;
        private final float onClick_colorAlphaDurationInSec = 0.5f;

        public RightDirectionKeyGroup(int textureUnitNumber, float buttonColorAlpha) {
            float alpha = buttonColorAlpha;
            float width = 0.2f;
            float height = 0.8f;
            float upDownWidth = width + 0.2f;
            float upDownHeight = height + 0.2f;
            float gap = 0.01f;
            right = new ImageButtoner11(glContext, 1 - (width / 2), 0.0f, width, height, textureUnitNumber);
            //right = new ImageButtoner11(glContext, 0.0f, 0.0f, 1.0f, 1.0f, textureUnitNumber);
            //right.setColorAlpha(alpha);
            up = new ImageButtoner11(glContext, 1 - (width / 2), width + gap + upDownWidth / 2, upDownWidth, upDownHeight, textureUnitNumber);
            //up.setColorAlpha(alpha);
            up.rotate(90);
            left = new ImageButtoner11(glContext, 1 - (width + width / 2), 0.0f, width, height, textureUnitNumber);
            //left.setColorAlpha(alpha);
            left.rotate(180);
            down = new ImageButtoner11(glContext, 1 - (width / 2), -(width + gap + upDownWidth / 2), upDownWidth, upDownHeight, textureUnitNumber);
            //down.setColorAlpha(alpha);
            down.rotate(270);

            rightColorAlphaGradientor = new ColorAlphaGradientor();
            rightColorAlphaGradientor.setDrawInfo(alpha, 0.0f, ButtonManager.directionbttnPresentTimeInSec);
            upColorAlphaGradientor = new ColorAlphaGradientor();
            upColorAlphaGradientor.setDrawInfo(alpha, 0.0f, ButtonManager.directionbttnPresentTimeInSec);
            leftColorAlphaGradientor = new ColorAlphaGradientor();
            leftColorAlphaGradientor.setDrawInfo(alpha, 0.0f, ButtonManager.directionbttnPresentTimeInSec);
            downColorAlphaGradientor = new ColorAlphaGradientor();
            downColorAlphaGradientor.setDrawInfo(alpha, 0.0f, ButtonManager.directionbttnPresentTimeInSec);

            right.attachColorAlphaGradientor(rightColorAlphaGradientor);
            up.attachColorAlphaGradientor(upColorAlphaGradientor);
            left.attachColorAlphaGradientor(leftColorAlphaGradientor);
            down.attachColorAlphaGradientor(downColorAlphaGradientor);

            sounder = new Sounder(soundPlayer.getSoundPool(), soundPlayer.getButtonClickID());
            right.attachClickSounder(sounder);
            up.attachClickSounder(sounder);
            left.attachClickSounder(sounder);
            down.attachClickSounder(sounder);
        }

        /*
        public void setAlpha(float alpha) {
            right.setColorAlpha(alpha);
            up.setColorAlpha(alpha);
            left.setColorAlpha(alpha);
            down.setColorAlpha(alpha);
        }*/

        /*
        public void setDrawInfo(float startColorAlpha, float endColorAlpha, float durationInSecond) {

            rightGradientor.setDrawInfo(startColorAlpha, endColorAlpha, durationInSecond);
            upGradientor.setDrawInfo(startColorAlpha, endColorAlpha, durationInSecond);
            leftGradientor.setDrawInfo(startColorAlpha, endColorAlpha, durationInSecond);
            downGradientor.setDrawInfo(startColorAlpha, endColorAlpha, durationInSecond);

        } */

        public void draw() {

            right.draw();
            up.draw();
            left.draw();
            down.draw();

            /*
            rightGradientor.draw();
            upGradientor.draw();
            leftGradientor.draw();
            downGradientor.draw();
            */
        }

        /*
        public void setAlphaAndDraw(float alpha) {
            setAlpha(alpha);
            draw();
        }*/

        public void onTouchEvent(MotionEvent motionEvent) {
            String logMessage;
            right.onTouchEvent(motionEvent);
            up.onTouchEvent(motionEvent);
            left.onTouchEvent(motionEvent);
            down.onTouchEvent(motionEvent);

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                if (right.isButtonDown() || up.isButtonDown() || left.isButtonDown() || down.isButtonDown()) {

                    if (right.isButtonDown()) rightColorAlphaGradientor.setDrawInfoOnClick(
                            onClick_startColorAlpha, onClick_endColorAlpha, onClick_colorAlphaDurationInSec);
                    if (up.isButtonDown()) upColorAlphaGradientor.setDrawInfoOnClick(
                            onClick_startColorAlpha, onClick_endColorAlpha, onClick_colorAlphaDurationInSec);
                    if (left.isButtonDown()) leftColorAlphaGradientor.setDrawInfoOnClick(
                            onClick_startColorAlpha, onClick_endColorAlpha, onClick_colorAlphaDurationInSec);
                    if (down.isButtonDown()) downColorAlphaGradientor.setDrawInfoOnClick(
                            onClick_startColorAlpha, onClick_endColorAlpha, onClick_colorAlphaDurationInSec);
                }
            }

        }
    }

    final class LeftDirectionKeyGroup {

        private ImageButtoner11 right;
        private ImageButtoner11 up;
        private ImageButtoner11 left;
        private ImageButtoner11 down;

        private ColorAlphaGradientor rightColorAlphaGradientor;
        private ColorAlphaGradientor upColorAlphaGradientor;
        private ColorAlphaGradientor leftColorAlphaGradientor;
        private ColorAlphaGradientor downColorAlphaGradientor;

        private Sounder sounder;

        private final float onClick_startColorAlpha = ButtonManager.directionbttnColorAlpha * 1.0f;
        private final float onClick_endColorAlpha = ButtonManager.directionbttnColorAlpha * 0.1f;
        private final float onClick_colorAlphaDurationInSec = 0.5f;

        public LeftDirectionKeyGroup(int textureUnitNumber, float buttonColorAlpha) {
            float alpha = buttonColorAlpha;
            float width = 0.2f;
            float height = 0.8f;
            float upDownWidth = width + 0.2f;
            float upDownHeight = height + 0.2f;
            float gap = 0.01f;
            right = new ImageButtoner11(glContext,  -1 + (width + width / 2), 0.0f, width, height, textureUnitNumber);
            //right = new ImageButtoner11(glContext, 0.0f, 0.0f, 1.0f, 1.0f, textureUnitNumber);
            right.setColorAlpha(alpha);
            up = new ImageButtoner11(glContext, -1 + (width / 2), width + gap + upDownWidth / 2, upDownWidth, upDownHeight, textureUnitNumber);
            up.setColorAlpha(alpha);
            up.rotate(90);
            left = new ImageButtoner11(glContext,  -1 + (width / 2), 0.0f, width, height, textureUnitNumber);
            left.setColorAlpha(alpha);
            left.rotate(180);
            down = new ImageButtoner11(glContext, -1 + (width / 2), -(width + gap + upDownWidth / 2), upDownWidth, upDownHeight, textureUnitNumber);
            down.setColorAlpha(alpha);
            down.rotate(270);

            rightColorAlphaGradientor = new ColorAlphaGradientor();
            rightColorAlphaGradientor.setDrawInfo(alpha, 0.0f, ButtonManager.directionbttnPresentTimeInSec);
            upColorAlphaGradientor = new ColorAlphaGradientor();
            upColorAlphaGradientor.setDrawInfo(alpha, 0.0f, ButtonManager.directionbttnPresentTimeInSec);
            leftColorAlphaGradientor = new ColorAlphaGradientor();
            leftColorAlphaGradientor.setDrawInfo(alpha, 0.0f, ButtonManager.directionbttnPresentTimeInSec);
            downColorAlphaGradientor = new ColorAlphaGradientor();
            downColorAlphaGradientor.setDrawInfo(alpha, 0.0f, ButtonManager.directionbttnPresentTimeInSec);

            right.attachColorAlphaGradientor(rightColorAlphaGradientor);
            up.attachColorAlphaGradientor(upColorAlphaGradientor);
            left.attachColorAlphaGradientor(leftColorAlphaGradientor);
            down.attachColorAlphaGradientor(downColorAlphaGradientor);

            sounder = new Sounder(soundPlayer.getSoundPool(), soundPlayer.getButtonClickID());
            right.attachClickSounder(sounder);
            up.attachClickSounder(sounder);
            left.attachClickSounder(sounder);
            down.attachClickSounder(sounder);
        }

        public void setAlpha(float alpha) {
            right.setColorAlpha(alpha);
            up.setColorAlpha(alpha);
            left.setColorAlpha(alpha);
            down.setColorAlpha(alpha);
        }

        public void draw() {
            right.draw();
            up.draw();
            left.draw();
            down.draw();
        }

        public void setAlphaAndDraw(float alpha) {
            setAlpha(alpha);
            draw();
        }

        public void onTouchEvent(MotionEvent motionEvent) {
            String logMessage;
            right.onTouchEvent(motionEvent);
            up.onTouchEvent(motionEvent);
            left.onTouchEvent(motionEvent);
            down.onTouchEvent(motionEvent);

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                if (right.isButtonDown() || up.isButtonDown() || left.isButtonDown() || down.isButtonDown()) {

                    if (right.isButtonDown()) rightColorAlphaGradientor.setDrawInfoOnClick(
                            onClick_startColorAlpha, onClick_endColorAlpha, onClick_colorAlphaDurationInSec);
                    if (up.isButtonDown()) upColorAlphaGradientor.setDrawInfoOnClick(
                            onClick_startColorAlpha, onClick_endColorAlpha, onClick_colorAlphaDurationInSec);
                    if (left.isButtonDown()) leftColorAlphaGradientor.setDrawInfoOnClick(
                            onClick_startColorAlpha, onClick_endColorAlpha, onClick_colorAlphaDurationInSec);
                    if (down.isButtonDown()) downColorAlphaGradientor.setDrawInfoOnClick(
                            onClick_startColorAlpha, onClick_endColorAlpha, onClick_colorAlphaDurationInSec);
                }
            }

        } // end of onTouchEvent

    }
}