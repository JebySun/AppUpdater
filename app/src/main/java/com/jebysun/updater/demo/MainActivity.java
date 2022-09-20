package com.jebysun.updater.demo;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jebysun.updater.AppUpdater;
import com.jebysun.updater.listener.UpdateCheckCallback;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String UPDATE_URL = "https://gitee.com/jebysun/website/raw/master/github/app_latest_version.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 进入即检查更新
        checkPermissionUpdate();

        // 手动更新，通常用于App关于页面检查新版本。
        Button btnCheckUpdate = this.findViewById(R.id.btn_check_update);
        btnCheckUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNewVersion();
            }
        });

    }

    /**
     * 申请权限然后检查新版本
     * Android 6.0以上申请文件读写权限
     */
    private void checkPermissionUpdate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionX.init(this)
                    .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .request(new RequestCallback() {
                        @Override
                        public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                            if (allGranted) {
                                // 检查更新
                                AppUpdater.with(MainActivity.this)
                                        .setHostUpdateCheckUrl(UPDATE_URL)
                                        .check();
                            }
                        }
                    });
        } else {
            // 检查更新
            AppUpdater.with(MainActivity.this)
                    .setHostUpdateCheckUrl(UPDATE_URL)
                    .check();
        }

    }

    /**
     * 手动检查更新
     */
    private void checkNewVersion() {
        AppUpdater.with(this)
                // 手动强制检查更新
                .setForceMode(true)
                .setHostUpdateCheckUrl(UPDATE_URL)
                // 检查结果回调
                .setUpdateCheckCallback(new UpdateCheckCallback() {
                    @Override
                    public void onSuccess(boolean hasNew) {
                        if (!hasNew) {
                            Toast.makeText(MainActivity.this, "你已经安装最新版本", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String msg) {
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                })
                .check();
    }

    public void toNextActivity(View view) {
        startActivity(new Intent(this, SecondActivity.class));
    }
}
