package com.rexvit.browser.view.customView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import androidx.core.internal.view.SupportMenu;

import com.rexvit.browser.R;
import com.rexvit.browser.utils.animUtils.BezierEaseInterpolator;
import java.util.ArrayDeque;
import java.util.Queue;


public class AnimatedProgressBar extends View {
    private static final long ALPHA_DURATION = 200;
    private static final long PROGRESS_DURATION = 500;
    private final Interpolator mAlphaInterpolator;
    private final Queue<Animation> mAnimationQueue;
    private boolean mBidirectionalAnimate;
    private int mDrawWidth;
    private final Paint mPaint;
    private int mProgress;
    private int mProgressColor;
    private final Interpolator mProgressInterpolator;
    private final Rect mRect;

    public AnimatedProgressBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mProgress = 0;
        this.mBidirectionalAnimate = true;
        this.mDrawWidth = 0;
        this.mAlphaInterpolator = new LinearInterpolator();
        this.mProgressInterpolator = new BezierEaseInterpolator();
        this.mAnimationQueue = new ArrayDeque<>();
        this.mPaint = new Paint();
        this.mRect = new Rect();
        init(context, attributeSet);
    }

    public AnimatedProgressBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mProgress = 0;
        this.mBidirectionalAnimate = true;
        this.mDrawWidth = 0;
        this.mAlphaInterpolator = new LinearInterpolator();
        this.mProgressInterpolator = new BezierEaseInterpolator();
        this.mAnimationQueue = new ArrayDeque<>();
        this.mPaint = new Paint();
        this.mRect = new Rect();
        init(context, attributeSet);
    }

    private void init(Context context, AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.AnimatedProgressBar, 0, 0);
        try {
            this.mProgressColor = obtainStyledAttributes.getColor(1, SupportMenu.CATEGORY_MASK);
            this.mBidirectionalAnimate = obtainStyledAttributes.getBoolean(0, false);
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    public int getProgress() {
        return this.mProgress;
    }

    @Override 
    protected void onDraw(Canvas canvas) {
        this.mPaint.setColor(this.mProgressColor);
        this.mPaint.setStrokeWidth(10.0f);
        Rect rect = this.mRect;
        rect.right = rect.left + this.mDrawWidth;
        canvas.drawRect(this.mRect, this.mPaint);
    }

    public void setProgress(int i) {
        if (i > 100) {
            i = 100;
        } else if (i < 0) {
            i = 0;
        }
        if (getAlpha() < 1.0f) {
            fadeIn();
        }
        int measuredWidth = getMeasuredWidth();
        Rect rect = this.mRect;
        rect.left = 0;
        rect.top = 0;
        rect.bottom = getBottom() - getTop();
        if (i < this.mProgress && !this.mBidirectionalAnimate) {
            this.mDrawWidth = 0;
        } else if (i == this.mProgress && i == 100) {
            fadeOut();
        }
        this.mProgress = i;
        int i2 = this.mDrawWidth;
        int i3 = ((this.mProgress * measuredWidth) / 100) - i2;
        if (i3 != 0) {
            animateView(i2, i3, measuredWidth);
        }
    }

    private void animateView(int i, int i2, int i3) {
        ProgressAnimation progressAnimation = new ProgressAnimation(i, i2, i3);
        progressAnimation.setDuration(PROGRESS_DURATION);
        progressAnimation.setInterpolator(this.mProgressInterpolator);
        if (!this.mAnimationQueue.isEmpty()) {
            this.mAnimationQueue.add(progressAnimation);
        } else {
            startAnimation(progressAnimation);
        }
    }

    private void fadeIn() {
        animate().alpha(1.0f).setDuration(ALPHA_DURATION).setInterpolator(this.mAlphaInterpolator).start();
    }

    
    public void fadeOut() {
        animate().alpha(0.0f).setDuration(ALPHA_DURATION).setInterpolator(this.mAlphaInterpolator).start();
    }

    @Override 
    protected void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable instanceof Bundle) {
            Bundle bundle = (Bundle) parcelable;
            this.mProgress = bundle.getInt("progressState");
            parcelable = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(parcelable);
    }

    @Override 
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putInt("progressState", this.mProgress);
        return bundle;
    }

    
    
    public class ProgressAnimation extends Animation {
        private int mDeltaWidth;
        private int mInitialWidth;
        private int mMaxWidth;

        @Override 
        public boolean willChangeBounds() {
            return false;
        }

        @Override 
        public boolean willChangeTransformationMatrix() {
            return false;
        }

        ProgressAnimation(int i, int i2, int i3) {
            this.mInitialWidth = i;
            this.mDeltaWidth = i2;
            this.mMaxWidth = i3;
        }

        @Override 
        protected void applyTransformation(float f, Transformation transformation) {
            int i = this.mInitialWidth + ((int) (this.mDeltaWidth * f));
            if (i <= this.mMaxWidth) {
                AnimatedProgressBar.this.mDrawWidth = i;
                AnimatedProgressBar.this.invalidate();
            }
            if (Math.abs(1.0f - f) < 1.0E-5d) {
                if (AnimatedProgressBar.this.mProgress >= 100) {
                    AnimatedProgressBar.this.fadeOut();
                }
                if (AnimatedProgressBar.this.mAnimationQueue.isEmpty()) {
                    return;
                }
                AnimatedProgressBar animatedProgressBar = AnimatedProgressBar.this;
                animatedProgressBar.startAnimation((Animation) animatedProgressBar.mAnimationQueue.poll());
            }
        }
    }
}
