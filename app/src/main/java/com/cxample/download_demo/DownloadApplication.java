package com.cxample.download_demo;

import android.app.Application;
import android.content.Context;

import com.cxample.download_demo.download.DownloadManager;

/**
 * Created by yanqing on 2018/2/24.
 */

public class DownloadApplication extends Application {
    /**
     * 关于后台强制杀死应用时需要对download模块作出相应的处理
     */

    @Override
    public void onCreate() {
        super.onCreate();
        DownloadManager.init(getApplicationContext());
    }

    public static void exit() {
        int pid = android.os.Process.myPid();    //获取当前应用程序的PID
        android.os.Process.killProcess(pid);    //杀死当前进程
    }
}
