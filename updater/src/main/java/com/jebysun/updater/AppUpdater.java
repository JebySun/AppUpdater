package com.jebysun.updater;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.widget.RemoteViews;

import com.jebysun.updater.listener.OnUpdateCheckResultListener;
import com.jebysun.updater.listener.UpdateListener;
import com.jebysun.updater.model.AppUpdateInfo;
import com.jebysun.updater.service.UpdateService;
import com.jebysun.updater.utils.AndroidUtil;
import com.jebysun.updater.utils.JavaUtil;
import com.jebysun.updater.widget.CheckedDialogFragment;
import com.jebysun.updater.widget.ProgressDialogFragment;

import java.io.File;
import java.util.List;
import java.util.Map;


/**
 * App实现自动更新
 * TODO：
 * 1.当检查到有新版时，检查最新版apk文件是否已经下载，已下载则直接提示安装。
 * 2.检查当前网络环境，如果是WIFI才检查更新。
 * @author JebySun
 *
 */
public class AppUpdater {

	public static final int DOWNLOAD_NOTIFY_ID = 1;

	private String hostUpdateCheckUrl;
	private String downloadFilePath;
	private String downloadFileName;

	private Context context;
	private ServiceConnection serviceConn;
	private UpdateService updateService;
	private ProgressDialogFragment progressDialog;
	private NotificationCompat.Builder notifyBuilder;
	private RemoteViews customViews;
	private CheckedDialogFragment updateDialog;

	private OnUpdateCheckResultListener updateCheckListener;

	private int iconResId;
	private boolean forceChecked;
	private long fileSize;
	private boolean downloadInBack;
	private float fCount = 1f; //转换单位量
	private String format;     //进度格式



	private AppUpdater(Context context) {
		this.context = context;
	}

	public static AppUpdater with(Context context) {
		if (!(context instanceof Activity)) {
			throw new RuntimeException("ensure parameter \"context\" is instance of Activity");
		}
		return new AppUpdater(context);
	}

	public void check() {
		if (serviceConn != null) {
			context.unbindService(serviceConn);
		}

		serviceConn = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {

				final NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				//返回一个MsgService对象
				updateService = ((UpdateService.MsgBinder)service).getService();
	        	//注册回调接口来接收下载进度的变化
				updateService.setUpdateListener(new UpdateListener() {

	                @Override
	                public void onDownloading(Integer... values) {
	                	if (values[1] == 0) {
	                		//设置进度条对话框的最大值
	                		fileSize = values[0];
	                		progressDialog = setProgressTxtFormat(progressDialog);
	                	} else if (downloadInBack) { //后台下载，只更新通知中的进度条
							int size = values[1];
							size = (int) (size * 100.0F / fileSize);
							customViews.setProgressBar(R.id.notify_progress, 100, size, false);
							customViews.setTextViewText(R.id.notify_progress_percent, 100 * size / 100 + "%");
							customViews.setTextViewText(R.id.notify_progress_size, format.replace("%1f", JavaUtil.formatFloat2String(values[1]/fCount, 2)).replace("%2f", JavaUtil.formatFloat2String(fileSize/fCount, 2)));
							notifyMgr.notify(DOWNLOAD_NOTIFY_ID, notifyBuilder.build());
	                	} else {
	                		//设置进度条对话框进度
	                		setProgressValue(progressDialog, values[1]/fCount);
	                	}
	                }

					@Override
					public void onFoundNewVersion(AppUpdateInfo appInfo) {
						if (updateCheckListener != null) {
							updateCheckListener.onSuccess(true);
						}
						setDownloadFileName(getDownloadFileName() + "_v" + appInfo.getVersionName()+ ".apk");

						final String downloadUrl = appInfo.getApkUrl();
						StringBuilder updateMsgBuldr = new StringBuilder();
						List<String> updateMsgList =appInfo.getUpdateMsgList();
						for (int i=0; i<updateMsgList.size(); i++) {
							updateMsgBuldr.append(i+1).append(".").append(updateMsgList.get(i)).append("\n");
						}
						String updateMsg = (String) updateMsgBuldr.subSequence(0, updateMsgBuldr.length()-1);

						updateDialog = new CheckedDialogFragment();
						updateDialog.setTitle("检测到新版本");
						updateDialog.setMessage(updateMsg);
						updateDialog.setMessageGravity(Gravity.LEFT);
						updateDialog.setPositiveButton("立即更新");
						updateDialog.setNegativeButton("暂不更新");
						updateDialog.setOnButtonClickListener(new CheckedDialogFragment.OnClickBtnListener() {
							@Override
							public void clicked(CheckedDialogFragment dialog, int which) {
								switch (which) {
									case CheckedDialogFragment.BTN_OK:
										dialog.dismiss();
										fileDownload(downloadUrl);
										break;
									case CheckedDialogFragment.BTN_CANCEL:
										release();
										break;
								}
							}
						});
						updateDialog.setCanceledOnTouchOutside(false);
						updateDialog.show(((Activity)context).getFragmentManager(), "CheckedDialogFragment");
					}

					@Override
					public void onNoFoundNewVersion() {
						if (forceChecked && updateCheckListener!=null) {
							updateCheckListener.onSuccess(false);
							forceChecked = false;
						}
						release();
					}

					@Override
					public void onDownloadFinish() {
						progressDialog.dismiss();
						notifyMgr.cancel(DOWNLOAD_NOTIFY_ID);
						//安装
						AndroidUtil.installApk(context, getDownloadFilePath() + File.separator + getDownloadFileName());
						release();
					}

					@Override
					public void onDownloadError(String errorMsg) {
						AndroidUtil.toast(context, "下载出错，请重试！");
						release();
					}

					@Override
					public void onCheckError(String errorMsg) {
						if (updateCheckListener != null) {
							updateCheckListener.onError(errorMsg);
						}
						release();
					}
				});
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				release();
			}
		};

