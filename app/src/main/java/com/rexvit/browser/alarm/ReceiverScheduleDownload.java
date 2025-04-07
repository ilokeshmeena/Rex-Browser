package com.rexvit.browser.alarm;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.rexvit.browser.fragment.DownloadingFragment;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.Status;
import com.tonyodev.fetch2core.Func;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ReceiverScheduleDownload extends BroadcastReceiver {
    private final String TAG = "ReceiverScheduleDownload";

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ReceiverScheduleDownload", "Alarm received for background download");
        new DownloadInBackground(context).execute(new Void[0]);
    }

    
    public class DownloadInBackground extends AsyncTask<Void, Void, Void> {
        private Context context;

        public DownloadInBackground(Context context) {
            this.context = context;
        }

        
        @SuppressLint("LongLogTag")
        @Override
        public Void doInBackground(Void... voidArr) {
            Fetch defaultInstance = Fetch.Impl.getDefaultInstance();
            boolean z = false;
            while (!z) {
                Log.d("ReceiverScheduleDownload", "Downloading in bg..");
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                z = ReceiverScheduleDownload.this.hasActiveDownloads(defaultInstance);
            }
            Log.d("ReceiverScheduleDownload", "No active downloads ");
            new AlarmScheduleDownload(this.context).cancelAlarm();
            return null;
        }
    }

    public boolean hasActiveDownloads(Fetch fetch) {
        final ArrayList arrayList = new ArrayList();
        fetch.getDownloadsInGroup(DownloadingFragment.GROUP_ID, new Func() { 
            @Override 
            public final void call(Object obj) {
                ReceiverScheduleDownload.listHasActiveDownloads(arrayList, (List) obj);
            }
        });
        return arrayList.size() > 0;
    }

    
    public static  void listHasActiveDownloads(ArrayList arrayList, List list) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Download download = (Download) it.next();
            if (download.getStatus() == Status.DOWNLOADING) {
                arrayList.add(download);
            }
        }
    }
}
