package com.cxample.download_demo.download;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cxample.download_demo.download.db.DownloadDao;
import com.cxample.download_demo.download.db.DownloadDatabase;
import com.cxample.download_demo.download.db.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanqing on 2018/2/24.
 */

public class DownloadManager {
    private static final String TAG = "DownloadManager";

    public static int MAX_ID = 0;//方便生成新的下载Id（测试用）

    private static List<DownloadInfo> sInfoList;

    public static void init(Context context) {
        sInfoList = new ArrayList<>();
        DownloadDatabase database = DownloadDatabase.getInstance(context);
        sInfoList = database.downloadDao().getAll();
        //方便生成新的下载Id（测试用）
        for(DownloadInfo info : sInfoList) {
            if(info.mTaskId > MAX_ID) {
                MAX_ID = info.mTaskId;
            }
        }
        Log.e(TAG, "init: " + sInfoList.size());
    }

    public static void release(){
        //退出应用时对任务做处理
    }

    public static void addDownloadInfo(DownloadInfo downloadInfo) {
        if(downloadInfo != null) {
            sInfoList.add(downloadInfo);
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
        for(DownloadInfo info : sInfoList) {
            if(info.mTaskId == id) {
                return info;
            }
        }
        return null;
    }

    public static List<DownloadInfo> getAllDownloadInfo() {
        return sInfoList;
    }

    public static void addDownload(Context context, DownloadInfo downloadInfo) {
        if(!checkIsExist(downloadInfo.mTaskId)) {
            sInfoList.add(downloadInfo);
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

    private static void startDownloadService(Context context, String action, int id) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(action);
        intent.putExtra(DownloadConstant.DOWNLOAD_ID, id);
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
}