		Intent updateIntent = new Intent(context, UpdateService.class);
		updateIntent.putExtra("update_check_url", hostUpdateCheckUrl);
		context.bindService(updateIntent, serviceConn, Context.BIND_AUTO_CREATE);

	}




	private void fileDownload(String downloadUrl) {
		progressDialog = new ProgressDialogFragment();
		progressDialog.setTitle("新版本下载");
		progressDialog.setMessage("正在下载新版本，请稍后...");
		progressDialog.setPositiveButton("后台下载");
		progressDialog.setNegativeButton("取消下载");
		progressDialog.setOnButtonClickListener(new ProgressDialogFragment.OnClickBtnListener() {
			@Override
			public void clicked(ProgressDialogFragment dialog, int which) {
				switch (which) {
					case CheckedDialogFragment.BTN_OK:
						dialog.dismiss();
						downloadInNotification();
						break;
					case CheckedDialogFragment.BTN_CANCEL:
						//TODO 下载过程中取消下载
						release();
						break;
				}
			}
		});
		progressDialog.setCancelable(false);
		progressDialog.show(((Activity)context).getFragmentManager(), "ProgressDialogFragment");

		//调用服务的文件下载方法
		updateService.startDownLoadTask(downloadUrl, getDownloadFilePath(), getDownloadFileName());
	}


	/**
	 * 释放
	 */
	private void release() {
		context.unbindService(serviceConn);
		updateService = null;
		serviceConn = null;
		notifyBuilder = null;
		customViews = null;
		context = null;
	}


    private ProgressDialogFragment setProgressTxtFormat(ProgressDialogFragment pd) {
    	float size = fileSize;
    	String[] f = {"B", "KB", "MB", "GB"};
    	int formatCount = 0;
    	while(size>=1024) {
    		size /= 1024;
    		formatCount ++;
    	}
    	//统计转换单位量
    	fCount = (float) Math.pow(1024, formatCount);
    	format = "%1f" + f[formatCount] + " / %2f" + f[formatCount];
    	pd.setMax(size);
    	pd.setProgressNumberFormat(format);
    	return pd;
    }

    /**
     * 更新完成进度
     * @param progressDialog
     * @param progress - 当前已完成进度
     */
    private void setProgressValue(ProgressDialogFragment progressDialog, float progress) {
		progressDialog.setProgress(progress);
    }


    /**
     * 获取新版本更新详情
     * @param map
     * @return
     */
	@SuppressWarnings("unchecked")
	private String getUpdateMessage(Map<String, Object> map) {
		StringBuffer updateInfo = new StringBuffer("最新版本：");
		updateInfo.append(map.get("versionName"));
		updateInfo.append("    本机版本：");
		updateInfo.append(map.get("localVerName"));
		updateInfo.append("\n文件大小：");
		updateInfo.append(map.get("fileSize"));
		updateInfo.append("\n发布日期：");
		updateInfo.append(map.get("date"));
		updateInfo.append("\n\n更新内容：");
		StringBuffer updateItem = new StringBuffer(updateInfo);
		List<String> list = (List<String>) map.get("updateContent");
		for (String string : list) {
			updateItem.append("\n");
			updateItem.append(string);
		}
		return updateItem.toString();
	}

	/**
	 * 后台下载
	 */
	private void downloadInNotification() {
		//TODO 应用名称
		String appName = AndroidUtil.getApplicationName(context);

		notifyBuilder = new NotificationCompat.Builder(context);
		// TODO 系统自带下载动态图标
		notifyBuilder.setSmallIcon(iconResId);
		notifyBuilder.setTicker("["+appName+"]" + "已转入后台下载");
		notifyBuilder.setAutoCancel(true);
		notifyBuilder.setOngoing(true);
		notifyBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
		customViews = new RemoteViews(context.getPackageName(), R.layout.layout_notification_download);
		customViews.setImageViewResource(R.id.notify_icon, iconResId);
		customViews.setTextViewText(R.id.notify_title, "["+appName+"]" + "下载中");
		//当前下载进度设置
		//============start=================
		customViews.setTextViewText(R.id.notify_progress_percent, "0%");
		customViews.setTextViewText(R.id.notify_progress_size, "0.0KB/0.0M");
		customViews.setProgressBar(R.id.notify_progress, 100, 0, false);
		//============end=================
		notifyBuilder.setContent(customViews);

		NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifyMgr.notify(DOWNLOAD_NOTIFY_ID, notifyBuilder.build());
		downloadInBack = true;
	}



	public AppUpdater setIconResId(int resId) {
		iconResId = resId;
		return this;
	}

	public AppUpdater setHostUpdateCheckUrl(String hostUpdateCheckUrl) {
		this.hostUpdateCheckUrl = hostUpdateCheckUrl;
		return this;
	}

	public String getDownloadFilePath() {
		return downloadFilePath;
	}

	public AppUpdater setDownloadFilePath(String downloadFilePath) {
		this.downloadFilePath = downloadFilePath;
		return this;
	}

	public String getDownloadFileName() {
		return downloadFileName;
	}

	public AppUpdater setDownloadFileName(String downloadFileName) {
		this.downloadFileName = downloadFileName;
		return this;
	}


	public AppUpdater setOnUpdateCheckListener(OnUpdateCheckResultListener listener) {
		this.updateCheckListener = listener;
		return this;
	}

	public AppUpdater setForceMode(boolean isForceModel) {
		forceChecked = isForceModel;
		return this;
	}



}



