package com.jebysun.updater.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jebysun.updater.R;
import com.jebysun.updater.utils.AndroidUtil;
import com.jebysun.updater.utils.JavaUtil;

/**
 * Created by Administrator on 2017/2/15.
 */

public class ProgressDialogFragment extends DialogFragment implements View.OnClickListener {

    private static final float WIDTH_PERCENT = 0.8F;

    public static final int BTN_OK = 1;
    public static final int BTN_CANCEL = 2;

    private TextView mTvTitle;
    private ProgressBar mProgressBar;
    private TextView mTvProgressMsg;
    private TextView mTvProgressPercent;
    private TextView mTvMsg;
    private Button mBtnOk;
    private Button mBtnCancel;

    private OnClickBtnListener mClickListener;

    private String mTxtTitle;
    private String mTxtMsg;
    private String mTxtBtnOk;
    private String mTxtBtnCancel;

    private String mFormat;
    private float mTaskTotal;
    private float mTaskFinished;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_progress, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.UpdaterDialog);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        initDialog(getDialog());
    }

    public void initDialog(Dialog dialog) {
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        int width = (int) (AndroidUtil.getScreenWidth(window.getContext()) * WIDTH_PERCENT);
        window.setGravity(Gravity.CENTER);
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(R.drawable.drawable_dialog_bg);
    }

    //TODO 下载过程中取消下载
    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mClickListener.clicked(this, BTN_CANCEL);
    }

    @Override
    public void onClick(View view) {
        if (view == mBtnOk) {
            if (mClickListener != null) {
                mClickListener.clicked(this, BTN_OK);
            }
        } else if (view == mBtnCancel) {
            this.dismiss();
            if (mClickListener != null) {
                mClickListener.clicked(this, BTN_CANCEL);
            }
        }
    }


    private void initView(View view) {
        mTvTitle = view.findViewById(R.id.tv_title);
        mProgressBar = view.findViewById(R.id.progress_download);
        mTvProgressMsg = view.findViewById(R.id.tv_progress_msg);
        mTvProgressPercent = view.findViewById(R.id.tv_progress_percent);
        mTvMsg = view.findViewById(R.id.tv_msg);
        mBtnOk = view.findViewById(R.id.btn_ok);
        mBtnCancel = view.findViewById(R.id.btn_cancel);

        mTvTitle.setText(mTvTitle!=null ? mTxtTitle : mTvTitle.getText());
        mTvMsg.setText(mTxtMsg!=null ? mTxtMsg : mTvMsg.getText());
        mBtnOk.setText(mTxtBtnOk!=null ? mTxtBtnOk : mBtnOk.getText());
        mBtnCancel.setText(mTxtBtnCancel!=null ? mTxtBtnCancel : mBtnCancel.getText());
        mProgressBar.setMax(100);

        mBtnOk.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
    }


    public void setTitle(String title) {
        mTxtTitle = title;
    }

    public void setMessage(String msg) {
        mTxtMsg = msg;
    }

    public void setPositiveButton(String txt) {
        mTxtBtnOk = txt;
    }

    public void setNegativeButton(String txt) {
        mTxtBtnCancel = txt;
    }

    public void setOnButtonClickListener(OnClickBtnListener listener) {
        this.mClickListener = listener;
    }

    public void setMax(float max) {
        //设置总文件大小
        this.mTaskTotal = max;
    }

    public void setProgressNumberFormat(String format) {
        this.mFormat = format;
    }

    public void setProgress(float progress) {
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

    public Button getButtonOk() {
        return mBtnOk;
    }



    ///////////////////////////////////////////////

    public interface OnClickBtnListener {
        void clicked(ProgressDialogFragment dialog, int which);
    }


}
