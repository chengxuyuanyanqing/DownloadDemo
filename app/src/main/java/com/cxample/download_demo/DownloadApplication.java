package com.cxample.download_demo;

import android.app.Application;

import com.cxample.download_demo.download.DownloadManager;

/**
 * Created by yanqing on 2018/2/24.
 */

public class DownloadApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DownloadManager.init(getApplicationContext());
    }
}
