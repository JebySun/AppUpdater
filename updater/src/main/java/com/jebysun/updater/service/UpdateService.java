package com.jebysun.updater.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import com.jebysun.updater.listener.UpdateListener;
import com.jebysun.updater.model.UpdateModel;
import com.jebysun.updater.task.CheckUpdateAsyncTask;
import com.jebysun.updater.task.DownloadAsyncTask;
import com.jebysun.updater.utils.AndroidUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动更新服务
 * @author JebySun
 *
 */
public class UpdateService extends Service {
	private AsyncTask<String, Integer, String> fileDownlaodTask;
	private UpdateListener updateListener;
	private long lastUpdateTime;
	private String hostUpdateCheckUrl;
	
	@Override
	public IBinder onBind(Intent intent) {
		//启动检查更新异步任务
		this.hostUpdateCheckUrl = intent.getStringExtra("update_check_url");
		new CheckUpdateAsyncTask(this).execute(this.hostUpdateCheckUrl);
		return new MsgBinder();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (fileDownlaodTask != null) {
			fileDownlaodTask.cancel(true);
		}

	}
	
	public void startDownLoadTask(String fileUrl, String downloadPath, String downloadFileName) {
		//启动下载任务
		fileDownlaodTask = new DownloadAsyncTask(this).execute(fileUrl, downloadPath, downloadFileName);
	}
	
    
    /**
     * 注册检查更新回调接口
     */
    public void setUpdateListener(UpdateListener updateListener) {  
    	this.updateListener = updateListener;
    }
    
    public void checkUpdateResult(String hostVersionInfo) {
		if (hostVersionInfo != null && hostVersionInfo.length() != 0 && !hostVersionInfo.equals("timeout")) {
			UpdateModel appUpdateInfo = parseJson(hostVersionInfo);
			if (AndroidUtil.getAppVersionCode(this) < appUpdateInfo.getVersionCode()) {
				this.updateListener.onFoundNewVersion(appUpdateInfo);
			} else {
				this.updateListener.onNoFoundNewVersion();
			}
		} else {
			this.updateListener.onCheckError("check update error");
		}

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
    

	public UpdateModel parseJson(String jsonStr) {
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
    
    
    
    
    
    
    
	public class MsgBinder extends Binder{  
        /** 
         * 获取当前Service的实例 
         * @return 
         */  
        public UpdateService getService(){  
            return UpdateService.this;  
        }  
    }
    

}







