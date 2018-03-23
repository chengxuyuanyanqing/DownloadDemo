package com.cxample.download_demo.download;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cxample.download_demo.download.db.DownloadInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yanqing on 2018/2/24.
 */

public class DownloadTaskManager {
    private static final String TAG = "DownloadTaskManager";
    private static final int TASK_MAX_COUNT = 5;

    private static final ExecutorService sExecutorService;

    private static ConcurrentHashMap<Integer, DownloadTask> sTaskList;

    static {
        sTaskList = new ConcurrentHashMap<>();
        sExecutorService = Executors.newFixedThreadPool(TASK_MAX_COUNT);
    }

    public synchronized void startTask(Context context, int taskId) {
        DownloadInfo downloadInfo = DownloadManager.getDownloadInfo(taskId);
        if(downloadInfo != null && downloadInfo.mTaskState != DownloadTask.TASK_STATE_FINISH) {
            downloadInfo.mTaskState = DownloadTask.TASK_STATE_WAITING;
            DownloadTask task = new DownloadTask(context, downloadInfo);
            sTaskList.put(downloadInfo.mTaskId, task);
            sExecutorService.execute(task);
        }
    }

    public synchronized void pauseTask(Context context, int taskId) {
        DownloadTask task = sTaskList.remove(taskId);
        if(task != null) {
            task.pause();
        }
    }

    public synchronized void deleteTask(Context context, int taskId) {
        DownloadTask task = sTaskList.remove(taskId);
        if(task != null) {
            task.delete();
        }
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.mTaskId = taskId;
        DownloadManager.deleteDownloadInfo(context, downloadInfo);
    }

    public synchronized void pauseAllTask(Context context, int[] ids) {
        if(ids == null || ids.length <= 0) return;
        for(int id : ids) {
            pauseTask(context, id);
        }
    }

    public synchronized void startAllTask(Context context, int[] ids) {
        if(ids == null || ids.length <= 0) return;
        long time = System.currentTimeMillis();
        for(int id : ids) {
            startTask(context, id);
        }
        long endTime = System.currentTimeMillis();
        Log.e(TAG, "startAllTask: " + (endTime - time));
    }

    public synchronized void deleteAllTask(Context context, int[] ids) {
        if(ids != null && ids.length > 0) {
            for(int id : ids) {
                deleteTask(context, id);
            }
        }
        //删除完成发送广播通知
        Intent intent = new Intent();
        intent.setAction(DownloadConstant.DOWNLOAD_DELETE_COMPLETE);
        context.sendBroadcast(intent);
    }
}
