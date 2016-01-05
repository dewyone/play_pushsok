package com.dewy.engine.animation.activities.lib;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.activities.view.TextureRectRenderer;
import com.dewy.engine.primitives.ObjectBuilder;
import com.dewy.engine.primitives.PrimitiveData;
import com.dewy.engine.util.Geometry;

import java.math.BigDecimal;

/**
 * Created by dewyone on 2015-10-03.
 */
public class ImageSpinner {
    private static final String TAG = "ImageSpinner";

    public static final int POSITION_COMPONENT_COUNT = 3;
    public final int UV_COMPONENT_COUNT = 2;
    private final GLContext glContext;
    private final Context context;

    private final SpinnerUnit spinnerUnitA;
    private final SpinnerUnit spinnerUnitB;

    private static final int SpinDown = 0;
    private static final int SpinUp = 1;
    private static final int SpinStop = -1;
    private int spinDirection = SpinStop;

    // rectangle to place in screen
    public static float rectWidth = 0.5f;
    public static float rectHeight = 0.5f;
    private final float centerX;
    private final float centerY;
    private float leftX;
    private float rightX;
    private float topY;
    private float bottomY;

    // default values
    private int drawNumberForUnknown = -1;
    private float delta = 0;        // ( delta >= 0, it represents the smallest movement of rectangle)
    private final static float SpeedUnit = 0.01f;
    float uvDelta;

    private static final float fontWidth = 0.1f;

    private boolean onTouchEventActivated = true;
    private boolean nextActivated = false;

    public ImageSpinner(GLContext glContext, float x, float y, int textureUnitNumber) {
        this(glContext, x, y, rectWidth, rectHeight, textureUnitNumber);
    }

    public ImageSpinner(GLContext glContext, float x, float y, float width, float height, int textureUnitNumber) {

        this.glContext = glContext;
        context = glContext.getContext();
        centerX = x;
        centerY = y;
        rectWidth = width;
        rectHeight = height;
        leftX = centerX - (rectWidth / 2);
        rightX = centerX + (rectWidth / 2);
        topY = centerY + (rectHeight / 2);
        bottomY = centerY - (rectHeight / 2);

        // get Vertex data and the way to draw itself
        PrimitiveData rectanglePrimiA = ObjectBuilder.createRectangle(new Geometry.Point(centerX, centerY, 0),
                new Geometry.Vector(0, 0, 1), rectWidth, rectHeight);
        PrimitiveData rectanglePrimiB = ObjectBuilder.createRectangle(new Geometry.Point(centerX, centerY, 0),
                new Geometry.Vector(0, 0, 1), rectWidth, rectHeight);

        // get UVs
        float [] uvDataA = getBaseUvData();
        float [] uvDataB = getBaseUvData();

        //** Using two TextureRectRenderers, we make one spinner
        TextureRectRenderer textureRectRendererA = new TextureRectRenderer(glContext, textureUnitNumber);
        TextureRectRenderer textureRectRendererB = new TextureRectRenderer(glContext, textureUnitNumber);

        spinnerUnitA = new SpinnerUnit(textureRectRendererA, "Even Number");
        spinnerUnitB = new SpinnerUnit(textureRectRendererB, "Odd Number");

        spinnerUnitA.setData(rectanglePrimiA, uvDataA);
        spinnerUnitB.setData(rectanglePrimiB, uvDataB);

        spinnerUnitA.representingNumber = 0;
        spinnerUnitA.rectToRedefine = false;
        spinnerUnitB.rectToRedefine = true;

        float one = 0.26f;
        float two = 0.01f;
        float result = one - two;

        String logMessage = "0.26f - 0.01f = " + result;
        Log.i(TAG, logMessage);

        BigDecimal bigDecimal = new BigDecimal(result);
        bigDecimal = bigDecimal.setScale(5, BigDecimal.ROUND_HALF_EVEN);
        logMessage = "bigDecimal.setScale(5, BigDecimal.ROUND_HALF_EVEN) of the result : " + bigDecimal.floatValue();
        Log.i(TAG, logMessage);
    }

