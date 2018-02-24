package com.cxample.download_demo;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cxample.download_demo.download.DownloadManager;
import com.cxample.download_demo.download.DownloadTask;
import com.cxample.download_demo.download.db.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView mAddBtn;
    private RecyclerView mDownloadList;
    private DownloadAdapter mAdapter;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            getDownloadInfo();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAddBtn = findViewById(R.id.add_download);
        mDownloadList = findViewById(R.id.download_list);

        mAdapter = new DownloadAdapter();
        mDownloadList.setLayoutManager(new LinearLayoutManager(this));
        mDownloadList.setAdapter(mAdapter);


        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadInfo downloadInfo = new DownloadInfo();
                downloadInfo.mTaskId = ++DownloadManager.MAX_ID;
                downloadInfo.mTaskTotalSize = 100;
                downloadInfo.mTaskFinishSize = 0;
                downloadInfo.mTaskState = DownloadTask.TASK_STATE_WAITING;
                DownloadManager.addDownload(MainActivity.this, downloadInfo);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getDownloadInfo();
    }

    public void getDownloadInfo() {
        List<DownloadInfo> downloadInfos = DownloadManager.getAllDownloadInfo();
        mAdapter.setDownloadInfos(downloadInfos);
        mHandler.removeMessages(1);
        mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    private class DownloadViewHolder extends RecyclerView.ViewHolder {

        public DownloadViewHolder(View itemView) {
            super(itemView);
        }


    }

    private class DownloadAdapter extends RecyclerView.Adapter<DownloadViewHolder> {
        private List<DownloadInfo> mDownloadInfos;

        public void setDownloadInfos(List<DownloadInfo> downloadInfos) {
            mDownloadInfos = downloadInfos;
            notifyDataSetChanged();
        }

        @Override
        public DownloadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.download_list_item_layout, null);
            return new DownloadViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DownloadViewHolder holder, int position) {
            TextView title = holder.itemView.findViewById(R.id.download_title);
            TextView hint = holder.itemView.findViewById(R.id.download_hint);
            ProgressBar progressBar = holder.itemView.findViewById(R.id.download_progress);
            DownloadInfo downloadInfo = mDownloadInfos.get(position);

            int progress = (int)(downloadInfo.mTaskFinishSize / (float)downloadInfo.mTaskTotalSize * 100);
            title.setText("任务：" + downloadInfo.mTaskId);
            progressBar.setProgress(progress);
            showDownloadState(hint, downloadInfo.mTaskState, progress);
        }

        @Override
        public int getItemCount() {
            return mDownloadInfos == null ? 0 : mDownloadInfos.size();
        }

        private void showDownloadState(TextView view, int state, int progress) {
            switch(state) {
                case DownloadTask.TASK_STATE_RUNNING: {
                    view.setText("进度：" + progress + "%");
                    break;
                }
                case DownloadTask.TASK_STATE_ERROR: {
                    view.setText("下载出错");
                    break;
                }
                case DownloadTask.TASK_STATE_FINISH: {
                    view.setText("已完成");
                    break;
                }
                case DownloadTask.TASK_STATE_WAITING: {
                    view.setText("等待中");
                    break;
                }
                case DownloadTask.TASK_STATE_PAUSE: {
                    view.setText("已暂停");
                    break;
                }
            }
        }
    }
}
