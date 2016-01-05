package com.dewy.engine.animation.activities;

import com.dewy.engine.platform.GLContext;
import com.dewy.engine.animation.activities.view.WelcomeRenderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dewyone on 2015-08-31.
 */
public class Welcome extends GLActivity {

    private final GLActivity platform;
    private final GLContext GLContext;

    private WelcomeRenderer welcomeRenderer;

    private float screenWidth;
    private float screenHeight;

    public Welcome(GLActivity MainActivity) {
        this.platform = MainActivity;
        this.GLContext = platform.glContext;

        welcomeRenderer = new WelcomeRenderer(GLContext);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}