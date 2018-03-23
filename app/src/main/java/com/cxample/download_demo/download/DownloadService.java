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

    private DownloadTaskManager mTaskManager;

    public DownloadService() {
        super("DownloadService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null || intent.getAction() == null) return;
        if(mTaskManager == null) {
            mTaskManager = new DownloadTaskManager();
        }
        String action = intent.getAction();
        int[] downloadIds = intent.getIntArrayExtra(DownloadConstant.DOWNLOAD_IDS);
        switch(action) {
            case DownloadConstant.ACTION_START: {
                if(downloadIds != null && downloadIds.length > 0) {
                    mTaskManager.startTask(getApplicationContext(), downloadIds[0]);
                }
                break;
            }
            case DownloadConstant.ACTION_PAUSE: {
                if(downloadIds != null && downloadIds.length > 0) {
                    mTaskManager.pauseTask(getApplicationContext(), downloadIds[0]);
                }
                break;
            }
            case DownloadConstant.ACTION_DELETE: {
                if(downloadIds != null && downloadIds.length > 0) {
                    mTaskManager.deleteTask(getApplicationContext(), downloadIds[0]);
                }
                break;
            }
            case DownloadConstant.ACTION_PAUSE_ALL: {
                mTaskManager.pauseAllTask(getApplicationContext(), downloadIds);
                break;
            }
            case DownloadConstant.ACTION_START_ALL: {
                mTaskManager.startAllTask(getApplicationContext(), downloadIds);
                break;
            }
            case DownloadConstant.ACTION_DELETE_ALL: {
                mTaskManager.deleteAllTask(getApplicationContext(), downloadIds);
                break;
            }
        }
    }
}
