package com.cxample.download_demo.download;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by yanqing on 2018/2/24.
 */

public class DownloadService extends IntentService {
    private static final String TAG = "DownloadService";

    public DownloadService() {
        super("DownloadService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null || intent.getAction() == null) return;
        String action = intent.getAction();
        int downloadId = intent.getIntExtra(DownloadConstant.DOWNLOAD_ID, -1);
        switch(action) {
            case DownloadConstant.ACTION_START: {
                Log.e(TAG, "onHandleIntent: start" + downloadId);
                if(downloadId != -1) {
                    DownloadTaskManager.startTask(getApplicationContext(), downloadId);
                }
                break;
            }
            case DownloadConstant.ACTION_PAUSE: {
                Log.e(TAG, "onHandleIntent: pause" + downloadId);
                if(downloadId != -1) {
                    DownloadTaskManager.pauseTask(getApplicationContext(), downloadId);
                }
                break;
            }
            case DownloadConstant.ACTION_DELETE: {
                Log.e(TAG, "onHandleIntent: delete" + downloadId);
                if(downloadId != -1) {
                    DownloadTaskManager.deleteTask(getApplicationContext(), downloadId);
                }
                break;
            }
        }
    }
}
