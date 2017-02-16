package com.jebysun.appupdater.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.jebysun.appupdater.R;
import com.jebysun.appupdater.utils.AndroidUtil;

/**
 * Created by Administrator on 2017/2/15.
 */

public class CheckedDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final int BTN_OK = 1;
    public static final int BTN_CANCEL = 2;

    private Dialog mDialog;
    private Window mWindow;

    private View mRootView;
    private TextView mTvTitle;
    private TextView mTvMsg;
    private Button mBtnOk;
    private Button mBtnCancel;

    private OnClickBtnListener mClickListener;

    private int mMsgViewGravity = Gravity.CENTER;
    private boolean mCanceledOnTouchOutside = true;

    private String mTxtTitle;
    private String mTxtMsg;
    private String mTxtBtnOk;
    private String mTxtBtnCancel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        init();
        mRootView = inflater.inflate(R.layout.fragment_dialog_checkedmsg, container);
        initView();
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWindow.setLayout((int)(AndroidUtil.getScreenWidth(getActivity()) * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
    }

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
        mDialog.setCanceledOnTouchOutside(mCanceledOnTouchOutside);
    }

    private void initView() {
        mTvTitle = (TextView) mRootView.findViewById(R.id.tv_title);
        mTvMsg = (TextView) mRootView.findViewById(R.id.tv_msg);
        mBtnOk = (Button) mRootView.findViewById(R.id.btn_ok);
        mBtnCancel = (Button) mRootView.findViewById(R.id.btn_cancel);

        mTvMsg.setGravity(mMsgViewGravity);

        mTvTitle.setText(mTvTitle!=null ? mTxtTitle : mTvTitle.getText());
        mTvMsg.setText(mTxtMsg!=null ? mTxtMsg : mTvMsg.getText());
        mBtnOk.setText(mTxtBtnOk!=null ? mTxtBtnOk : mBtnOk.getText());
        mBtnCancel.setText(mTxtBtnCancel!=null ? mTxtBtnCancel : mBtnCancel.getText());

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

    public void setMessageGravity(int gravity) {
        this.mMsgViewGravity = gravity;
    }

    public void setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
        mCanceledOnTouchOutside = canceledOnTouchOutside;
    }

    public void setOnButtonClickListener(OnClickBtnListener listener) {
        this.mClickListener = listener;
    }







    ///////////////////////////////////////////////

    public interface OnClickBtnListener {
        void clicked(CheckedDialogFragment dialog, int which);
    }


}
