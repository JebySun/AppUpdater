package com.jebysun.updater.listener;

/**
 * 更新监听接口
 * @author JebySun
 *
 */
public interface OnUpdateCheckResultListener {
	
	void onSuccess(boolean hasNew);
	void onError(String msg);

}