    public void next() {
        // 1. verify that onTouchEvent is invalidated..
        // 2. spin until representing letter shows ( the info needed,
    }

    public void draw() {

        if (onTouchEventActivated) {
            spins();
        }
        else if ( nextActivated) {
            //next();
        }

        // draw
        spinnerUnitA.draw();
        spinnerUnitB.draw();
    }

    private void spins() {
        String logMessage;
        if (spinDirection == SpinStop) {delta = 0; return;}

        if (spinDirection != SpinStop) {
            logMessage = "Start Spin !";
            Log.i(TAG, logMessage);

            delta = SpeedUnit;

            /* deal with two special cases */
            if (existNumberToRedefine()) {      // when one unit is fully grown ( the height is full)
                logMessage = "Unknown Number exists, let's redefine !";
                Log.i(TAG, logMessage);
                redefineRect(spinDirection);
                spinUnit(spinDirection);
            }
            else if (isHeightLessThanDelta(spinDirection))      // when one unit's height is less than the delta of spinning
                spinUnitAndSetNextNumber(spinDirection);
            /* And a normal case */
            else spinUnit(spinDirection);
        }
    }



    private void redefineRect(int spinDirection) {
        if ( spinDirection == SpinDown) redefineRectForSpinDown();
        else if ( spinDirection == SpinUp) redefineRectForSpinUp();
    }

    private void redefineRectForSpinDown() {
        String logMessage;

        SpinnerUnit spinnerUnitToRedefine = null;
        int numberToRedefine = 0;

        /* get the spinner unit to redefine */
        if (spinnerUnitA.rectToRedefine && spinnerUnitB.rectToRedefine) ;//throw new Exception();
        else if (spinnerUnitA.rectToRedefine) spinnerUnitToRedefine = spinnerUnitA;
        else if (spinnerUnitB.rectToRedefine) spinnerUnitToRedefine = spinnerUnitB;
        else return;

        /* get the number that follows*/
        if (spinnerUnitA.rectToRedefine) {
            numberToRedefine = (spinnerUnitB.representingNumber + 1) % 10;
        }
        else if (spinnerUnitB.rectToRedefine) {
            numberToRedefine = (spinnerUnitA.representingNumber + 1) % 10;
        }

        spinnerUnitToRedefine.representingNumber = numberToRedefine;
        spinnerUnitToRedefine.rectToRedefine = false;
        logMessage = "the spinner redefined : " + spinnerUnitToRedefine.toString();
        Log.i(TAG, logMessage);
        logMessage = "the number redefined : " + spinnerUnitToRedefine.representingNumber;
        Log.i(TAG, logMessage);


        /* re-initiate the last number's vertex data and uv data */
        prepareToExpand(spinnerUnitToRedefine, numberToRedefine, SpinDown);
    }

    private void redefineRectForSpinUp() {
        String logMessage;

        SpinnerUnit spinnerUnitToRedefine = null;
        int numberToRedefine = 0;

        /* get the spinner unit to redefine */
        if (spinnerUnitA.rectToRedefine && spinnerUnitB.rectToRedefine) ;//throw new Exception();
        else if (spinnerUnitA.rectToRedefine) spinnerUnitToRedefine = spinnerUnitA;
        else if (spinnerUnitB.rectToRedefine) spinnerUnitToRedefine = spinnerUnitB;
        else return;

        /* get the number that follows*/
        if (spinnerUnitA.rectToRedefine) {
            numberToRedefine = spinnerUnitB.representingNumber - 1;
        }
        else if (spinnerUnitB.rectToRedefine) {
            numberToRedefine = spinnerUnitA.representingNumber - 1;
        }
        if (numberToRedefine == -1) numberToRedefine = 10 + numberToRedefine;

        spinnerUnitToRedefine.representingNumber = numberToRedefine;
        spinnerUnitToRedefine.rectToRedefine = false;
        logMessage = "the number redefined : " + spinnerUnitToRedefine.representingNumber;
        Log.i(TAG, logMessage);

        /* re-initiate the last number's vertex data and uv data */
        prepareToExpand(spinnerUnitToRedefine, numberToRedefine, SpinUp);
    }

