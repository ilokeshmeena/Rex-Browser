package com.ads.control;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.rexvit.browser.R;

public class InterstitialAdShow {
    public static InterstitialAd sInterstitialAd;
    Activity activity;

    public interface AdCloseListener {
        void onAdClosed();
    }

    private InterstitialAdShow(Activity activity) {
        this.activity = activity;
    }

    public static InterstitialAdShow getInstance(Activity activity) {
        return new InterstitialAdShow(activity);
    }


    public void loadInterstitialAd(Activity activity) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, activity.getResources().getString(R.string.ads_instritial), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(InterstitialAd interstitialAd) {
                InterstitialAdShow.sInterstitialAd = interstitialAd;
                Log.e("aaaaaaaaa", "onAdLoaded.");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.e("aaaaaaaaa", loadAdError.getMessage());
                InterstitialAdShow.sInterstitialAd = null;
            }
        });
    }

    public void showInterstitialAd(final Activity activity, final AdCloseListener onClose) {
        InterstitialAd interstitialAd = sInterstitialAd;
        if (interstitialAd != null) {
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    InterstitialAdShow.this.loadInterstitialAd(activity);
                    Log.e("aaaaaaaaa", "The ad was dismissed.");
                    onClose.onAdClosed();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    InterstitialAdShow.this.loadInterstitialAd(activity);
                    Log.e("aaaaaaaaa", "The ad failed to show.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.e("aaaaaaaaa", "The ad was shown.");
                }
            });
            interstitialAd.show(activity);
            return;
        }
        onClose.onAdClosed();
        loadInterstitialAd(activity);
    }
}