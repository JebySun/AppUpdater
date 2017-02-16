package com.jebysun.appupdater.listener;

import com.jebysun.appupdater.model.AppUpdateInfo;

/**
 * 更新监听接口
 * @author JebySun
 *
 */
public interface UpdateListener {
	
	void onDownloading(Integer... values);
	
	void checkUpdate(AppUpdateInfo appUpdateInfo);

	void onNotFound();
	
	void downloadFinish();
	
	void downloadError();

}