    private void prepareToExpand(SpinnerUnit spinnerUnitToRedefine, int numberToRedefine, int spinDirection) {
        String logMessage = "prepareToExpand, spinnerUnitToRedefine : " + spinnerUnitToRedefine.toString();
        Log.i(TAG, logMessage);
        PrimitiveData rectanglePrimi = spinnerUnitToRedefine.getRectanglePrimi();
        float [] uvData = spinnerUnitToRedefine.getUvData();
        float y_vertexData;
        float y_uvData;

        if (spinDirection == SpinDown) {
            logMessage = "spinDirection is SpinDown";
            Log.i(TAG, logMessage);

            y_vertexData = topY;
            y_uvData = 1;
        } else if (spinDirection == SpinUp) {
            logMessage = "spinDirection is SpinUp";
            Log.i(TAG, logMessage);

            y_vertexData = bottomY;
            y_uvData = 0;
        } else return;      // should throw Error

        // move the unit to the top or to the bottom, the height is zero
        for (int i = 0; i < 4; i++) {
            rectanglePrimi.vertexData[1 + i * POSITION_COMPONENT_COUNT] = y_vertexData;
        }

        // prepare to make the unit image expand
        for (int i = 0; i < 4; i++) {
            uvData[1 + i * UV_COMPONENT_COUNT] = y_uvData;
        }

        // move to the uv number, adjusts x coords in uv
        moveUvImageTo(spinnerUnitToRedefine.getUvData(), numberToRedefine);
    }



    private void spinUnit(int spinDirection) {
        if (spinDirection == SpinDown) {
            spinDown(getLowerSpinnerUnit(), getUpperSpinnerUnit());
        } else if (spinDirection == SpinUp) {
            spinUp(getLowerSpinnerUnit(), getUpperSpinnerUnit());
        }
    }

    private void spinDown(SpinnerUnit lowerSpinnerUnit, SpinnerUnit upperSpinnerUnit) {
        String logMessage;

        // when there is a unit of zero height
        if (lowerSpinnerUnit == upperSpinnerUnit) {
            logMessage = "lower and upper unit is same, spinDown(), lower unit is " + lowerSpinnerUnit.toString() +
                    ", upper unit is " + upperSpinnerUnit.toString();
            Log.i(TAG, logMessage);
            if (lowerSpinnerUnit == spinnerUnitA) upperSpinnerUnit = spinnerUnitB;
            else if (lowerSpinnerUnit == spinnerUnitB) upperSpinnerUnit = spinnerUnitA;
        }

        logMessage = "spinDown(), lower unit is " + lowerSpinnerUnit.toString() +
                ", upper unit is " + upperSpinnerUnit.toString();
        Log.i(TAG, logMessage);
        logMessage = "And lower unit number : " + lowerSpinnerUnit.representingNumber + ", upper unit number : " +
                upperSpinnerUnit.representingNumber;
        Log.i(TAG, logMessage);
        logMessage = "And lower unit vertexData[4] : " + lowerSpinnerUnit.getRectanglePrimi().vertexData[4] +
                ", height : " + lowerSpinnerUnit.getHeight();
        Log.i(TAG, logMessage);
        logMessage = "And upper unit vertexData[4] : " + upperSpinnerUnit.getRectanglePrimi().vertexData[4] +
                ", height : " + upperSpinnerUnit.getHeight();
        Log.i(TAG, logMessage);
        logMessage = "And upper unit vertexData[1] : " + upperSpinnerUnit.getRectanglePrimi().vertexData[1];
        Log.i(TAG, logMessage);

        /** shrink lower rectangle */
        shrinkAndMoveUnitBy(lowerSpinnerUnit, delta, SpinDown);

        //** expand upper rectangle
        expandAndMoveUnitBy(upperSpinnerUnit, delta, SpinDown);
    }

