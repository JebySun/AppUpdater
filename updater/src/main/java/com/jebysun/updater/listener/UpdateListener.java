package com.jebysun.updater.listener;

import com.jebysun.updater.model.UpdateModel;

/**
 * 更新监听接口
 * @author JebySun
 *
 */
public interface UpdateListener {
	void onFoundNewVersion(UpdateModel updateInfo);
	void onNoFoundNewVersion();
	void onCheckError(String errorMsg);

	void onDownloading(Integer... values);
	void onDownloadFinish();
	void onDownloadCanceled();
	void onDownloadFailed(String errorMsg);
}
