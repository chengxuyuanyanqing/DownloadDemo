package com.cxample.download_demo.download;

import android.content.Context;
import android.util.Log;

import com.cxample.download_demo.download.db.DownloadInfo;

/**
 * Created by yanqing on 2018/2/24.
 */

public class DownloadTask implements Runnable {
    private static final String TAG = "DownloadTask";
    public static final int TASK_STATE_WAITING = 0;
    public static final int TASK_STATE_RUNNING = 1;
    public static final int TASK_STATE_PAUSE = 2;
    public static final int TASK_STATE_FINISH = 3;
    public static final int TASK_STATE_ERROR = 4;

    private int mTaskId;
    private int mTaskTotalSize;
    private int mTaskFinishSize;
    private int mTaskState;

    private boolean isPause;
    private boolean isDelete;

    private Context mContext;

    public DownloadTask(Context context, DownloadInfo downloadInfo) {
        mContext = context;
        mTaskId = downloadInfo.mTaskId;
        mTaskTotalSize = downloadInfo.mTaskTotalSize;
        mTaskFinishSize = downloadInfo.mTaskFinishSize;
        mTaskState = downloadInfo.mTaskState;
    }

    public DownloadTask(Context context, int taskId, int taskTotalSize, int taskFinishSize, int taskState) {
        mTaskId = taskId;
        mTaskTotalSize = taskTotalSize;
        mTaskFinishSize = taskFinishSize;
        mTaskState = taskState;
    }

    public void pause() {
        isPause = true;
    }

    public void delete() {
        isDelete = true;
    }

    public void start() {
        isPause = false;
        isDelete = false;
    }

    @Override
    public void run() {
        try {
            download();
            onEnd();
        } catch(Exception e) {
            onError();
        }
    }

    private void onError() {
        Log.e(TAG, "onError: error" + mTaskId);
        mTaskState = TASK_STATE_ERROR;
        DownloadManager.updateDownloadInfo(mContext, getDownloadInfo());
    }

    private void onEnd() {
        if(isDelete) {
            Log.e(TAG, "onEnd: delete" + mTaskId);
            DownloadManager.deleteDownloadInfo(mContext, getDownloadInfo());
        } else {
            if(isPause) {
                Log.e(TAG, "onEnd: pause" + mTaskId);
                mTaskState = TASK_STATE_PAUSE;
            } else {
                Log.e(TAG, "onEnd: finish" + mTaskId);
                mTaskState = TASK_STATE_FINISH;
            }
            DownloadManager.updateDownloadInfo(mContext, getDownloadInfo());
        }
    }

    private void download() throws Exception {
        Log.e(TAG, "download: start" + mTaskId);
        mTaskState = TASK_STATE_RUNNING;
        while(!isPause && !isDelete) {
            if(mTaskTotalSize <= 0) {
                getDownloadTotalSize();
            } else {
                if(mTaskFinishSize == mTaskTotalSize) {
                    break;
                } else if(mTaskFinishSize > mTaskTotalSize) {
                    onError();
                    break;
                } else {
                    //继续下载
                    mTaskFinishSize++;
                    mTaskFinishSize++;
                    Thread.sleep(500);
//                    Log.e(TAG, "download: " + mTaskId + ":" + mTaskFinishSize);
                    DownloadManager.updateDownloadInfo(mContext, getDownloadInfo());
                }
            }
        }
    }

    private void getDownloadTotalSize() {
        //获取下载任务的总大小
        mTaskTotalSize = 100;
    }

    public DownloadInfo getDownloadInfo() {
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.mTaskId = mTaskId;
        downloadInfo.mTaskTotalSize = mTaskTotalSize;
        downloadInfo.mTaskFinishSize = mTaskFinishSize;
        downloadInfo.mTaskState = mTaskState;
        return downloadInfo;
    }
}
