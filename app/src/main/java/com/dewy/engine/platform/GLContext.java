package com.dewy.engine.platform;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by dewyone on 2015-10-06.
 */
public abstract class GLContext {
    // ailas : glContext
    // Gives the basic information on the environment of rendering, plus on the states of activity
    // such as screen width, height
    //
    public static final int BYTES_PER_FLOAT = 4;
    public static final int BYTES_PER_SHORT = 2;

    private Activity activityContext;
    private Context context;
    protected EGLConfig eglConfig;

    protected float screenWidth;
    protected float screenHeight;

    protected final List<GLSurfaceView.Renderer> rendererList = new ArrayList<>();
    protected final List<View.OnTouchListener> onTouchListenerList = new ArrayList<>();
    protected final List<ActivityLifeCycle> activityLifeCycleList = new ArrayList<>();

    public void setActivityContext(Activity activityContext) {
        this.activityContext = activityContext;
        setContext(activityContext);
    }
    public Activity getActivityContext() {
        return activityContext;
    }
    private void setContext(Context context) {
        this.context = context;
    }
    public Context getContext() {
        return context;
    }

    public EGLConfig getEglConfig() {
        return eglConfig;
    }

    public void setEglConfig(EGLConfig eglConfig) {
        this.eglConfig = eglConfig;
    }

    public void setScreenWidthHeight(float screenWidth, float screenHeight) {
        setScreenWidth(screenWidth);
        setScreenHeight(screenHeight);
    }

    public float getScreenWidth() {
        return screenWidth;
    }
    public void setScreenWidth(float screenWidth) {
        this.screenWidth = screenWidth;
    }

    public float getScreenHeight() {
        return screenHeight;
    }
    public void setScreenHeight(float screenHeight) {
        this.screenHeight = screenHeight;
    }

    public void addRenderer(GLSurfaceView.Renderer renderer) {
        rendererList.add(renderer);
    }

    public void addOnTouchListener(View.OnTouchListener onTouchListener) {
        onTouchListenerList.add(onTouchListener);
    }

    public void addActivityLifeCycleListener(ActivityLifeCycle activityLifeCycle) {
        activityLifeCycleList.add(activityLifeCycle);
    }
}