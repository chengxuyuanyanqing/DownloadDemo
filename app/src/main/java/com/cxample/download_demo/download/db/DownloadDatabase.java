package com.cxample.download_demo.download.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by yanqing on 2018/2/24.
 */

@Database(entities = {DownloadInfo.class}, exportSchema = false, version = 1)
public abstract class DownloadDatabase extends RoomDatabase {
    private static final String DB_NAME = "download.db";

    private static final Object mLock = new Object();

    private static DownloadDatabase mDatabaseInstance;

    public abstract DownloadDao downloadDao();


    public static DownloadDatabase getInstance(Context context) {
        if(mDatabaseInstance == null) {
            synchronized(mLock) {
                if(mDatabaseInstance == null) {
                    mDatabaseInstance = Room
                            .databaseBuilder(context, DownloadDatabase.class, DB_NAME)
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return mDatabaseInstance;
    }
}
