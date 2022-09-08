package com.jebysun.updater.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jebysun.updater.R;
import com.jebysun.updater.utils.AndroidUtil;

/**
 * Created by Administrator on 2017/2/15.
 */

public class CheckedDialogFragment extends DialogFragment implements View.OnClickListener {

    private static final float WIDTH_PERCENT = 0.8F;

    public static final int BTN_OK = 1;
    public static final int BTN_CANCEL = 2;


    private TextView mTvTitle;
    private TextView mTvMsg;
    private Button mBtnOk;
    private Button mBtnCancel;

    private OnClickBtnListener mClickListener;

    private int mMsgViewGravity = Gravity.CENTER;
    private boolean mCancelable = true;
    private boolean mCanceledOnTouchOutside = true;

    private String mTxtTitle;
    private String mTxtMsg;
    private String mTxtBtnOk;
    private String mTxtBtnCancel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_checkedmsg, container, false);
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

    private void initView(View view) {
        mTvTitle = view.findViewById(R.id.tv_title);
        mTvMsg = view.findViewById(R.id.tv_msg);
        mBtnOk = view.findViewById(R.id.btn_ok);
        mBtnCancel = view.findViewById(R.id.btn_cancel);

        mTvMsg.setGravity(mMsgViewGravity);

        mTvTitle.setText(mTvTitle != null ? mTxtTitle : mTvTitle.getText());

        // 更新描述换行缩进
        Paint tvPaint = mTvMsg.getPaint();
        float rawIndentWidth = tvPaint.measureText("1. ");
        SpannableString spannableString = new SpannableString(mTxtMsg != null ? mTxtMsg : mTvMsg.getText());
        LeadingMarginSpan.Standard marginSpan = new LeadingMarginSpan.Standard(0, (int) rawIndentWidth);
        spannableString.setSpan(marginSpan, 0, spannableString.length(), SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
        mTvMsg.setText(spannableString);


        mBtnOk.setText(mTxtBtnOk != null ? mTxtBtnOk : mBtnOk.getText());
        mBtnCancel.setText(mTxtBtnCancel != null ? mTxtBtnCancel : mBtnCancel.getText());

        mBtnOk.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
    }

    public void initDialog(Dialog dialog) {
        dialog.setCancelable(mCancelable);
        dialog.setCanceledOnTouchOutside(mCanceledOnTouchOutside);
        Window window = dialog.getWindow();
        int width = (int) (AndroidUtil.getScreenWidth(window.getContext()) * WIDTH_PERCENT);
        window.setGravity(Gravity.CENTER);
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(R.drawable.drawable_dialog_bg);
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
            this.dismiss();
            if (mClickListener != null) {
                mClickListener.clicked(this, BTN_CANCEL);
            }
        }
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
