package com.rexvit.browser.view.customView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.core.view.ViewCompat;

import java.io.IOException;


public class FullscreenVideoView extends RelativeLayout implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener {
    private static final String TAG = "FullscreenVideoView";
    protected Activity activity;
    protected MediaPlayer.OnCompletionListener completionListener;
    protected Context context;
    protected ViewGroup.LayoutParams currentLayoutParams;
    protected State currentState;
    protected boolean detachedByFullscreen;
    protected MediaPlayer.OnErrorListener errorListener;
    protected boolean fullscreen;
    protected MediaPlayer.OnInfoListener infoListener;
    protected int initialConfigOrientation;
    protected int initialMovieHeight;
    protected int initialMovieWidth;
    protected State lastState;
    protected MediaPlayer mediaPlayer;
    protected View onProgressView;
    protected ViewGroup parentView;
    protected MediaPlayer.OnPreparedListener preparedListener;
    protected MediaPlayer.OnSeekCompleteListener seekCompleteListener;
    protected boolean shouldAutoplay;
    protected SurfaceHolder surfaceHolder;
    protected boolean surfaceIsReady;
    protected SurfaceView surfaceView;
    protected boolean videoIsReady;

    
    public enum State {
        IDLE,
        INITIALIZED,
        PREPARED,
        PREPARING,
        STARTED,
        STOPPED,
        PAUSED,
        PLAYBACKCOMPLETED,
        ERROR,
        END
    }

