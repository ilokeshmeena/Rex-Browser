package com.rexvit.browser.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;


public abstract class NetworkReceiver extends BroadcastReceiver {
    public abstract void onConnectivityChange(boolean z);

    @Override 
    public void onReceive(Context context, Intent intent) {
        onConnectivityChange(isConnected(context));
    }

    private static boolean isConnected(@NonNull Context context) {
        NetworkInfo activeNetworkInfo;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager == null || (activeNetworkInfo = connectivityManager.getActiveNetworkInfo()) == null || !activeNetworkInfo.isConnected()) ? false : true;
    }
}
