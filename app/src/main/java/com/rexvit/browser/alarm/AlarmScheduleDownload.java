package com.rexvit.browser.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.tonyodev.fetch2core.FetchCoreDefaults;


public class AlarmScheduleDownload {
    static final  boolean assertionsDisabled = false;
    private Context mContext;
    private final String TAG = "AlarmScheduleDownload";
    final int REQUEST_CODE = 12322;

    public AlarmScheduleDownload(Context context) {
        this.mContext = context;
    }

    public void setAlarm() {
        if (!alarmUp()) {
            cancelAlarm();
        }
        Intent intent = new Intent(this.mContext, ReceiverScheduleDownload.class);
        intent.setAction("com.rexvit.browser.DOWNLOAD");
        ((AlarmManager) this.mContext.getSystemService(Context.ALARM_SERVICE)).setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + FetchCoreDefaults.DEFAULT_PROGRESS_REPORTING_INTERVAL_IN_MILLISECONDS, 60000L, PendingIntent.getBroadcast(this.mContext, 12322, intent, PendingIntent.FLAG_IMMUTABLE));
        Log.d(this.TAG, "Alarm set...");
    }

    public boolean alarmUp() {
        Intent intent = new Intent(this.mContext, ReceiverScheduleDownload.class);
        intent.setAction("com.rexvit.browser.DOWNLOAD");
        boolean z = PendingIntent.getBroadcast(this.mContext, 12322, intent,  PendingIntent.FLAG_IMMUTABLE) != null;
        String str = this.TAG;
        Log.d(str, "Alarm is up? " + z);
        return z;
    }

    public void cancelAlarm() {
        Intent intent = new Intent(this.mContext, ReceiverScheduleDownload.class);
        intent.setAction("com.rexvit.browser.DOWNLOAD");
        PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 12322, intent, PendingIntent.FLAG_IMMUTABLE);
        ((AlarmManager) this.mContext.getSystemService(Context.ALARM_SERVICE)).cancel(broadcast);
        broadcast.cancel();
        Log.d(this.TAG, "Cancelling downloading alarm");
    }
}
