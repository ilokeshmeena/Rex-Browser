package com.rexvit.browser.view.viewListener;

import android.content.Context;
import android.media.MediaPlayer;
import com.rexvit.browser.Interface.UIController;


public class VideoCompletionListener implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    Context c;
    UIController mUIController;

    @Override 
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        return false;
    }

    public VideoCompletionListener(Context context) {
        this.c = context;
        this.mUIController = (UIController) context;
    }

    @Override 
    public void onCompletion(MediaPlayer mediaPlayer) {
        this.mUIController.onHideCustomView();
    }
}
