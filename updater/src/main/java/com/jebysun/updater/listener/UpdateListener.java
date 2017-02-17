package com.jebysun.updater.listener;

import com.jebysun.updater.model.AppUpdateInfo;

/**
 * 更新监听接口
 * @author JebySun
 *
 */
public interface UpdateListener {
	void onFoundNewVersion(AppUpdateInfo updateInfo);
	void onNoFoundNewVersion();
	void onCheckError(String errorMsg);

	void onDownloading(Integer... values);
	void onDownloadFinish();
	void onDownloadError(String errorMsg);
}
