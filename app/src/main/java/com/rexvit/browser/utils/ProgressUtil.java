package com.rexvit.browser.utils;

import android.os.Build;
import android.widget.ProgressBar;


public class ProgressUtil {
    public static void updateProgressToViewWithMark(ProgressBar progressBar, long j) {
        updateProgressToViewWithMark(progressBar, j, true);
    }

    public static void updateProgressToViewWithMark(ProgressBar progressBar, long j, boolean z) {
        if (progressBar.getTag() == null) {
            return;
        }
        int intValue = (int) (j / ((Integer) progressBar.getTag()).intValue());
        if (Build.VERSION.SDK_INT >= 24) {
            progressBar.setProgress(intValue, z);
        } else {
            progressBar.setProgress(intValue);
        }
    }

    public static void calcProgressToViewAndMark(ProgressBar progressBar, long j, long j2) {
        calcProgressToViewAndMark(progressBar, j, j2, true);
    }

    public static void calcProgressToViewAndMark(ProgressBar progressBar, long j, long j2, boolean z) {
        int reducePrecision = reducePrecision(j2);
        int i = reducePrecision == 0 ? 1 : (int) (j2 / reducePrecision);
        progressBar.setTag(Integer.valueOf(i));
        int i2 = (int) (j / i);
        progressBar.setMax(reducePrecision);
        if (Build.VERSION.SDK_INT >= 24) {
            progressBar.setProgress(i2, z);
        } else {
            progressBar.setProgress(i2);
        }
    }

    private static int reducePrecision(long j) {
        if (j <= 2147483647L) {
            return (int) j;
        }
        int i = 10;
        while (j > 2147483647L) {
            j /= i;
            i *= 5;
        }
        return (int) j;
    }
}
