package com.ads.control;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.rexvit.browser.R;


public class AdmobHelp {

    public static long TimeReload = 60000;
    private static AdmobHelp instance;
    public static long timeLoad;
    public AdCloseListener adCloseListener;
    public boolean isReloaded = false;

    public static InterstitialAd sInterstitialAd;

    public interface AdCloseListener {
        void onAdClosed();
    }

    public static AdmobHelp getInstance() {
        if (instance == null) {
            instance = new AdmobHelp();
        }
        return instance;
    }

    private AdmobHelp() {
    }

    public void init(Context context) {


    }

    public void loadBanner(Activity activity) {
        final ShimmerFrameLayout shimmerFrameLayout = (ShimmerFrameLayout) activity.findViewById(R.id.shimmer_container);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        final LinearLayout linearLayout = (LinearLayout) activity.findViewById(R.id.banner_container);
        try {
            AdView adView = new AdView(activity);
            adView.setAdUnitId(activity.getString(R.string.ads_banner));
            linearLayout.addView(adView);
            adView.setAdSize(getAdSize(activity));
            adView.loadAd(new AdRequest.Builder().build());
            adView.setAdListener(new AdListener() {
                @SuppressLint("WrongConstant")
                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    linearLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }

                @SuppressLint("WrongConstant")
                @Override
                public void onAdLoaded() {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception unused) {
            unused.printStackTrace();
        }
    }
    @SuppressLint("WrongConstant")
    public void loadBannerFragment(final Activity activity,final View view) {
        final ShimmerFrameLayout shimmerFrameLayout = (ShimmerFrameLayout) view.findViewById(R.id.shimmer_container);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        final LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.banner_container);
        try {
            AdView adView = new AdView(activity);
            adView.setAdUnitId(activity.getString(R.string.ads_banner));
            linearLayout.addView(adView);
            adView.setAdSize(getAdSize(activity));
            adView.loadAd(new AdRequest.Builder().build());
            adView.setAdListener(new AdListener() {
                @Override
                @SuppressLint("WrongConstant")
                public void onAdFailedToLoad(LoadAdError i) {
                    super.onAdFailedToLoad(i);
                    linearLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }

                @Override
                @SuppressLint("WrongConstant")
                public void onAdLoaded() {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception unused) {
        }
    }
    private AdSize getAdSize(Activity activity) {
        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getMetrics(displayMetrics);
        return AdSize.FULL_BANNER;
    }


}
