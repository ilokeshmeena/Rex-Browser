package com.rexvit.browser.services;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.rexvit.browser.activities.DownloadsActivity;
import com.tonyodev.fetch2.DefaultFetchNotificationManager;
import com.tonyodev.fetch2.DownloadNotification;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchIntent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MainDownloadNotificationManager extends DefaultFetchNotificationManager {
    private Context context;
    private Map<Integer, DownloadNotification> downloadNotificationsMap = new LinkedHashMap<>();
    private String notificationManagerAction = "DEFAULT_FETCH2_NOTIFICATION_MANAGER_ACTION_" + System.currentTimeMillis();


    public MainDownloadNotificationManager(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Fetch getFetchInstanceForNamespace(String str) {
        return Fetch.Impl.getDefaultInstance();
    }

    @Override
    public NotificationCompat.Builder getNotificationBuilder(int i, int i2) {
        PendingIntent activity = PendingIntent.getActivity(this.context, 5, new Intent(this.context, DownloadsActivity.class), PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notificationBuilder = super.getNotificationBuilder(i, i2);
        notificationBuilder.setContentIntent(activity);
        return notificationBuilder;
    }


    @Override
    public PendingIntent getActionPendingIntent(DownloadNotification downloadNotification, DownloadNotification.ActionType actionType) {
        PendingIntent broadcast;
        synchronized (this.downloadNotificationsMap) {
            Intent intent = new Intent(this.notificationManagerAction);
            intent.putExtra(FetchIntent.EXTRA_NAMESPACE, downloadNotification.getNamespace());
            intent.putExtra(FetchIntent.EXTRA_DOWNLOAD_ID, downloadNotification.getNotificationId());
            intent.putExtra(FetchIntent.EXTRA_NOTIFICATION_ID, downloadNotification.getNotificationId());
            intent.putExtra(FetchIntent.EXTRA_GROUP_ACTION, false);
            intent.putExtra(FetchIntent.EXTRA_NOTIFICATION_GROUP_ID, downloadNotification.getGroupId());
            int i = -1;
            switch (actionType) {
                case CANCEL:
                    i = 4;
                    break;
                case DELETE:
                    i = 2;
                    break;
                case RESUME:
                    i = 1;
                    break;
                case PAUSE:
                    i = 3;
                    break;
                case RETRY:
                    i = 5;
                    break;
            }
            intent.putExtra(FetchIntent.EXTRA_ACTION_TYPE, i);
            broadcast = PendingIntent.getBroadcast(this.context, downloadNotification.getNotificationId() + i, intent, PendingIntent.FLAG_IMMUTABLE);
        }
        return broadcast;
    }

    @Override
    public PendingIntent getGroupActionPendingIntent(int i, List<? extends DownloadNotification> list, DownloadNotification.ActionType actionType) {
        PendingIntent broadcast;
        synchronized (this.downloadNotificationsMap) {
            Intent intent = new Intent(this.notificationManagerAction);
            intent.putExtra(FetchIntent.EXTRA_NOTIFICATION_GROUP_ID, i);
            intent.putExtra(FetchIntent.EXTRA_DOWNLOAD_NOTIFICATIONS, new ArrayList<>(list));
            intent.putExtra(FetchIntent.EXTRA_GROUP_ACTION, true);
            int i2 = -1;
            switch (actionType) {
                case CANCEL_ALL:
                    i2 = 8;
                    break;
                case DELETE_ALL:
                    i2 = 9;
                    break;
                case RESUME_ALL:
                    i2 = 7;
                    break;
                case PAUSE_ALL:
                    i2 = 6;
                    break;
                case RETRY_ALL:
                    i2 = 10;
                    break;
            }
            intent.putExtra(FetchIntent.EXTRA_ACTION_TYPE, i2);
            broadcast = PendingIntent.getBroadcast(this.context, i + i2, intent, PendingIntent.FLAG_IMMUTABLE);
        }
        return broadcast;
    }

}
