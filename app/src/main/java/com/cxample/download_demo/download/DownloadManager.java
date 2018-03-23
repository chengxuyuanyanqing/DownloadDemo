package com.cxample.download_demo.download;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cxample.download_demo.download.db.DownloadDao;
import com.cxample.download_demo.download.db.DownloadDatabase;
import com.cxample.download_demo.download.db.DownloadInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yanqing on 2018/2/24.
 */

public class DownloadManager {
    private static final String TAG = "DownloadManager";

    public static int MAX_ID = 0;//方便生成新的下载Id（测试用）

    private static List<DownloadInfo> sInfoList;

    public static void init(Context context) {
        sInfoList = Collections.synchronizedList(new ArrayList<DownloadInfo>());
        DownloadDatabase database = DownloadDatabase.getInstance(context);
        long time = System.currentTimeMillis();
        sInfoList = database.downloadDao().getAll();
        long endTime = System.currentTimeMillis();
        Log.e(TAG, "init db :" + (endTime - time) + " ms");
        initDownloadStatus(context);
        //方便生成新的下载Id（测试用）
        long time1 = System.currentTimeMillis();
        for(DownloadInfo info : sInfoList) {
            if(info.mTaskId > MAX_ID) {
                MAX_ID = info.mTaskId;
            }
        }

        long endTime1 = System.currentTimeMillis();
        Log.e(TAG, "init maxId :" + ((endTime1 - time1)) + " ms");
        Log.e(TAG, "init: " + sInfoList.size());
    }

