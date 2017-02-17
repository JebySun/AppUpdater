package com.jebysun.updater.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jebysun.updater.AppUpdater;
import com.jebysun.updater.listener.OnUpdateCheckResultListener;

import java.io.File;

public class MainActivity extends Activity {

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 自动检查更新
        AppUpdater.with(this)
                // 设置通知栏图标
                .setIconResId(R.mipmap.ic_launcher)
                // 设置检查新版本URL，得到json格式数据
                .setHostUpdateCheckUrl("http://files.cnblogs.com/files/jebysun/app_version.js")
                // 设置下载路径
                .setDownloadFilePath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download")
                // 设置下载文件名
                .setDownloadFileName("WACA_update")
                .check();


        Button btnCheckUpdate = (Button) this.findViewById(R.id.btn_check_update);
        btnCheckUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNewVersion();
            }
        });


    }

    /**
     * 手动检查更新
     */
    private void checkNewVersion() {
        // 提示用户正在检查更新
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.show();
        AppUpdater.with(this)
                // 手动强制检查更新
                .setForceMode(true)
                .setIconResId(R.mipmap.ic_launcher)
                .setHostUpdateCheckUrl("http://files.cnblogs.com/files/jebysun/app_version.js")
                .setDownloadFilePath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download")
                .setDownloadFileName("WACA_update")
                // 检查结果回调
                .setOnUpdateCheckListener(new OnUpdateCheckResultListener() {
                    @Override
                    public void onSuccess(boolean hasNew) {
                        // 关闭提示
                        mProgressDialog.dismiss();
                        if (!hasNew) {
                            Toast.makeText(MainActivity.this, "你已经安装最新版本", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String msg) {
                        // 关闭提示
                        mProgressDialog.dismiss();
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                })
                .check();
    }

}
