package com.cxample.download_demo.download;

import android.content.Context;

import com.cxample.download_demo.download.db.DownloadInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanqing on 2018/2/24.
 */

public class DownloadTaskManager {
    private static final int TASK_MAX_COUNT = 5;

    private static final ExecutorService sExecutorService;

    private static ConcurrentHashMap<Integer, DownloadTask> sTaskList;
    private static LinkedBlockingQueue<Runnable> sWaitingQueue;

    static {
        sTaskList = new ConcurrentHashMap<>();
        sWaitingQueue = new LinkedBlockingQueue<>();
        sExecutorService = new ThreadPoolExecutor(TASK_MAX_COUNT, TASK_MAX_COUNT, 0, TimeUnit.SECONDS, sWaitingQueue);
    }

    public static synchronized void startTask(Context context, int taskId) {
        DownloadInfo downloadInfo = DownloadManager.getDownloadInfo(taskId);
        if(downloadInfo != null && downloadInfo.mTaskState != DownloadTask.TASK_STATE_FINISH) {
            downloadInfo.mTaskState = DownloadTask.TASK_STATE_WAITING;
            DownloadTask task = new DownloadTask(context, downloadInfo);
            sTaskList.put(downloadInfo.mTaskId, task);
            sExecutorService.execute(task);
        }
    }

    public static synchronized void pauseTask(Context context, int taskId) {
        DownloadTask task = sTaskList.remove(taskId);
        if(task != null) {
            task.pause();
        }
    }

    public static synchronized void deleteTask(Context context, int taskId) {
        DownloadTask task = sTaskList.remove(taskId);
        if(task != null) {
            task.delete();
        }
    }
}
