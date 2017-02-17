package com.jebysun.updater.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jebysun.updater.R;
import com.jebysun.updater.utils.AndroidUtil;
import com.jebysun.updater.utils.JavaUtil;

/**
 * Created by Administrator on 2017/2/15.
 */

public class ProgressDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final int BTN_OK = 1;
    public static final int BTN_CANCEL = 2;

    private Dialog mDialog;
    private Window mWindow;

    private View mRootView;
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
        init();
        mRootView = inflater.inflate(R.layout.fragment_dialog_progress, container);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWindow.setLayout((int)(AndroidUtil.getScreenWidth(getActivity()) * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        initView();
    }

    @Override
    public void onPause() {
        this.dismiss();
        super.onPause();
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
            mDialog.dismiss();
            if (mClickListener != null) {
                mClickListener.clicked(this, BTN_CANCEL);
            }
        }
    }

    private void init() {
        mDialog = getDialog();
        mWindow = mDialog.getWindow();
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setCanceledOnTouchOutside(true);
    }

    private void initView() {
        mTvTitle = (TextView) mRootView.findViewById(R.id.tv_title);
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.progress_download);
        mTvProgressMsg = (TextView) mRootView.findViewById(R.id.tv_progress_msg);
        mTvProgressPercent = (TextView) mRootView.findViewById(R.id.tv_progress_percent);
        mTvMsg = (TextView) mRootView.findViewById(R.id.tv_msg);
        mBtnOk = (Button) mRootView.findViewById(R.id.btn_ok);
        mBtnCancel = (Button) mRootView.findViewById(R.id.btn_cancel);

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
        this.mTaskFinished = progress;
        //更新进度
        mProgressBar.setProgress((int) (mProgressBar.getMax() * progress/mTaskTotal));
        //更新完成百分数
        mTvProgressPercent.setText((int)(100 * progress/mTaskTotal) + "%");
        //更新已下载多少兆字节
        mTvProgressMsg.setText(mFormat.replace("%1f", JavaUtil.formatFloat2String(mTaskFinished, 2)).replace("%2f", JavaUtil.formatFloat2String(mTaskTotal, 2)));
    }




    ///////////////////////////////////////////////

    public interface OnClickBtnListener {
        void clicked(ProgressDialogFragment dialog, int which);
    }


}
