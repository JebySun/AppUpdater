package com.jebysun.updater.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jebysun.updater.R;
import com.jebysun.updater.utils.AndroidUtil;


public class CheckResultDialogActivity extends AppCompatActivity implements View.OnClickListener {

    private static final float WIDTH_PERCENT = 0.8F;
    public static final int BTN_OK = 1;
    public static final int BTN_CANCEL = 2;

    // TODO 用成员属性替换静态builder
    private static Builder mBuilder;
    // 是否弹框内view互动事件导致的弹框消失
    private boolean mCanceledWithEvent;


    // Activity必须保留无参构造
    public CheckResultDialogActivity() {}

    public CheckResultDialogActivity(Builder builder) {
        mBuilder = builder;
    }

    public void show() {
        Intent dialogIntent = new Intent(mBuilder.context, CheckResultDialogActivity.class);
        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mBuilder.context.startActivity(dialogIntent);
    }

    public void dismiss() {
        finish();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_dialog_checkedmsg);

        this.resizeDialog();
        this.initView();
    }

    @Override
    public void onBackPressed() {
        if (mBuilder.cancelable) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBuilder.clickListener != null && !mCanceledWithEvent) {
            mBuilder.clickListener.onCanceled();
        }
    }

    @Override
    public void onClick(View view) {
        mCanceledWithEvent = true;
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
        TextView tvContent = findViewById(R.id.tv_msg);
        Button btnOK = findViewById(R.id.btn_ok);
        Button btnCancel = findViewById(R.id.btn_cancel);

        tvContent.setGravity(mBuilder.contentGravity);

        tvTitle.setText(mBuilder.title != null ? mBuilder.title : tvTitle.getText());
        tvContent.setText(mBuilder.content != null ? mBuilder.content : tvContent.getText());

        // 更新描述换行缩进
        Paint tvPaint = tvContent.getPaint();
        float rawIndentWidth = tvPaint.measureText("1. ");
        SpannableString spannableString = new SpannableString(mBuilder.content != null ? mBuilder.content : "");
        LeadingMarginSpan.Standard marginSpan = new LeadingMarginSpan.Standard(0, (int) rawIndentWidth);
        spannableString.setSpan(marginSpan, 0, spannableString.length(), SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
        tvContent.setText(spannableString);


        btnOK.setText(mBuilder.strBtnOK != null ? mBuilder.strBtnOK : btnOK.getText());
        btnCancel.setText(mBuilder.strBtnCancel != null ? mBuilder.strBtnCancel : btnCancel.getText());

        btnOK.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }


    /////////////////////////////////////////////////////////////////////////

    public static class Builder {

        private Context context;

        private String title;
        private String content;
        private int contentGravity = Gravity.CENTER;
        private String strBtnOK;
        private String strBtnCancel;
        private boolean cancelable = true;
        private boolean canceledOnTouchOutside = true;

        private OnClickButtonListener clickListener;


        public Builder(Context context) {
            this.context = context;
        }

        public CheckResultDialogActivity build() {
            return new CheckResultDialogActivity(this);
        }

        public void show() {
            build().show();
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }


        public Builder setContentGravity(int gravity) {
            this.contentGravity = gravity;
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
    }


    /////////////////////////////////////////////////////////////////////

    public interface OnClickButtonListener {
        void clicked(CheckResultDialogActivity dialog, int which);
        void onCanceled();
    }


}
