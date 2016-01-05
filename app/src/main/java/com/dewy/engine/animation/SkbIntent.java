package com.dewy.engine.animation;

import android.os.Bundle;

import com.dewy.engine.animation.activities.GLActivity;

/**
 * Created by dewyone on 2015-08-31.
 */
public class SkbIntent {
    private GLActivity fromWhom;
    private Class toWhom;

    private Bundle bundle;

    public SkbIntent(GLActivity fromWhom, Class toWhom) {
        this.fromWhom = fromWhom;
        this.toWhom = toWhom;
    }

    public GLActivity getFromWhom() {
        return fromWhom;
    }

    public Class getToWhom() {
        return toWhom;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}