    public FullscreenVideoView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public FullscreenVideoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        init();
    }

    public FullscreenVideoView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.context = context;
        init();
    }

    @Override 
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        resize();
    }

    @Override 
    public Parcelable onSaveInstanceState() {
        Log.d(TAG, "onSaveInstanceState");
        return super.onSaveInstanceState();
    }

    @Override 
    public void onRestoreInstanceState(Parcelable parcelable) {
        Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(parcelable);
    }

    @Override 
    protected void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow - detachedByFullscreen: " + this.detachedByFullscreen);
        super.onDetachedFromWindow();
        if (!this.detachedByFullscreen) {
            MediaPlayer mediaPlayer = this.mediaPlayer;
            if (mediaPlayer != null) {
                mediaPlayer.setOnPreparedListener(null);
                this.mediaPlayer.setOnErrorListener(null);
                this.mediaPlayer.setOnSeekCompleteListener(null);
                this.mediaPlayer.setOnCompletionListener(null);
                this.mediaPlayer.setOnInfoListener(null);
                if (this.mediaPlayer.isPlaying()) {
                    this.mediaPlayer.stop();
                }
                this.mediaPlayer.release();
                this.mediaPlayer = null;
            }
            this.videoIsReady = false;
            this.surfaceIsReady = false;
            this.currentState = State.END;
        }
        this.detachedByFullscreen = false;
    }

    @Override 
    public synchronized void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated called = " + this.currentState);
        this.mediaPlayer.setDisplay(this.surfaceHolder);
        if (!this.surfaceIsReady) {
            this.surfaceIsReady = true;
            if (this.currentState != State.PREPARED && this.currentState != State.PAUSED && this.currentState != State.STARTED && this.currentState != State.PLAYBACKCOMPLETED) {
                tryToPrepare();
            }
        }
    }

    @Override 
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        Log.d(TAG, "surfaceChanged called");
        resize();
    }

    @Override 
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed called");
        MediaPlayer mediaPlayer = this.mediaPlayer;
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            this.mediaPlayer.pause();
        }
        this.surfaceIsReady = false;
    }

    @Override 
    public synchronized void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared called");
        this.videoIsReady = true;
        tryToPrepare();
    }

    @Override 
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onSeekComplete");
        stopLoading();
        if (this.lastState != null) {
            int i = inState.in[this.lastState.ordinal()];
            if (i == 1) {
                start();
            } else if (i == 2) {
                this.currentState = State.PLAYBACKCOMPLETED;
            } else if (i == 3) {
                this.currentState = State.PREPARED;
            }
        }
        MediaPlayer.OnSeekCompleteListener onSeekCompleteListener = this.seekCompleteListener;
        if (onSeekCompleteListener != null) {
            onSeekCompleteListener.onSeekComplete(mediaPlayer);
        }
    }

    
    
    static  class inState {
        static final  int[] in = new int[State.values().length];

        static {
            try {
                in[State.STARTED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                in[State.PLAYBACKCOMPLETED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                in[State.PREPARED.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    @Override 
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (this.mediaPlayer != null && this.currentState != State.ERROR) {
            Log.d(TAG, "onCompletion");
            if (!this.mediaPlayer.isLooping()) {
                this.currentState = State.PLAYBACKCOMPLETED;
            } else {
                start();
            }
        }
        MediaPlayer.OnCompletionListener onCompletionListener = this.completionListener;
        if (onCompletionListener != null) {
            onCompletionListener.onCompletion(mediaPlayer);
        }
    }

    @Override 
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i2) {
        Log.d(TAG, "onInfo " + i);
        MediaPlayer.OnInfoListener onInfoListener = this.infoListener;
        if (onInfoListener != null) {
            return onInfoListener.onInfo(mediaPlayer, i, i2);
        }
        return false;
    }

    @Override 
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        Log.d(TAG, "onError called - " + i + " - " + i2);
        stopLoading();
        this.currentState = State.ERROR;
        MediaPlayer.OnErrorListener onErrorListener = this.errorListener;
        if (onErrorListener != null) {
            return onErrorListener.onError(mediaPlayer, i, i2);
        }
        return false;
    }

    @Override 
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i2) {
        Log.d(TAG, "onVideoSizeChanged = " + i + " - " + i2);
        if (this.initialMovieWidth == 0 && this.initialMovieHeight == 0) {
            this.initialMovieWidth = i;
            this.initialMovieHeight = i2;
            resize();
        }
    }

    protected void init() {
        if (isInEditMode()) {
            return;
        }
        this.shouldAutoplay = false;
        this.currentState = State.IDLE;
        this.fullscreen = false;
        this.initialConfigOrientation = -1;
        setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        initObjects();
    }

    protected void initObjects() {
        this.mediaPlayer = new MediaPlayer();
        this.surfaceView = new SurfaceView(this.context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.addRule(13);
        this.surfaceView.setLayoutParams(layoutParams);
        addView(this.surfaceView);
        this.surfaceHolder = this.surfaceView.getHolder();
        this.surfaceHolder.setType(3);
        this.surfaceHolder.addCallback(this);
        if (this.onProgressView == null) {
            this.onProgressView = new ProgressBar(this.context);
        }
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams2.addRule(13);
        this.onProgressView.setLayoutParams(layoutParams2);
        addView(this.onProgressView);
    }

    protected void releaseObjects() {
        SurfaceHolder surfaceHolder = this.surfaceHolder;
        if (surfaceHolder != null) {
            surfaceHolder.removeCallback(this);
            this.surfaceHolder = null;
        }
        MediaPlayer mediaPlayer = this.mediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            this.mediaPlayer = null;
        }
        SurfaceView surfaceView = this.surfaceView;
        if (surfaceView != null) {
            removeView(surfaceView);
        }
        View view = this.onProgressView;
        if (view != null) {
            removeView(view);
        }
    }

    protected void prepare() throws IllegalStateException {
        startLoading();
        this.videoIsReady = false;
        this.initialMovieHeight = -1;
        this.initialMovieWidth = -1;
        this.mediaPlayer.setOnPreparedListener(this);
        this.mediaPlayer.setOnErrorListener(this);
        this.mediaPlayer.setOnSeekCompleteListener(this);
        this.mediaPlayer.setOnInfoListener(this);
        this.mediaPlayer.setOnVideoSizeChangedListener(this);
        this.mediaPlayer.setAudioStreamType(3);
        this.currentState = State.PREPARING;
        this.mediaPlayer.prepareAsync();
    }

    protected void tryToPrepare() {
        if (this.surfaceIsReady && this.videoIsReady) {
            MediaPlayer mediaPlayer = this.mediaPlayer;
            if (mediaPlayer != null) {
                this.initialMovieWidth = mediaPlayer.getVideoWidth();
                this.initialMovieHeight = this.mediaPlayer.getVideoHeight();
            }
            resize();
            stopLoading();
            this.currentState = State.PREPARED;
            if (this.shouldAutoplay) {
                start();
            }
            MediaPlayer.OnPreparedListener onPreparedListener = this.preparedListener;
            if (onPreparedListener != null) {
                onPreparedListener.onPrepared(this.mediaPlayer);
            }
        }
    }

    protected void startLoading() {
        View view = this.onProgressView;
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    protected void stopLoading() {
        View view = this.onProgressView;
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    public synchronized State getCurrentState() {
        return this.currentState;
    }

    public boolean isFullscreen() {
        return this.fullscreen;
    }

    public void setFullscreen(boolean z) throws RuntimeException {
        if (this.mediaPlayer == null) {
            throw new RuntimeException("Media Player is not initialized");
        }
        if (this.currentState == State.ERROR || this.fullscreen == z) {
            return;
        }
        this.fullscreen = z;
        final boolean isPlaying = this.mediaPlayer.isPlaying();
        if (isPlaying) {
            pause();
        }
        boolean z2 = true;
        if (this.fullscreen) {
            Activity activity = this.activity;
            if (activity != null) {
                activity.setRequestedOrientation(-1);
            }
            @SuppressLint("ResourceType") View findViewById = getRootView().findViewById(16908290);
            ViewParent parent = getParent();
            if (parent instanceof ViewGroup) {
                if (this.parentView == null) {
                    this.parentView = (ViewGroup) parent;
                }
                this.detachedByFullscreen = true;
                this.currentLayoutParams = getLayoutParams();
                this.parentView.removeView(this);
            } else {
                Log.e(TAG, "Parent View is not a ViewGroup");
            }
            if (findViewById instanceof ViewGroup) {
                ((ViewGroup) findViewById).addView(this);
            } else {
                Log.e(TAG, "RootView is not a ViewGroup");
            }
        } else {
            Activity activity2 = this.activity;
            if (activity2 != null) {
                activity2.setRequestedOrientation(this.initialConfigOrientation);
            }
            ViewParent parent2 = getParent();
            if (parent2 instanceof ViewGroup) {
                ViewGroup viewGroup = this.parentView;
                if (viewGroup == null || viewGroup.getParent() == null) {
                    z2 = false;
                } else {
                    this.detachedByFullscreen = true;
                }
                ((ViewGroup) parent2).removeView(this);
                if (z2) {
                    this.parentView.addView(this);
                    setLayoutParams(this.currentLayoutParams);
                }
            }
        }
        resize();
        new Handler(Looper.getMainLooper()).post(new Runnable() { 
            @Override 
            public void run() {
                if (!isPlaying || FullscreenVideoView.this.mediaPlayer == null) {
                    return;
                }
                FullscreenVideoView.this.start();
            }
        });
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        this.initialConfigOrientation = activity.getRequestedOrientation();
    }

    public void resize() {
        if (this.initialMovieHeight == -1 || this.initialMovieWidth == -1 || this.surfaceView == null) {
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() { 
            @Override 
            public void run() {
                int i;
                int i2;
                View view = (View) FullscreenVideoView.this.getParent();
                if (view != null) {
                    float f = FullscreenVideoView.this.initialMovieWidth / FullscreenVideoView.this.initialMovieHeight;
                    int width = view.getWidth();
                    int height = view.getHeight();
                    float f2 = width;
                    float f3 = height;
                    if (f > f2 / f3) {
                        i2 = (int) (f2 / f);
                        i = width;
                    } else {
                        i = (int) (f * f3);
                        i2 = height;
                    }
                    ViewGroup.LayoutParams layoutParams = FullscreenVideoView.this.surfaceView.getLayoutParams();
                    layoutParams.width = i;
                    layoutParams.height = i2;
                    FullscreenVideoView.this.surfaceView.setLayoutParams(layoutParams);
                    Log.d(FullscreenVideoView.TAG, "Resizing: initialMovieWidth: " + FullscreenVideoView.this.initialMovieWidth + " - initialMovieHeight: " + FullscreenVideoView.this.initialMovieHeight);
                    Log.d(FullscreenVideoView.TAG, "Resizing: screenWidth: " + width + " - screenHeight: " + height);
                }
            }
        });
    }

    public boolean isShouldAutoplay() {
        return this.shouldAutoplay;
    }

    public void setShouldAutoplay(boolean z) {
        this.shouldAutoplay = z;
    }

    @Deprecated
    public void fullscreen() throws IllegalStateException {
        setFullscreen(!this.fullscreen);
    }

    public int getCurrentPosition() {
        MediaPlayer mediaPlayer = this.mediaPlayer;
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public int getDuration() {
        MediaPlayer mediaPlayer = this.mediaPlayer;
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public int getVideoHeight() {
        MediaPlayer mediaPlayer = this.mediaPlayer;
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoHeight();
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public int getVideoWidth() {
        MediaPlayer mediaPlayer = this.mediaPlayer;
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoWidth();
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public boolean isLooping() {
        MediaPlayer mediaPlayer = this.mediaPlayer;
        if (mediaPlayer != null) {
            return mediaPlayer.isLooping();
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public boolean isPlaying() throws IllegalStateException {
        MediaPlayer mediaPlayer = this.mediaPlayer;
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void pause() throws IllegalStateException {
        Log.d(TAG, "pause");
        if (this.mediaPlayer != null) {
            this.currentState = State.PAUSED;
            this.mediaPlayer.pause();
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void reset() {
        Log.d(TAG, "reset");
        if (this.mediaPlayer != null) {
            this.currentState = State.IDLE;
            releaseObjects();
            initObjects();
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void start() throws IllegalStateException {
        Log.d(TAG, "start");
        if (this.mediaPlayer != null) {
            this.currentState = State.STARTED;
            this.mediaPlayer.setOnCompletionListener(this);
            this.mediaPlayer.start();
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void stop() throws IllegalStateException {
        Log.d(TAG, "stop");
        if (this.mediaPlayer != null) {
            this.currentState = State.STOPPED;
            this.mediaPlayer.stop();
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void seekTo(int i) throws IllegalStateException {
        Log.d(TAG, "seekTo = " + i);
        MediaPlayer mediaPlayer = this.mediaPlayer;
        if (mediaPlayer != null) {
            if (mediaPlayer.getDuration() <= -1 || i > this.mediaPlayer.getDuration()) {
                return;
            }
            this.lastState = this.currentState;
            pause();
            this.mediaPlayer.seekTo(i);
            startLoading();
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener onCompletionListener) {
        if (this.mediaPlayer != null) {
            this.completionListener = onCompletionListener;
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener onErrorListener) {
        if (this.mediaPlayer != null) {
            this.errorListener = onErrorListener;
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener) {
        MediaPlayer mediaPlayer = this.mediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void setOnInfoListener(MediaPlayer.OnInfoListener onInfoListener) {
        if (this.mediaPlayer != null) {
            this.infoListener = onInfoListener;
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener onSeekCompleteListener) {
        if (this.mediaPlayer != null) {
            this.seekCompleteListener = onSeekCompleteListener;
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener) {
        MediaPlayer mediaPlayer = this.mediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener onPreparedListener) {
        if (this.mediaPlayer != null) {
            this.preparedListener = onPreparedListener;
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void setLooping(boolean z) {
        MediaPlayer mediaPlayer = this.mediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(z);
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void setVolume(float f, float f2) {
        MediaPlayer mediaPlayer = this.mediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(f, f2);
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void setVideoPath(String str) throws IOException, RuntimeException {
        if (this.mediaPlayer != null) {
            if (this.currentState != State.IDLE) {
                throw new IllegalStateException("FullscreenVideoView Invalid State: " + this.currentState);
            }
            this.mediaPlayer.setDataSource(str);
            this.currentState = State.INITIALIZED;
            prepare();
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void setVideoURI(Uri uri) throws IOException, RuntimeException {
        if (this.mediaPlayer != null) {
            if (this.currentState != State.IDLE) {
                throw new IllegalStateException("FullscreenVideoView Invalid State: " + this.currentState);
            }
            this.mediaPlayer.setDataSource(this.context, uri);
            this.currentState = State.INITIALIZED;
            prepare();
            return;
        }
        throw new RuntimeException("Media Player is not initialized");
    }

    public void setOnProgressView(View view) {
        View view2 = this.onProgressView;
        if (view2 != null) {
            removeView(view2);
        }
        this.onProgressView = view;
        View view3 = this.onProgressView;
        if (view3 != null) {
            addView(view3);
        }
    }
}
