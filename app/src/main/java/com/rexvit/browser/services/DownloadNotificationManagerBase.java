package com.rexvit.browser.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.rexvit.browser.R;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.DownloadNotification;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchIntent;
import com.tonyodev.fetch2.FetchNotificationManager;
import com.tonyodev.fetch2.Status;
import com.tonyodev.fetch2.util.FetchDefaults;
import com.tonyodev.fetch2.util.NotificationUtilsKt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class DownloadNotificationManagerBase implements FetchNotificationManager {
    private Context context;
    private NotificationManager notificationManager;
    private Map<Integer, DownloadNotification> downloadNotificationsMap = new LinkedHashMap();
    private Set downloadNotificationExcludeSet = new LinkedHashSet();
    private String notificationManagerAction = "DEFAULT_FETCH2_NOTIFICATION_MANAGER_ACTION_" + System.currentTimeMillis();

    @Override 
    public abstract Fetch getFetchInstanceForNamespace(String str);

    @Override 
    public long getNotificationTimeOutMillis() {
        return 10000L;
    }

    public DownloadNotificationManagerBase(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        initialize();
    }

    @Override 
    public String getNotificationManagerAction() {
        return this.notificationManagerAction;
    }

    @Override 
    public BroadcastReceiver getBroadcastReceiver() {
        return new BroadcastReceiver() { 
            @Override 
            public void onReceive(Context context, Intent intent) {
                NotificationUtilsKt.onDownloadNotificationActionTriggered(context, intent, (com.tonyodev.fetch2.FetchNotificationManager) DownloadNotificationManagerBase.this);
            }
        };
    }

    private final void initialize() {
        registerBroadcastReceiver();
        createNotificationChannels(this.context, this.notificationManager);
    }

    @Override 
    public void registerBroadcastReceiver() {
        this.context.registerReceiver(getBroadcastReceiver(), new IntentFilter(getNotificationManagerAction()));
    }

    @Override 
    public void unregisterBroadcastReceiver() {
        this.context.unregisterReceiver(getBroadcastReceiver());
    }

    @Override 
    public void createNotificationChannels(Context context, NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= 26) {
            createChannel(R.string.notification_channel_silent_id, R.string.notification_channel_silent_name, 2);
            createChannel(R.string.notification_channel_loud_id, R.string.notification_channel_loud_name, 3);
        }
    }

    private void createChannel(int i, int i2, int i3) {
        String string = this.context.getString(i);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (this.notificationManager.getNotificationChannel(string) == null) {
                this.notificationManager.createNotificationChannel(new NotificationChannel(string, this.context.getString(i2), i3));
            }
        }
    }

    @Override 
    public String getChannelId(int i, Context context) {
        DownloadNotification downloadNotification = this.downloadNotificationsMap.get(Integer.valueOf(i));
        if (downloadNotification != null) {
            if (downloadNotification.getStatus().equals(Status.COMPLETED) || downloadNotification.getStatus().equals(Status.FAILED)) {
                return context.getString(R.string.notification_channel_loud_id);
            }
            return context.getString(R.string.notification_channel_silent_id);
        }
        return context.getString(R.string.notification_channel_silent_id);
    }

    @Override 
    public boolean updateGroupSummaryNotification(int i, NotificationCompat.Builder builder, List list, Context context) {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            DownloadNotification downloadNotification = (DownloadNotification) it.next();
            String subtitleText = getSubtitleText(context, downloadNotification);
            inboxStyle.addLine("" + downloadNotification.getTotal() + ' ' + subtitleText);
        }
        builder.setPriority(0).setSmallIcon(17301634).setContentTitle(context.getString(R.string.fetch_notification_default_channel_name)).setContentText("").setStyle(inboxStyle).setGroup(String.valueOf(i)).setGroupSummary(true);
        return false;
    }

    @Override 
    public void updateNotification(NotificationCompat.Builder builder, DownloadNotification downloadNotification, Context context) {
        builder.setPriority(0).setSmallIcon(downloadNotification.isDownloading() ? 17301633 : 17301634).setContentTitle(downloadNotification.getTitle()).setContentText(getSubtitleText(context, downloadNotification)).setOngoing(downloadNotification.isOnGoingNotification()).setGroup(String.valueOf(downloadNotification.getGroupId())).setGroupSummary(false);
        if (!downloadNotification.isFailed() && !downloadNotification.isCompleted()) {
            builder.setProgress(downloadNotification.getProgressIndeterminate() ? 0 : 100, downloadNotification.getProgress() >= 0 ? downloadNotification.getProgress() : 0, downloadNotification.getProgressIndeterminate());
        } else {
            builder.setProgress(0, 0, false);
        }
        if (downloadNotification.isDownloading()) {
            builder.setTimeoutAfter(getNotificationTimeOutMillis()).addAction(R.drawable.fetch_notification_pause, context.getString(R.string.fetch_notification_download_pause), getActionPendingIntent(downloadNotification, DownloadNotification.ActionType.PAUSE)).addAction(R.drawable.fetch_notification_cancel, context.getString(R.string.fetch_notification_download_cancel), getActionPendingIntent(downloadNotification, DownloadNotification.ActionType.CANCEL));
        } else if (downloadNotification.isPaused()) {
            builder.setTimeoutAfter(getNotificationTimeOutMillis()).addAction(R.drawable.fetch_notification_resume, context.getString(R.string.fetch_notification_download_resume), getActionPendingIntent(downloadNotification, DownloadNotification.ActionType.RESUME)).addAction(R.drawable.fetch_notification_cancel, context.getString(R.string.fetch_notification_download_cancel), getActionPendingIntent(downloadNotification, DownloadNotification.ActionType.CANCEL));
        } else if (downloadNotification.isQueued()) {
            builder.setTimeoutAfter(getNotificationTimeOutMillis());
        } else {
            builder.setTimeoutAfter(FetchDefaults.DEFAULT_NOTIFICATION_TIMEOUT_AFTER_RESET);
        }
    }

    @Override 
    public PendingIntent getActionPendingIntent(DownloadNotification downloadNotification, DownloadNotification.ActionType actionType) {
        PendingIntent broadcast;
        synchronized (this.downloadNotificationsMap) {
            Intent intent = new Intent(this.notificationManagerAction);
            intent.putExtra(FetchIntent.EXTRA_NAMESPACE, downloadNotification.getNamespace());
            intent.putExtra(FetchIntent.EXTRA_DOWNLOAD_ID, downloadNotification.getNotificationId());
            intent.putExtra(FetchIntent.EXTRA_NOTIFICATION_ID, downloadNotification.getNotificationId());
            int i = 0;
            intent.putExtra(FetchIntent.EXTRA_GROUP_ACTION, false);
            intent.putExtra(FetchIntent.EXTRA_NOTIFICATION_GROUP_ID, downloadNotification.getGroupId());
            if (actionType == DownloadNotification.ActionType.CANCEL) {
                i = 4;
            } else if (actionType == DownloadNotification.ActionType.DELETE) {
                i = 2;
            } else if (actionType == DownloadNotification.ActionType.RESUME) {
                i = 1;
            } else if (actionType != DownloadNotification.ActionType.PAUSE) {
                i = actionType == DownloadNotification.ActionType.RETRY ? 5 : -1;
            }
            intent.putExtra(FetchIntent.EXTRA_ACTION_TYPE, i);
            broadcast = PendingIntent.getBroadcast(this.context, downloadNotification.getNotificationId() + i, intent, PendingIntent.FLAG_IMMUTABLE);
        }
        return broadcast;
    }

    @Override 
    public PendingIntent getGroupActionPendingIntent(int i, List list, DownloadNotification.ActionType actionType) {
        int i2;
        PendingIntent broadcast;
        synchronized (this.downloadNotificationsMap) {
            Intent intent = new Intent(this.notificationManagerAction);
            intent.putExtra(FetchIntent.EXTRA_NOTIFICATION_GROUP_ID, i);
            intent.putExtra(FetchIntent.EXTRA_DOWNLOAD_NOTIFICATIONS, new ArrayList(list));
            intent.putExtra(FetchIntent.EXTRA_GROUP_ACTION, true);
            if (actionType == DownloadNotification.ActionType.CANCEL_ALL) {
                i2 = 8;
            } else if (actionType == DownloadNotification.ActionType.DELETE_ALL) {
                i2 = 9;
            } else if (actionType == DownloadNotification.ActionType.RESUME_ALL) {
                i2 = 7;
            } else if (actionType == DownloadNotification.ActionType.PAUSE_ALL) {
                i2 = 6;
            } else {
                i2 = actionType == DownloadNotification.ActionType.RETRY_ALL ? 10 : -1;
            }
            intent.putExtra(FetchIntent.EXTRA_ACTION_TYPE, i2);
            broadcast = PendingIntent.getBroadcast(this.context, i + i2, intent, PendingIntent.FLAG_IMMUTABLE);
        }
        return broadcast;
    }

    @Override 
    public void cancelNotification(int i) {
        synchronized (this.downloadNotificationsMap) {
            this.notificationManager.cancel(i);
            this.downloadNotificationExcludeSet.remove(Integer.valueOf(i));
            DownloadNotification downloadNotification = this.downloadNotificationsMap.get(Integer.valueOf(i));
            if (downloadNotification != null) {
                this.downloadNotificationsMap.remove(Integer.valueOf(i));
                notify(downloadNotification.getGroupId());
            }
        }
    }

    @Override 
    public void cancelOngoingNotifications() {
        synchronized (this.downloadNotificationsMap) {
            Iterator<DownloadNotification> it = this.downloadNotificationsMap.values().iterator();
            while (it.hasNext()) {
                DownloadNotification next = it.next();
                if (!next.isFailed() && !next.isCompleted()) {
                    this.notificationManager.cancel(next.getNotificationId());
                    this.downloadNotificationExcludeSet.remove(Integer.valueOf(next.getNotificationId()));
                    it.remove();
                    notify(next.getGroupId());
                }
            }
        }
    }

    @Override 
    public void notify(int i) {
        synchronized (this.downloadNotificationsMap) {
            Collection<DownloadNotification> values = this.downloadNotificationsMap.values();
            ArrayList arrayList = new ArrayList();
            for (Object obj : values) {
                if (((DownloadNotification) obj).getGroupId() == i) {
                    arrayList.add(obj);
                }
            }
            ArrayList<DownloadNotification> arrayList2 = arrayList;
            NotificationCompat.Builder notificationBuilder = getNotificationBuilder(i, i);
            boolean updateGroupSummaryNotification = updateGroupSummaryNotification(i, notificationBuilder, arrayList2, this.context);
            for (DownloadNotification downloadNotification : arrayList2) {
                if (shouldUpdateNotification(downloadNotification)) {
                    int notificationId = downloadNotification.getNotificationId();
                    NotificationCompat.Builder notificationBuilder2 = getNotificationBuilder(notificationId, i);
                    updateNotification(notificationBuilder2, downloadNotification, this.context);
                    this.notificationManager.notify(notificationId, notificationBuilder2.build());
                    if (downloadNotification.getStatus().equals(Status.COMPLETED) || downloadNotification.getStatus().equals(Status.FAILED)) {
                        this.downloadNotificationExcludeSet.add(Integer.valueOf(downloadNotification.getNotificationId()));
                    }
                }
            }
            if (updateGroupSummaryNotification) {
                this.notificationManager.notify(i, notificationBuilder.build());
            }
        }
    }

    @Override 
    public boolean shouldUpdateNotification(DownloadNotification downloadNotification) {
        return !this.downloadNotificationExcludeSet.contains(Integer.valueOf(downloadNotification.getNotificationId()));
    }

    @Override 
    public boolean shouldCancelNotification(DownloadNotification downloadNotification) {
        return downloadNotification.isPaused();
    }

    @Override 
    public boolean postDownloadUpdate(Download download) {
        synchronized (this.downloadNotificationsMap) {
            if (this.downloadNotificationsMap.size() > 50) {
                this.downloadNotificationsMap.clear();
            }
            DownloadNotification downloadNotification = this.downloadNotificationsMap.get(Integer.valueOf(download.getId())) != null ? this.downloadNotificationsMap.get(Integer.valueOf(download.getId())) : new DownloadNotification();
            downloadNotification.setStatus(download.getStatus());
            downloadNotification.setProgress(download.getProgress());
            downloadNotification.setNotificationId(download.getId());
            downloadNotification.setGroupId(download.getGroup());
            downloadNotification.setEtaInMilliSeconds(download.getEtaInMilliSeconds());
            downloadNotification.setDownloadedBytesPerSecond(download.getDownloadedBytesPerSecond());
            downloadNotification.setTotal(download.getTotal());
            downloadNotification.setDownloaded(download.getDownloaded());
            downloadNotification.setNamespace(download.getNamespace());
            downloadNotification.setTitle(getDownloadNotificationTitle(download));
            this.downloadNotificationsMap.put(Integer.valueOf(download.getId()), downloadNotification);
            if (this.downloadNotificationExcludeSet.contains(Integer.valueOf(downloadNotification.getNotificationId())) && !downloadNotification.isFailed() && !downloadNotification.isCompleted()) {
                this.downloadNotificationExcludeSet.remove(Integer.valueOf(downloadNotification.getNotificationId()));
            }
            if (!downloadNotification.isCancelledNotification() && !shouldCancelNotification(downloadNotification)) {
                notify(download.getGroup());
            }
            cancelNotification(downloadNotification.getNotificationId());
        }
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override 
    public NotificationCompat.Builder getNotificationBuilder(int i, int i2) {
        NotificationCompat.Builder builder;
        synchronized (this.downloadNotificationsMap) {
            Context context = this.context;
            builder = new NotificationCompat.Builder(context, getChannelId(i, context));
            builder.setGroup(String.valueOf(i)).setStyle(null).setProgress(0, 0, false).setContentTitle(null).setContentText(null).setContentIntent(null).setGroupSummary(false).setTimeoutAfter(FetchDefaults.DEFAULT_NOTIFICATION_TIMEOUT_AFTER_RESET).setOngoing(false).setGroup(String.valueOf(i2)).setSmallIcon(17301634).mActions.clear();
        }
        return builder;
    }

    @Override 
    public String getDownloadNotificationTitle(Download download) {
        String lastPathSegment = download.getFileUri().getLastPathSegment();
        if (lastPathSegment == null) {
            lastPathSegment = Uri.parse(download.getUrl()).getLastPathSegment();
        }
        return lastPathSegment == null ? download.getUrl() : lastPathSegment;
    }

    @Override 
    public String getSubtitleText(Context context, DownloadNotification downloadNotification) {
        if (downloadNotification.isCompleted()) {
            return context.getString(R.string.fetch_notification_download_complete);
        }
        if (downloadNotification.isFailed()) {
            return context.getString(R.string.fetch_notification_download_failed);
        }
        if (downloadNotification.isPaused()) {
            return context.getString(R.string.fetch_notification_download_paused);
        }
        if (downloadNotification.isQueued()) {
            return context.getString(R.string.fetch_notification_download_starting);
        }
        if (downloadNotification.getEtaInMilliSeconds() < 0) {
            return context.getString(R.string.fetch_notification_download_downloading);
        }
        return getEtaText(context, downloadNotification.getEtaInMilliSeconds());
    }

    private final String getEtaText(Context context, long j) {
        long j2 = j / 1000;
        long j3 = j2 / 3600;
        long j4 = j2 - (3600 * j3);
        long j5 = j4 / 60;
        long j6 = j4 - (60 * j5);
        return j3 > 0 ? context.getString(R.string.fetch_notification_download_eta_hrs, Long.valueOf(j3), Long.valueOf(j5), Long.valueOf(j6)) : j5 > 0 ? context.getString(R.string.fetch_notification_download_eta_min, Long.valueOf(j5), Long.valueOf(j6)) : context.getString(R.string.fetch_notification_download_eta_sec, Long.valueOf(j6));
    }
}
