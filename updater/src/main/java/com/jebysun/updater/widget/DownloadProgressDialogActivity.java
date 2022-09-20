package com.jebysun.updater.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.jebysun.updater.R;
import com.jebysun.updater.utils.AndroidUtil;
import com.jebysun.updater.utils.JavaUtil;


public class DownloadProgressDialogActivity extends AppCompatActivity implements View.OnClickListener {

    private static final float WIDTH_PERCENT = 0.8F;
    public static final int BTN_OK = 1;
    public static final int BTN_CANCEL = 2;

    private ProgressBar mProgressBar;
    private TextView mTvProgressMsg;
    private TextView mTvProgressPercent;

    private String mFormat;
    private float mTaskTotal;
    private float mTaskFinished;

    private static Builder mBuilder;
    private LocalBroadcastManager localBroadcastMgr;
    private DownloadBroadcastReceiver broadcastReceiver;



    // Activity必须保留无参构造
    public DownloadProgressDialogActivity() {}

    public DownloadProgressDialogActivity(Builder builder) {
        mBuilder = builder;
    }

    public void show() {
        Intent dialogIntent = new Intent(mBuilder.context, DownloadProgressDialogActivity.class);
        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mBuilder.context.startActivity(dialogIntent);
    }

    public void dismiss() {
        finish();
    }

    public static void sendDismissBroadcast() {
        Intent intent = new Intent(DownloadBroadcastReceiver.ACTION_BROADCAST_PROGRESS);
        intent.putExtra("type", 4);
        LocalBroadcastManager.getInstance(mBuilder.context).sendBroadcast(intent);
    }

    //设置总文件大小
    public static void setMax(float max) {
        Intent intent = new Intent(DownloadBroadcastReceiver.ACTION_BROADCAST_PROGRESS);
        intent.putExtra("type", 1);
        intent.putExtra("max", max);
        LocalBroadcastManager.getInstance(mBuilder.context).sendBroadcast(intent);
    }

    /**
     * 文字显示格式：已下载和需下载总大小
     * @param format
     */
    public static void setProgressNumberFormat(String format) {
        Intent intent = new Intent(DownloadBroadcastReceiver.ACTION_BROADCAST_PROGRESS);
        intent.putExtra("type", 2);
        intent.putExtra("format", format);
        LocalBroadcastManager.getInstance(mBuilder.context).sendBroadcast(intent);
    }

