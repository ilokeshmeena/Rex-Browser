package com.rexvit.browser.utils.animUtils;

import android.view.animation.Interpolator;

import androidx.core.view.animation.PathInterpolatorCompat;


public final class BezierEaseInterpolator implements Interpolator {
    private static final Interpolator sBezierInterpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1.0f);

    @Override 
    public float getInterpolation(float f) {
        return sBezierInterpolator.getInterpolation(f);
    }
}