    private static void initDownloadStatus(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                ArrayList<Integer> ids = new ArrayList<>();
                for(DownloadInfo info : sInfoList) {
                    if(info.mTaskState == DownloadTask.TASK_STATE_RUNNING
                            || info.mTaskState == DownloadTask.TASK_STATE_WAITING) {
                        ids.add(info.mTaskId);
                    }
                }
                startAllDownload(context, ids);
                long endTime = System.currentTimeMillis();
                Log.e(TAG, "run: init time :" + ((endTime - time)) + " s");
            }
        }).start();
    }

    public static void addDownloadInfo(Context context, DownloadInfo downloadInfo) {
        if(downloadInfo != null) {
            sInfoList.add(downloadInfo);
            updateDownloadInfoByDB(context, downloadInfo);
        }
    }

    public static void updateDownloadInfo(Context context, DownloadInfo downloadInfo) {
        if(downloadInfo != null) {
            for(DownloadInfo info : sInfoList) {
                if(info.mTaskId == downloadInfo.mTaskId) {
                    info.mTaskTotalSize = downloadInfo.mTaskTotalSize;
                    info.mTaskFinishSize = downloadInfo.mTaskFinishSize;
                    info.mTaskState = downloadInfo.mTaskState;
                }
            }
            updateDownloadInfoByDB(context, downloadInfo);
        }
    }

    private static void updateDownloadInfoByDB(Context context, DownloadInfo downloadInfo) {
        if(downloadInfo != null) {
            DownloadDao downloadDao = DownloadDatabase.getInstance(context).downloadDao();
            downloadDao.insert(downloadInfo);
        }
    }

    public static void deleteDownloadInfo(Context context, DownloadInfo downloadInfo) {
        if(downloadInfo != null) {
            for(int i = 0; i < sInfoList.size(); i++) {
                DownloadInfo info = sInfoList.get(i);
                if(info.mTaskId == downloadInfo.mTaskId) {
                    sInfoList.remove(i);
                    break;
                }
            }
            deleteDownloadInfoByDB(context, downloadInfo);
        }
    }

    private static void deleteDownloadInfoByDB(Context context, DownloadInfo downloadInfo) {
        if(downloadInfo != null) {
            DownloadDao downloadDao = DownloadDatabase.getInstance(context).downloadDao();
            downloadDao.delete(downloadInfo);
        }
    }

    public static DownloadInfo getDownloadInfo(int id) {
        for(int i = sInfoList.size() - 1; i >= 0; i--) {
            DownloadInfo info = sInfoList.get(i);
            if(info.mTaskId == id) {
                return info;
            }
        }
        return null;
    }

    public static List<DownloadInfo> getAllDownloadInfo() {
        return sInfoList;
    }

    public static List<DownloadInfo> getAllRunningAndWaitingDownload() {
        List<DownloadInfo> infos = new ArrayList<>();
        for(DownloadInfo info : sInfoList) {
            if(info.mTaskState == DownloadTask.TASK_STATE_RUNNING || info.mTaskState == DownloadTask.TASK_STATE_WAITING) {
                infos.add(info);
            }
        }
        return infos;
    }

    public static List<DownloadInfo> getAllPauseAndErrorDownload() {
        List<DownloadInfo> infos = new ArrayList<>();
        for(DownloadInfo info : sInfoList) {
            if(info.mTaskState == DownloadTask.TASK_STATE_PAUSE || info.mTaskState == DownloadTask.TASK_STATE_ERROR) {
                infos.add(info);
            }
        }
        return infos;
    }

    public static void addDownload(Context context, DownloadInfo downloadInfo) {
        if(!checkIsExist(downloadInfo.mTaskId)) {
            addDownloadInfo(context, downloadInfo);
        }
        startDownloadService(context, DownloadConstant.ACTION_START, downloadInfo.mTaskId);
    }

    public static void pauseDownload(Context context, DownloadInfo downloadInfo) {
        if(checkIsExist(downloadInfo.mTaskId)) {
            startDownloadService(context, DownloadConstant.ACTION_PAUSE, downloadInfo.mTaskId);
        }
    }

    public static void deleteDownload(Context context, DownloadInfo downloadInfo) {
        if(checkIsExist(downloadInfo.mTaskId)) {
            startDownloadService(context, DownloadConstant.ACTION_DELETE, downloadInfo.mTaskId);
        }
    }

    public static void pauseAllDownload(Context context, ArrayList<Integer> ids) {
        if(ids == null || ids.size() <= 0) return;
        startDownloadService(context, DownloadConstant.ACTION_PAUSE_ALL, toIntArray(ids));
    }

    public static void pauseAllDownload(Context context, int... ids) {
        startDownloadService(context, DownloadConstant.ACTION_PAUSE_ALL, ids);
    }

    public static void startAllDownload(Context context, ArrayList<Integer> ids) {
        if(ids == null || ids.size() <= 0) return;
        startDownloadService(context, DownloadConstant.ACTION_START_ALL, toIntArray(ids));
    }

    public static void startAllDownload(Context context, int... ids) {
        startDownloadService(context, DownloadConstant.ACTION_START_ALL, ids);
    }

    public static void deleteAllDownload(Context context, ArrayList<Integer> ids) {
        if(ids == null || ids.size() <= 0) return;
        startDownloadService(context, DownloadConstant.ACTION_DELETE_ALL, toIntArray(ids));
    }

    public static void deleteAllDownload(Context context, int... ids) {
        startDownloadService(context, DownloadConstant.ACTION_DELETE_ALL, ids);
    }

    private static void startDownloadService(Context context, String action, int... ids) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(action);
        intent.putExtra(DownloadConstant.DOWNLOAD_IDS, ids);
        context.startService(intent);
    }

    private static boolean checkIsExist(int id) {
        for(DownloadInfo info : sInfoList) {
            if(info.mTaskId == id) {
                return true;
            }
        }
        return false;
    }

    private static int[] toIntArray(ArrayList<Integer> arrayList) {
        if(arrayList == null || arrayList.size() == 0) {
            return null;
        }
        int[] ints = new int[arrayList.size()];
        for(int i = 0; i < arrayList.size(); i++) {
            ints[i] = arrayList.get(i);
        }
        return ints;
    }
}