    private void spinUp(SpinnerUnit lowerSpinnerUnit, SpinnerUnit upperSpinnerUnit) {
        String logMessage;

        if (lowerSpinnerUnit == upperSpinnerUnit) {
            logMessage = "lower and upper unit is same, spinDown(), lower unit is " + lowerSpinnerUnit.toString() +
                    ", upper unit is " + upperSpinnerUnit.toString();
            Log.i(TAG, logMessage);
            if (upperSpinnerUnit == spinnerUnitA) lowerSpinnerUnit = spinnerUnitB;
            else if (upperSpinnerUnit == spinnerUnitB) lowerSpinnerUnit = spinnerUnitA;
        }

        logMessage = "spinUp(), lower unit is " + lowerSpinnerUnit.toString() +
                ", upper unit is " + upperSpinnerUnit.toString();
        Log.i(TAG, logMessage);
        logMessage = "And lower unit number : " + lowerSpinnerUnit.representingNumber + ", upper unit number : " +
                upperSpinnerUnit.representingNumber;
        Log.i(TAG, logMessage);
        logMessage = "And lower unit vertexData[4] : " + lowerSpinnerUnit.getRectanglePrimi().vertexData[4] +
                ", height : " + lowerSpinnerUnit.getHeight();
        Log.i(TAG, logMessage);
        logMessage = "And upper unit vertexData[4] : " + upperSpinnerUnit.getRectanglePrimi().vertexData[4] +
                ", height : " + upperSpinnerUnit.getHeight();
        Log.i(TAG, logMessage);
        logMessage = "And lower unit vertexData[4] : " + lowerSpinnerUnit.getRectanglePrimi().vertexData[4];
        Log.i(TAG, logMessage);

        // shrink upper rectangle
        shrinkAndMoveUnitBy(upperSpinnerUnit, delta, SpinUp);

        // expand lower rectangle, append from bottom
        expandAndMoveUnitBy(lowerSpinnerUnit, delta, SpinUp);
    }

