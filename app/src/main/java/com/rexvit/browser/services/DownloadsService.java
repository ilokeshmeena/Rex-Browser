package com.rexvit.browser.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.gson.JsonObject;
import com.rexvit.browser.R;
import com.rexvit.browser.activities.DownloadsActivity;
import com.rexvit.browser.activities.NewDownloadActivity;
import com.rexvit.browser.database.DataUpdatedEvent;
import com.rexvit.browser.fragment.DownloadingFragment;
import com.rexvit.browser.network.RequestService;
import com.rexvit.browser.network.RetrofitClient;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2.Status;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2core.FetchObserver;
import com.tonyodev.fetch2core.Func;
import com.tonyodev.fetch2core.Reason;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DownloadsService extends Service {
    public static final String ACTION_PAUSE_ALL_DOWNLOADS = "com.rexvit.browser.pause_all_downloads";
    public static final String ACTION_RESUME_ALL_DOWNLOADS = "com.rexvit.browser.resume_all_downloads";
    private static final int ONGOING_NOTIFICATION_ID = 7;
    private static final String SILENT_CHANNEL_ID = "silent_channel_id";
    Fetch fetch;
    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    NotificationManager notificationManager;
    private final String TAG = "DownloadsService";
    private final IBinder binder = new DownloadsBinder();
    boolean shouldStopService = true;
    FetchListener fetchListener = new FetchListener() {

        @Override
        public void onError(Download download, Error error, Throwable th) {
        }

        @Override
        public void onCancelled(Download download) {
        }

        @Override 
        public void onDeleted(Download download) {
        }

        @Override 
        public void onDownloadBlockUpdated(Download download, DownloadBlock downloadBlock, int i) {
        }



        @Override 
        public void onPaused(Download download) {
        }

        @Override 
        public void onProgress(Download download, long j, long j2) {
        }

        @Override 
        public void onQueued(Download download, boolean z) {
        }

        @Override 
        public void onResumed(Download download) {
        }

        @Override 
        public void onStarted(Download download, List<? extends DownloadBlock> list, int i) {
        }

        @Override 
        public void onWaitingNetwork(Download download) {
        }

        @Override 
        public void onAdded(Download download) {
            DownloadsService.this.updateNotificationDownloadCounter();
        }

        @Override 
        public void onCompleted(Download download) {
            DownloadsService.this.updateNotificationDownloadCounter();
        }

        @Override 
        public void onRemoved(Download download) {
            DownloadsService.this.updateNotificationDownloadCounter();
        }
    };

    
    public interface OnUrlGeneratedCallBack {
        void urlGenerated(String str);
    }

    
    private final class ServiceHandler extends Handler {
        @Override 
        public void handleMessage(Message message) {
        }

        public ServiceHandler(Looper looper) {
            super(looper);
        }
    }

    @Override 
    public void onCreate() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.notificationManager = notificationManager;
        createSilentNotificationChannel(notificationManager);
        startForeground(7, createNotificationBuilder().build());
        this.fetch = Fetch.Impl.getDefaultInstance();
        updateNotificationDownloadCounter();
        this.fetch.addListener(this.fetchListener);
        this.fetch.addActiveDownloadsObserver(true, new FetchObserver() { 
            @Override 
            public final void onChanged(Object obj, Reason reason) {
                DownloadsService.this.objOnCreate((Boolean) obj, reason);
            }
        });
        HandlerThread handlerThread = new HandlerThread("ServiceStartArguments", 10);
        handlerThread.start();
        this.mServiceLooper = handlerThread.getLooper();
        this.mServiceHandler = new ServiceHandler(this.mServiceLooper);
    }

    public  void objOnCreate(Boolean bool, Reason reason) {
        if (bool.booleanValue() || !this.shouldStopService) {
            return;
        }
        stopForeground(true);
        stopSelf();
    }

    @Override 
    public int onStartCommand(Intent intent, int i, int i2) {
        Log.d(this.TAG, "onStart command called");
        if (intent.getAction() != null) {
            handleIntentAction(intent);
            return Service.START_NOT_STICKY;
        }
        Message obtainMessage = this.mServiceHandler.obtainMessage();
        obtainMessage.arg1 = i2;
        this.mServiceHandler.sendMessage(obtainMessage);
        return Service.START_NOT_STICKY;
    }

    @Override 
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    @Override 
    public void onDestroy() {
        Log.d(this.TAG, "Service done");
    }

    
    public class DownloadsBinder extends Binder {
        public DownloadsBinder() {
        }

        public DownloadsService getService() {
            return DownloadsService.this;
        }
    }

    private void createSilentNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(SILENT_CHANNEL_ID, "Download booster", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Downloading");
            notificationChannel.setSound(null, null);
            notificationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private NotificationCompat.Builder createNotificationBuilder() {
        return new NotificationCompat.Builder(this, SILENT_CHANNEL_ID).setSmallIcon(R.drawable.ic_notifi_dow).setContentTitle(getString(R.string.app_name)).setContentText(getString(R.string.active_downloads)).addAction(R.drawable.ic_action_pause, getString(R.string.pause_all), createPendingIntentForAction(0, ACTION_PAUSE_ALL_DOWNLOADS)).addAction(R.drawable.ic_action_play, getString(R.string.start_all), createPendingIntentForAction(1, ACTION_RESUME_ALL_DOWNLOADS)).setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, DownloadsActivity.class), PendingIntent.FLAG_IMMUTABLE)).setPriority(-1).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setOngoing(true);
    }

    public void addNewDownload(Intent intent) {
        String stringExtra = intent.getStringExtra(NewDownloadActivity.DOWNLOAD_URL);
        final String stringExtra2 = intent.getStringExtra(NewDownloadActivity.DOWNLOAD_FILE_NAME);
        final String stringExtra3 = intent.getStringExtra(NewDownloadActivity.DOWNLOAD_PATH);
        if (isInstagramVideoUrl(stringExtra)) {
            getDirectVideoUrl(stringExtra, new OnUrlGeneratedCallBack() { 
                @Override 
                public final void urlGenerated(String str) {
                    DownloadsService.this.addNewDown(stringExtra3, stringExtra2, str);
                }
            });
        } else {
            addNewDown(stringExtra, stringExtra3, stringExtra2);
        }
    }

    
    
    public void addNewDown(String str, String str2, String str3) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(NewDownloadActivity.DEFAULT_DOWNLOAD_PATH, str2).apply();
        Request request = new Request(str, str2 + str3);
        request.setGroupId(DownloadingFragment.GROUP_ID);
        request.setDownloadOnEnqueue(true);
        this.fetch.enqueue(request, new Func() { 
            @Override 
            public final void call(Object obj) {
                DownloadsService.this.evAddDownloadToQueue((Request) obj);
            }
        }, new Func() { 
            @Override 
            public final void call(Object obj) {
                DownloadsService.this.erAddDownloadToQueue((Error) obj);
            }
        });
    }

    public  void evAddDownloadToQueue(Request request) {
        Log.d(this.TAG, "Request was successfully enqueued for download.");
        EventBus.getDefault().post(new DataUpdatedEvent.NewDownloadAdded());
    }

    public  void erAddDownloadToQueue(Error error) {
        Log.d(this.TAG, "An error occurred enqueuing the request.");
    }

    public void pauseDownload(int i) {
        this.fetch.pause(i);
        this.shouldStopService = false;
    }

    public void resumeDownload(int i) {
        this.fetch.resume(i);
        this.shouldStopService = true;
    }

    public void removeDownload(int i) {
        this.fetch.remove(i);
    }

    public void removeDownload(List<Integer> list) {
        this.fetch.delete(list);
    }

    public void updateDownload(Download download, String str) {
        Request request = download.getRequest();
        Request request2 = new Request(str, download.getFile());
        request2.setGroupId(request.getGroupId());
        request2.setIdentifier(request.getIdentifier());
        request2.setNetworkType(request.getNetworkType());
        request2.setPriority(request.getPriority());
        request2.setTag(request.getTag());
        this.fetch.updateRequest(download.getId(), request2, true, new Func() { 
            @Override 
            public final void call(Object obj) {
                DownloadsService.this.fetch.resume(download.getId());
                EventBus.getDefault().post(new DataUpdatedEvent.NewDownloadAdded());
            }
        }, new Func() { 
            @Override 
            public final void call(Object obj) {
                Log.d(DownloadsService.this.TAG, obj.toString());
            }
        });
    }


    public void retryDownload(int i) {
        this.fetch.retry(i);
    }

    private PendingIntent createPendingIntentForAction(int i, String str) {
        Intent intent = new Intent(this, DownloadsService.class);
        intent.setAction(str);
        return PendingIntent.getService(this, i, intent,  PendingIntent.FLAG_IMMUTABLE);
    }

    private void handleIntentAction(Intent intent) {
        String action = intent.getAction();
        action.hashCode();
        if (action.equals(ACTION_PAUSE_ALL_DOWNLOADS)) {
            this.shouldStopService = false;
            this.fetch.pauseAll();
            stopForeground(true);
            stopSelf();
        } else if (action.equals(ACTION_RESUME_ALL_DOWNLOADS)) {
            this.shouldStopService = true;
            this.fetch.resumeAll();
        }
    }

    
    public void updateNotificationDownloadCounter() {
        final ArrayList arrayList = new ArrayList();
        this.fetch.getDownloadsInGroup(DownloadingFragment.GROUP_ID, new Func() { 
            @Override 
            public final void call(Object obj) {
                DownloadsService.this.startUpdateNotificationDownload(arrayList, (List) obj);
            }
        });
    }

    public  void startUpdateNotificationDownload(List list, List list2) {
        Iterator it = list2.iterator();
        while (it.hasNext()) {
            Download download = (Download) it.next();
            if (download.getStatus() != Status.COMPLETED) {
                list.add(download);
            }
        }
        NotificationCompat.Builder createNotificationBuilder = createNotificationBuilder();
        if (list.size() > 0) {
            createNotificationBuilder.setContentText(getString(R.string.active_downloads, new Object[]{Integer.valueOf(list.size())}));
        } else {
            createNotificationBuilder.setContentText(getString(R.string.downloads));
        }
        this.notificationManager.notify(7, createNotificationBuilder.build());
    }

    private boolean isInstagramVideoUrl(String str) {
        return Pattern.matches("https://www\\.instagram\\.com/p/[a-zA-Z0-9]+/", str);
    }

    private void getDirectVideoUrl(String str, final OnUrlGeneratedCallBack onUrlGeneratedCallBack) {
        ((RequestService) RetrofitClient.getRetrofitInstance().create(RequestService.class)).getVideoUrl(Uri.parse(str).getPathSegments().get(1)).enqueue(new Callback<JsonObject>() {
            @Override 
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                onUrlGeneratedCallBack.urlGenerated(response.body().get("graphql").getAsJsonObject().get("shortcode_media").getAsJsonObject().get("video_url").getAsString());
            }

            @Override 
            public void onFailure(Call<JsonObject> call, Throwable th) {
                if (th.getMessage() != null) {
                    String str2 = DownloadsService.this.TAG;
                    Log.d(str2, "Error: " + th.getMessage());
                }
            }
        });
    }
}
