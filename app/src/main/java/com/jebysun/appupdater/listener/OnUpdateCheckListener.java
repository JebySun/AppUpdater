package com.jebysun.appupdater.listener;

/**
 * 更新监听接口
 * @author JebySun
 *
 */
public interface OnUpdateCheckListener {
	
	void onSuccess(boolean hasNew);
	void onError(String msg);

}
