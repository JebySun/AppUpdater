package com.jebysun.appupdater;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jebysun.appupdater.listener.OnUpdateCheckListener;

import java.io.File;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCheckUpdate = (Button) this.findViewById(R.id.btn_check_update);
        btnCheckUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNewVersion();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 检查更新
        AppUpdater.with(this)
                .setHostUpdateCheckUrl("http://files.cnblogs.com/files/jebysun/app_version_default.js")
                .setDownloadFilePath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download")
                .setDownloadFileName("WACA_update")
                .check();
    }


    private void checkNewVersion() {
        //TODO 提示用户正在检查更新

        AppUpdater.with(this)
                .setHostUpdateCheckUrl("http://files.cnblogs.com/files/jebysun/app_version_default.js")
                .setDownloadFilePath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download")
                .setDownloadFileName("WACA_update")
                .setOnUpdateCheckListener(new OnUpdateCheckListener() {
                    @Override
                    public void onSuccess(boolean hasNew) {
                        //TODO 关闭提示
                        if (!hasNew) {
                            Toast.makeText(MainActivity.this, "你已经安装最新版本", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String msg) {
                        //TODO 关闭提示

                    }
                })
                .check();
    }

}