    /**
     * 设置进度
     * @param progress
     */
    public static void sendProgressBroadcast(float progress) {
        Intent intent = new Intent(DownloadBroadcastReceiver.ACTION_BROADCAST_PROGRESS);
        intent.putExtra("type", 3);
        intent.putExtra("progress", progress);
        LocalBroadcastManager.getInstance(mBuilder.context).sendBroadcast(intent);
    }




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dialog_progress);

        this.resizeDialog();
        this.initView();

        // 注册本地广播
        localBroadcastMgr = LocalBroadcastManager.getInstance(this);
        broadcastReceiver = new DownloadBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(DownloadBroadcastReceiver.ACTION_BROADCAST_PROGRESS);
        localBroadcastMgr.registerReceiver(broadcastReceiver, intentFilter);

    }

    @Override
    public void onBackPressed() {
        if (mBuilder.cancelable) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        localBroadcastMgr.unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btn_ok) {
            if (mBuilder.clickListener != null) {
                mBuilder.clickListener.clicked(this, BTN_OK);
            }
        } else if (viewId == R.id.btn_cancel) {
            dismiss();
            if (mBuilder.clickListener != null) {
                mBuilder.clickListener.clicked(this, BTN_CANCEL);
            }
        }
    }

    private void resizeDialog() {
        Window window = this.getWindow();
        int width = (int) (AndroidUtil.getScreenWidth(window.getContext()) * WIDTH_PERCENT);
        window.setGravity(Gravity.CENTER);
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(R.drawable.drawable_dialog_bg);

        setFinishOnTouchOutside(mBuilder.canceledOnTouchOutside);
    }

    private void initView() {
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvMessage = findViewById(R.id.tv_msg);
        mTvProgressMsg = findViewById(R.id.tv_progress_msg);
        mProgressBar = findViewById(R.id.progress_download);
        mTvProgressPercent = findViewById(R.id.tv_progress_percent);

        Button btnOK = findViewById(R.id.btn_ok);
        Button btnCancel = findViewById(R.id.btn_cancel);

        tvTitle.setText(mBuilder.title != null ? mBuilder.title : tvTitle.getText());
        tvMessage.setText(mBuilder.message != null ? mBuilder.message : tvMessage.getText());
        mProgressBar.setMax(100);
        btnOK.setText(mBuilder.strBtnOK != null ? mBuilder.strBtnOK : btnOK.getText());
        btnCancel.setText(mBuilder.strBtnCancel != null ? mBuilder.strBtnCancel : btnCancel.getText());

        btnCancel.setVisibility(mBuilder.negativeButtonGone ? View.GONE : View.VISIBLE);

        btnOK.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    /**
     * 设置进度
     * @param progress
     */
    private void setProgress(float progress) {
        if (this.mTaskTotal == 0F) {
            indeterminateProgress(progress);
            return;
        }

        this.mTaskFinished = progress;
        //更新进度
        mProgressBar.setProgress((int) (mProgressBar.getMax() * progress/mTaskTotal));
        //更新完成百分数
        mTvProgressPercent.setText((int)(100 * progress/mTaskTotal) + "%");
        //更新已下载多少兆字节
        mTvProgressMsg.setText(mFormat.replace("%1f", JavaUtil.formatFloat2String(mTaskFinished, 2)).replace("%2f", JavaUtil.formatFloat2String(mTaskTotal, 2)));
    }

    /**
     * 未知大小文件下载进度无法知道
     * @param progress
     */
    private void indeterminateProgress(float progress) {
        this.mTaskFinished = progress;
        mProgressBar.setIndeterminate(true);
        //更新进度
        mProgressBar.setProgress(0);
        //更新完成百分数
        mTvProgressPercent.setVisibility(View.INVISIBLE);
        //更新已下载多少兆字节
        mTvProgressMsg.setText(mFormat.replace("%1f",JavaUtil.formatFloat2String(mTaskFinished, 2)));
    }


    /////////////////////////////////////////////////////////////////

    public static class Builder {

        private Context context;

        private String title;
        private String message;
        private int contentGravity = Gravity.CENTER;
        private String strBtnOK;
        private String strBtnCancel;
        private boolean cancelable = true;
        private boolean canceledOnTouchOutside = true;
        private boolean negativeButtonGone;

        private OnClickButtonListener clickListener;


        public Builder(Context context) {
            this.context = context;
        }

        public DownloadProgressDialogActivity build() {
            return new DownloadProgressDialogActivity(this);
        }

        public void show() {
            build().show();
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setPositiveButtonText(String str) {
            this.strBtnOK = str;
            return this;
        }

        public Builder setNegativeButtonText(String str) {
            this.strBtnCancel = str;
            return this;
        }

        public Builder setOnButtonClickListener(OnClickButtonListener listener) {
            this.clickListener = listener;
            return this;
        }

        public Builder setCancelable(boolean b) {
            this.cancelable = b;
            return this;
        }

        public Builder setCanceledOnTouchOutside(boolean b) {
            this.canceledOnTouchOutside = b;
            return this;
        }
        public void setNegativeButtonGone(boolean required) {
            this.negativeButtonGone = required;
        }
    }

    /////////////////////////////////////////////////////////////////

    public class DownloadBroadcastReceiver extends BroadcastReceiver {

        public static final String ACTION_BROADCAST_PROGRESS = "action_broadcast_progress";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(ACTION_BROADCAST_PROGRESS)) {
                return;
            }

            int type = intent.getIntExtra("type", 0);
            if (type == 1) {
                mTaskTotal = intent.getFloatExtra("max", 0F);
                return;
            }
            if (type == 2) {
                mFormat = intent.getStringExtra("format");
                return;
            }
            if (type == 3) {
                float progress = intent.getFloatExtra("progress", 0F);
                setProgress(progress);
                return;
            }
            if (type == 4) {
                dismiss();
                return;
            }
        }
    }


    /////////////////////////////////////////////////////////////////

    public interface OnClickButtonListener {
        void clicked(DownloadProgressDialogActivity dialog, int which);
    }

}
