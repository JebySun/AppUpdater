package com.jebysun.updater.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.jebysun.updater.AppUpdater.*;
import com.jebysun.updater.listener.UpdateListener;
import com.jebysun.updater.model.UpdateModel;
import com.jebysun.updater.task.CheckUpdateAsyncTask;
import com.jebysun.updater.task.DownloadAsyncTask;
import com.jebysun.updater.utils.AndroidUtil;
import com.jebysun.updater.utils.JavaUtil;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查更新服务
 * @author JebySun
 * @date 2022/09/10
 */
public class UpdateService extends Service {

	public static final String EXTRA_KEY_HOST_URL = "host_url";

	private AsyncTask<String, Integer, String> downloadTask;
	private UpdateListener updateListener;
	private long lastUpdateTime;
	private String hostUpdateCheckUrl;

	private boolean serviceStarted;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Service已在运行
		if (this.serviceStarted) {
			return super.onStartCommand(intent, flags, startId);
		}

		ServiceBridge.initUpdateService(this);

		this.hostUpdateCheckUrl = intent.getStringExtra(EXTRA_KEY_HOST_URL);
		// 启动检查更新异步任务
		new CheckUpdateAsyncTask(this).execute(this.hostUpdateCheckUrl);

		this.serviceStarted = true;

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		this.serviceStarted = false;
		if (downloadTask != null) {
			downloadTask.cancel(true);
		}
		super.onDestroy();
	}




	/**
	 * 启动下载任务
	 * @param fileUrl
	 * @param downloadPath
	 * @param downloadFileName
	 */
	public void startDownLoadTask(String fileUrl, String downloadPath, String downloadFileName) {
		downloadTask = new DownloadAsyncTask(this).execute(fileUrl, downloadPath, downloadFileName);
	}

	public void cancelDownload() {
		if (downloadTask != null) {
			downloadTask.cancel(true);
		}
	}
	
    
    /**
     * 注册检查更新回调接口
     */
    public void setUpdateListener(UpdateListener updateListener) {  
    	this.updateListener = updateListener;
    }

    public void checkUpdateResult(String hostVersionInfo) {
		if (JavaUtil.isEmptyString(hostVersionInfo) || hostVersionInfo.equals("timeout")) {
			this.updateListener.onCheckError("check update error");
			return;
		}

		UpdateModel appUpdateModel = parseJson(hostVersionInfo);
		if (AndroidUtil.getAppVersionCode(this) < appUpdateModel.getVersionCode()) {
			this.updateListener.onFoundNewVersion(appUpdateModel);
			return;
		}

		this.updateListener.onNoFoundNewVersion();
    }
    
    /**
     * 更新ProgressDialog或Notification进度
     * @param values
     */
    public void updateProgress(Integer... values) {
    	if(values[0] == -100) {
    		updateListener.onDownloadFinish();
    	} else if (values[0] == -1) {
    		updateListener.onDownloadFailed("download error");
        } else if(values[0] == -2) {
			updateListener.onDownloadCanceled();
		} else if (System.currentTimeMillis() - lastUpdateTime > 100) {
    		updateListener.onDownloading(values);
    		lastUpdateTime = System.currentTimeMillis();
    	}
    }
    

	private UpdateModel parseJson(String jsonStr) {
		UpdateModel appInfo = new UpdateModel();
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			appInfo.setVersionCode(jsonObj.getInt("versionCode"));
			appInfo.setVersionName(jsonObj.getString("versionName"));
			appInfo.setFileSize(jsonObj.getString("fileSize"));
			appInfo.setApkUrl(jsonObj.getString("apkUrl"));
			appInfo.setRequired(jsonObj.getBoolean("required"));
			appInfo.setReleaseDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jsonObj.getString("releaseDate")));
			List<String> relaseNoteList = new ArrayList<>();
			JSONArray jsonArr = jsonObj.getJSONArray("releaseNotes");
			for (int i=0; i<jsonArr.length(); i++) {
				relaseNoteList.add(jsonArr.getString(i));
			}
			appInfo.setReleaseNoteList(relaseNoteList);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return appInfo;
	}
    

}



