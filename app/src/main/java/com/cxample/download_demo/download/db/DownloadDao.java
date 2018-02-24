package com.cxample.download_demo.download.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by yanqing on 2018/2/24.
 */
@Dao
public interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DownloadInfo downloadInfo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<DownloadInfo> downloadInfos);

    @Query("SELECT * FROM download_info")
    List<DownloadInfo> getAll();

    @Delete
    void delete(DownloadInfo downloadInfo);

    @Delete
    void delete(List<DownloadInfo> downloadInfos);
}
