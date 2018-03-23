package com.cxample.download_demo;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cxample.download_demo.download.DownloadConstant;
import com.cxample.download_demo.download.DownloadManager;
import com.cxample.download_demo.download.DownloadTask;
import com.cxample.download_demo.download.db.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView mAddBtn;
    private RecyclerView mDownloadList;
    private DownloadAdapter mAdapter;

    private int mSelectedPosition;

    private ProgressDialog mProgressDialog;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1) {
                getDownloadInfo();
            } else if(msg.what == 2) {
                if(mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(MainActivity.this);
                    mProgressDialog.setCancelable(false);
                }
                String message = String.valueOf(msg.obj);
                mProgressDialog.setMessage(message);
                if(!mProgressDialog.isShowing()) {
                    mProgressDialog.show();
                }
            } else if(msg.what == 3) {
                if(mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                }
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(DownloadConstant.DOWNLOAD_DELETE_COMPLETE.equals(action)) {
                hideDialog();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(mReceiver,new IntentFilter(DownloadConstant.DOWNLOAD_DELETE_COMPLETE));

        mAddBtn = findViewById(R.id.add_download);
        mDownloadList = findViewById(R.id.download_list);

        mAdapter = new DownloadAdapter();
        mDownloadList.setLayoutManager(new LinearLayoutManager(this));
        mDownloadList.setAdapter(mAdapter);


        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        showDialog();
                        for(int i = 0; i < 100; i++) {
                            DownloadInfo downloadInfo = new DownloadInfo();
                            downloadInfo.mTaskId = ++DownloadManager.MAX_ID;
                            downloadInfo.mTaskTotalSize = 100;
                            downloadInfo.mTaskFinishSize = 0;
                            downloadInfo.mTaskState = DownloadTask.TASK_STATE_WAITING;
                            DownloadManager.addDownload(MainActivity.this, downloadInfo);
                        }
                        hideDialog();
                    }
                }).start();
            }
        });
        findViewById(R.id.pause_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<DownloadInfo> infos = DownloadManager.getAllDownloadInfo();
                ArrayList<Integer> ids = new ArrayList<>();
                for(DownloadInfo info : infos) {
                    if(info.mTaskState == DownloadTask.TASK_STATE_RUNNING
                            || info.mTaskState == DownloadTask.TASK_STATE_WAITING) {
                        ids.add(info.mTaskId);
                    }
                }
                DownloadManager.pauseAllDownload(MainActivity.this, ids);
            }
        });

        findViewById(R.id.start_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<DownloadInfo> infos = DownloadManager.getAllDownloadInfo();
                ArrayList<Integer> ids = new ArrayList<>();
                for(DownloadInfo info : infos) {
                    if(info.mTaskState == DownloadTask.TASK_STATE_PAUSE
                            || info.mTaskState == DownloadTask.TASK_STATE_ERROR) {
                        ids.add(info.mTaskId);
                    }
                }
                DownloadManager.startAllDownload(MainActivity.this, ids);
            }
        });

        findViewById(R.id.delete_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
                List<DownloadInfo> infos = DownloadManager.getAllDownloadInfo();
                ArrayList<Integer> ids = new ArrayList<>();
                for(DownloadInfo info : infos) {
                    ids.add(info.mTaskId);
                }
                DownloadManager.deleteAllDownload(MainActivity.this, ids);
            }
        });
    }

    private void showDialog() {
        Message message = Message.obtain();
        message.what = 2;
        message.obj = "添加下载";
        mHandler.sendMessage(message);
        mHandler.removeMessages(1);
    }

    private void hideDialog() {
        Message message = Message.obtain();
        message.what = 3;
        mHandler.sendMessage(message);
        mHandler.sendEmptyMessageDelayed(1, 1000);
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

    private View.OnCreateContextMenuListener mItemClickListener = new View.OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            Object object = v.getTag();
            if(object != null) {
                mSelectedPosition = (int)object;
                getMenuInflater().inflate(R.menu.download_item_click_menu, menu);
            }
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.start: {
                startDownload();
                return true;
            }
            case R.id.pause: {
                pauseDownload();
                return true;
            }
            case R.id.delete: {
                deleteDownload();
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    private void startDownload() {
        DownloadInfo downloadInfo = mAdapter.getItemData(mSelectedPosition);
        if(downloadInfo != null && (downloadInfo.mTaskState == DownloadTask.TASK_STATE_PAUSE
                || downloadInfo.mTaskState == DownloadTask.TASK_STATE_ERROR)) {
            DownloadManager.addDownload(this, downloadInfo);
        }
    }

    private void pauseDownload() {
        DownloadInfo downloadInfo = mAdapter.getItemData(mSelectedPosition);
        if(downloadInfo != null && downloadInfo.mTaskState == DownloadTask.TASK_STATE_RUNNING) {
            DownloadManager.pauseDownload(this, downloadInfo);
        }
    }

    private void deleteDownload() {
        DownloadInfo downloadInfo = mAdapter.getItemData(mSelectedPosition);
        if(downloadInfo != null) {
            DownloadManager.deleteDownload(this, downloadInfo);
        }
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

        public DownloadInfo getItemData(int position) {
            if(mDownloadInfos != null && position >= 0 && position < mDownloadInfos.size()) {
                return mDownloadInfos.get(position);
            }
            return null;
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

            holder.itemView.setTag(position);
            holder.itemView.setOnCreateContextMenuListener(mItemClickListener);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.showContextMenu();
                }
            });
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

    private static final long MAX_TIME = 3000;
    private long mCurrTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            long time = System.currentTimeMillis();
            if(time - mCurrTime > MAX_TIME) {
                Toast.makeText(this, "再次点击退出应用", Toast.LENGTH_SHORT).show();
                mCurrTime = time;
            } else {
                DownloadApplication.exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
