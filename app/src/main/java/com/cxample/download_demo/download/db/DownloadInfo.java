package com.cxample.download_demo.download.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by yanqing on 2018/2/24.
 */

@Entity(tableName = "download_info")
public class DownloadInfo implements Serializable {
    @ColumnInfo(name = "task_id")
    @PrimaryKey
    public int mTaskId;
    @ColumnInfo(name = "task_total_size")
    public int mTaskTotalSize;
    @ColumnInfo(name = "task_finish_size")
    public int mTaskFinishSize;
    @ColumnInfo(name = "task_state")
    public int mTaskState;
}