    private void shrinkAndMoveUnitBy(SpinnerUnit unitToShrink, float shrinkDelta, int spinDirection) {
        String logMessage;
        float [] vertexData = unitToShrink.getRectanglePrimi().vertexData;
        float [] uvData = unitToShrink.getUvData();
        float shrinkUvDelta = ( (1 / rectHeight) * -shrinkDelta);       // negative of normalized coords ( uv coords : 0 to 1, normalized coords : 1 to -1)

        logMessage = "shrinkAndMoveUnitBy(), shrinkDelta : " + shrinkDelta + ", unitToShrink : " + unitToShrink.toString();
        Log.i(TAG, logMessage);
        if (spinDirection == SpinDown) {
            /* Lower Unit */

            //** shrink lower rectangle at top
            vertexData[1] = new BigDecimal(vertexData[1] - shrinkDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();
            vertexData[10] = new BigDecimal(vertexData[10] - shrinkDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();

            // crop the uv number image from bottom
            uvData[3] = new BigDecimal(uvData[3] + shrinkUvDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();
            uvData[5] = new BigDecimal(uvData[5] + shrinkUvDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();
        }
        else if (spinDirection == SpinUp) {
            /* Upper Unit */

            // shrink upper rectangle at bottom
            vertexData[4] = new BigDecimal(vertexData[4] + shrinkDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();
            vertexData[7] = new BigDecimal(vertexData[7] + shrinkDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();

            // shrink the uv number image from top
            uvData[1] = new BigDecimal(uvData[1] - shrinkUvDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();
            uvData[7] = new BigDecimal(uvData[7] - shrinkUvDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();
        }

        /* When Unit Height is Zero, the unit's number and upper-lower becomes unknown */
        if (unitToShrink.getHeight() == 0) {
            logMessage = "the unit " + unitToShrink.toString() + "'s height is Zero.";
            Log.i(TAG, logMessage);

            //**  For determining and redefining next number
            unitToShrink.representingNumber = drawNumberForUnknown;
            unitToShrink.rectToRedefine = true;
        }

        logMessage = "And vertexData[1] : " + vertexData[1] + ", vertexData[4] : " + vertexData[4];
        Log.i(TAG, logMessage);
        logMessage = "And uvData[1] : " + uvData[1] + ", uvData[3] : " + uvData[3];
        Log.i(TAG, logMessage);
    }

    private void expandAndMoveUnitBy(SpinnerUnit unitToExpand, float expandDelta, int spinDirection) {
        String logMessage;
        float [] vertexData = unitToExpand.getRectanglePrimi().vertexData;
        float [] uvData = unitToExpand.getUvData();
        float expandUvDelta = ( (1 / rectHeight) * -expandDelta);       // negative of normalized coords ( uv coords : 0 to 1, normalized coords : 1 to -1)

        logMessage = "expandAndMoveUnitBy(), expandDelta : " + expandDelta + ", unitToExpand : " + unitToExpand;
        Log.i(TAG, logMessage);
        if (spinDirection == SpinDown) {
            /* Upper Unit */

            /* expand upper rectangle at bottom */
            vertexData[4] = new BigDecimal(vertexData[4] - expandDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();
            vertexData[7] = new BigDecimal(vertexData[7] - expandDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();

            // expand the uv number image from top
            uvData[1] = new BigDecimal(uvData[1] + expandUvDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();
            uvData[7] = new BigDecimal(uvData[7] + expandUvDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();
        }
        else if (spinDirection == SpinUp) {
            /* Lower Unit */

            // expand lower rectangle at top, append from bottom
            vertexData[1] = new BigDecimal(vertexData[1] + expandDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();
            vertexData[10] = new BigDecimal(vertexData[10] + expandDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();

            // expand the uv number image from bottom
            uvData[3] = new BigDecimal(uvData[3] - expandUvDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();
            uvData[5] = new BigDecimal(uvData[5] - expandUvDelta).setScale(5, BigDecimal.ROUND_HALF_EVEN).floatValue();
        }

        logMessage = "And vertexData[1] : " + vertexData[1] + ", vertexData[4]" + vertexData[4];
        Log.i(TAG, logMessage);
        logMessage = "And uvData[1] : " + uvData[1] + ", uvData[3] : " + uvData[3];
        Log.i(TAG, logMessage);
    }




    private void spinUnitAndSetNextNumber(int spinDirection) {
        if (spinDirection == SpinDown) spinUnitAndSetNextNumberForSpinDown(getLowerSpinnerUnit(), getUpperSpinnerUnit());
        else if (spinDirection == SpinUp) spinUnitAndSetNextNumberForSpinUp(getLowerSpinnerUnit(), getUpperSpinnerUnit());
    }

    private boolean isHeightLessThanDelta(int spinDirection) {
        float lowerHeight = getLowerSpinnerUnit().getHeight();
        float upperHeight = getUpperSpinnerUnit().getHeight();
        //String logMessage = "lowerHeight : " + lowerHeight + ", upperHeight : " + upperHeight;
        //Log.i(TAG, logMessage);
        if (spinDirection == SpinDown && lowerHeight !=0 && lowerHeight < (Math.abs(delta)) ||
                spinDirection == SpinUp && upperHeight != 0 && upperHeight < (Math.abs(delta))) return true;
        else return false;
    }

    private void spinUnitAndSetNextNumberForSpinDown(SpinnerUnit lowerSpinnerUnit, SpinnerUnit upperSpinnerUnit) {
        String logMessage;

        float lowerUnitHeight = lowerSpinnerUnit.getHeight();

        if (lowerUnitHeight < (Math.abs(delta))) {  // the upper rectangle is full
            logMessage = "spinUnitAndSetN...(), lowerUnitHeight " + lowerUnitHeight + " is less than Math.abs(delta)  "
                    + Math.abs(delta) + ",   let's shrink upper unit by the shrinkDelta, upper unit : " + upperSpinnerUnit.toString();
            Log.i(TAG, logMessage);

            /* **  clean up the deviation */

            // get the rest
            float restDelta = Math.abs(delta) - lowerUnitHeight;

            // expand upper unit by the lowerUnitHeight
            expandAndMoveUnitBy(upperSpinnerUnit, lowerUnitHeight, SpinDown);
            // shrink upper unit by the rest
            shrinkAndMoveUnitBy(upperSpinnerUnit, restDelta, SpinDown);

            // determine following unit
            SpinnerUnit followingUnit = determineFollowingUnit(upperSpinnerUnit, SpinDown);

            // expand following unit by the rest
            expandAndMoveUnitBy(followingUnit, restDelta, SpinDown);



            logMessage = "And lower unit is " + lowerSpinnerUnit.toString() +
                    ", upper unit is " + upperSpinnerUnit.toString();
            Log.i(TAG, logMessage);
            //logMessage = "And lower unit number : " + lowerSpinnerUnit.representingNumber + ", upper unit number : " +
            //        upperSpinnerUnit.representingNumber;
            //Log.i(TAG, logMessage);
            logMessage = "And lower unit vertexData[4] : " + lowerSpinnerUnit.getRectanglePrimi().vertexData[4] +
                    ", height : " + lowerSpinnerUnit.getHeight();
            Log.i(TAG, logMessage);
            logMessage = "And upper unit vertexData[4] : " + upperSpinnerUnit.getRectanglePrimi().vertexData[4] +
                    ", height : " + upperSpinnerUnit.getHeight();
            Log.i(TAG, logMessage);
        }
    }

    private void spinUnitAndSetNextNumberForSpinUp(SpinnerUnit lowerSpinnerUnit, SpinnerUnit upperSpinnerUnit) {
        String logMessage;

        float upperUnitHeight = upperSpinnerUnit.getHeight();

        if (upperUnitHeight < (Math.abs(delta))) {  // the lower rectangle is full
            logMessage = "upperUnitHeight " + upperUnitHeight + " is less than Math.abs(delta)  " + Math.abs(delta)
                    + Math.abs(delta) + ",   let's shrink lower unit by the shrinkDelta, lower unit : " + lowerSpinnerUnit.toString();
            Log.i(TAG, logMessage);

            /* ** clean up the deviation */

            // get the rest
            float restDelta = Math.abs(delta) - upperUnitHeight;

            // expand lower unit by the upperUnitHeight
            expandAndMoveUnitBy(lowerSpinnerUnit, upperUnitHeight, SpinUp);
            // shrink lower unit by the shrinkDelta
            shrinkAndMoveUnitBy(lowerSpinnerUnit, restDelta, SpinUp);

            // determine following unit
            SpinnerUnit followingUnit = determineFollowingUnit(lowerSpinnerUnit, SpinUp);

            // expand following unit by the rest
            expandAndMoveUnitBy(followingUnit, restDelta, SpinUp);
        }
    }

    private SpinnerUnit determineFollowingUnit(SpinnerUnit ofThisUnit, int spinDirection) {
        String logMessage;
        SpinnerUnit spinnerUnitToRedefine = null;
        int numberToRedefine = 0;

        // determine the following unit
        if (ofThisUnit == spinnerUnitA) spinnerUnitToRedefine = spinnerUnitB;
        else if (ofThisUnit == spinnerUnitB) spinnerUnitToRedefine = spinnerUnitA;

        if (spinDirection == SpinDown) {
            /* get the number that follows*/
            numberToRedefine = (ofThisUnit.representingNumber + 1) % 10;

            spinnerUnitToRedefine.representingNumber = numberToRedefine;
            spinnerUnitToRedefine.rectToRedefine = false;
            //logMessage = "the number redefined : " + spinnerUnitToRedefine.representingNumber;
            //Log.i(TAG, logMessage);

            /* re-initiate the last number's vertex data and uv data */

            PrimitiveData rectanglePrimi = spinnerUnitToRedefine.getRectanglePrimi();
            // move the shrinking rect to the top, the height is same
            rectanglePrimi.vertexData[1] = topY;
            rectanglePrimi.vertexData[10] = topY;
            rectanglePrimi.vertexData[4] = topY;
            rectanglePrimi.vertexData[7] = topY;

            // prepare to make the image expand bottom-up
            float [] uvData = spinnerUnitToRedefine.getUvData();
            uvData[1] = 1;
            uvData[7] = 1;
            uvData[3] = 1;
            uvData[5] = 1;
        }
        else if (spinDirection == SpinUp) {
            numberToRedefine = ofThisUnit.representingNumber - 1;
            if (numberToRedefine == -1) numberToRedefine = 10 + numberToRedefine;

            spinnerUnitToRedefine.representingNumber = numberToRedefine;
            spinnerUnitToRedefine.rectToRedefine = false;
            //logMessage = "the number redefined : " + spinnerUnitToRedefine.representingNumber;
            //Log.i(TAG, logMessage);

            /* re-initiate the last number's vertex data and uv data */

            PrimitiveData rectanglePrimi = spinnerUnitToRedefine.getRectanglePrimi();
            // move the shrinking rect to the top, the height is same
            rectanglePrimi.vertexData[1] = bottomY;
            rectanglePrimi.vertexData[10] = bottomY;
            rectanglePrimi.vertexData[4] = bottomY;
            rectanglePrimi.vertexData[7] = bottomY;

            // prepare to make the image expand bottom-up
            float [] uvData = spinnerUnitToRedefine.getUvData();
            uvData[1] = 0;
            uvData[7] = 0;
            uvData[3] = 0;
            uvData[5] = 0;
        }

        // move to the uv number
        moveUvImageTo(spinnerUnitToRedefine.getUvData(), numberToRedefine);

        return spinnerUnitToRedefine;
    }



    /* when one of two spinners is undefined, this method returns the other defined spinner */
    private SpinnerUnit getLowerSpinnerUnit() {
        if (spinnerUnitA.getHeight() == rectHeight) return spinnerUnitA;
        else if (spinnerUnitB.getHeight() == rectHeight) return spinnerUnitB;
        else return (spinnerUnitA.getRectanglePrimi().vertexData[4] < spinnerUnitB.getRectanglePrimi().vertexData[4])
                    ? spinnerUnitA : spinnerUnitB;
    }

    /* when one of two spinners is undefined, this method returns the other defined spinner */
    private SpinnerUnit getUpperSpinnerUnit() {
        if (spinnerUnitA.getHeight() == rectHeight) return spinnerUnitA;
        else if (spinnerUnitB.getHeight() == rectHeight) return spinnerUnitB;
        else return (spinnerUnitA.getRectanglePrimi().vertexData[4] > spinnerUnitB.getRectanglePrimi().vertexData[4])
                    ? spinnerUnitA : spinnerUnitB;
    }

    public int getRepresentingNumber() {
        return getRepresentingNumber(getLowerSpinnerUnit(), getUpperSpinnerUnit());
    }

    private int getRepresentingNumber(SpinnerUnit lowerSpinnerUnit, SpinnerUnit upperSpinnerUnit) {
        return (upperSpinnerUnit.getHeight() > lowerSpinnerUnit.getHeight())
                ? upperSpinnerUnit.representingNumber : lowerSpinnerUnit.representingNumber;
    }


    private void moveUvImageTo(float [] uvData, int number) {
        float [] data = getBaseUvData();
        uvData[0] = data[0] + (fontWidth * number);
        uvData[2] = data[2] + (fontWidth * number);
        uvData[4] = data[4] + (fontWidth * number);
        uvData[6] = data[6] + (fontWidth * number);
    }

    private boolean existNumberToRedefine() {
        return (spinnerUnitA.rectToRedefine || spinnerUnitB.rectToRedefine);
    }

    public void onTouchEvent(MotionEvent motionEvent) {
        onTouchEvent01(motionEvent);
    }

    private void onTouchEvent01(MotionEvent motionEvent) {
        String logMessage;
        float screenX = motionEvent.getX();
        float screenY = motionEvent.getY();
        float x = (screenX / glContext.getScreenWidth()) * 2 - 1;        // x, y in normalized coords
        float y = -((screenY / glContext.getScreenHeight()) * 2) + 1;;

        //logMessage = "Position of Action in normalized coordinates is x : " + x + ",   y : " + y;
        //Log.i(TAG, logMessage);

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE :
                //logMessage = "MotionEvent.ACTION_MOVE is generated..";
                //Log.i(TAG, logMessage);

                // get the distance of move
                // get the speed of move

                // calculate the distance to move rect
                // calculate the speed to move rect

                // move rect

                break;
            case  MotionEvent.ACTION_DOWN :

                //logMessage = "MotionEvent.ACTION_DOWN is generated..";
                //Log.i(TAG, logMessage);

                if ( x > leftX && x < rightX && y < (bottomY + (rectHeight / 2)) && y > (bottomY - (rectHeight / 2))) {
                    //logMessage = "ACTION_DOWN is clicked... , initiate SpinDown";
                    //Log.i(TAG, logMessage);
                    spinDirection = SpinDown;
                }
                else if ( x > leftX && x < rightX && y > (topY - (rectHeight / 2)) && y < (topY + (rectHeight / 2)) ) {
                    //logMessage = "ACTION_DOWN is clicked... , initiate SpinUp";
                    //Log.i(TAG, logMessage);
                    spinDirection = SpinUp;
                }

                break;
            case MotionEvent.ACTION_UP :
                //logMessage = "MotionEvent.ACTION_DOWN is generated..";
                //Log.i(TAG, logMessage);

                spinDirection = SpinStop;
                break;
        }
    }

    private static float[] getBaseUvData() {
        float startX = 0;
        float[] uvData = {
                startX, 0f,
                startX, 1f,
                startX + fontWidth, 1f,
                startX + fontWidth, 0f
        };

        return uvData;
    }
}

class SpinnerUnit {
    private final String Label;
    private PrimitiveData rectanglePrimi;
    private float [] uvData;

    private final TextureRectRenderer textureRectRenderer;

    public int representingNumber;

    public boolean rectToRedefine = true;

    public SpinnerUnit(TextureRectRenderer textureRectRenderer, String label) {
        this.textureRectRenderer = textureRectRenderer;
        this.Label = label;
    }

    public void setData(PrimitiveData primitiveData, float[] uvData) {
        this.rectanglePrimi = primitiveData;
        this.uvData = uvData;
    }

    public void setData(PrimitiveData primitiveData) {
        this.rectanglePrimi = primitiveData;
    }

    public void setData(float [] uvData) {
        this.uvData = uvData;
    }

    @Override
    public String toString() {
        return Label;
    }

    public PrimitiveData getRectanglePrimi() {
        return rectanglePrimi;
    }

    public float[] getUvData() {
        return uvData;
    }

    public float getHeight() {
        return Math.abs(rectanglePrimi.vertexData[1] - rectanglePrimi.vertexData[4]);
    }

    public void draw() {
        textureRectRenderer.setRectDrawingInfo(rectanglePrimi, uvData);
        textureRectRenderer.draw();
    }
}
