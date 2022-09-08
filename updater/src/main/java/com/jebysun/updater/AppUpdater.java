package com.jebysun.updater;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.jebysun.updater.listener.UpdateCheckCallback;
import com.jebysun.updater.listener.UpdateListener;
import com.jebysun.updater.model.UpdateModel;
import com.jebysun.updater.service.UpdateService;
import com.jebysun.updater.utils.AndroidUtil;
import com.jebysun.updater.utils.JavaUtil;
import com.jebysun.updater.widget.CheckedDialogFragment;
import com.jebysun.updater.widget.ProgressDialogFragment;

import java.io.File;
import java.util.List;

/**
 * App实现自动更新
 * TODO：
 * 1.当检查到有新版时，检查最新版apk文件是否已经下载，已下载则直接提示安装。
 * 2.检查当前网络环境，如果是WIFI才检查更新。
 * 3.忽略新版本提示过后，一定时间内不再提示更新。
 * @author JebySun
 *
 */
public class AppUpdater {

	public static final int DOWNLOAD_NOTIFY_ID = 1;
	public static final String NOTIFICATION_CHANNEL_ID = "notification_channel_download";

	private String hostUpdateCheckUrl;
	private String downloadFileName;

	private Context context;
	private ServiceConnection serviceConn;
	private UpdateService updateService;
	private ProgressDialogFragment progressDialog;
	private NotificationCompat.Builder notifyBuilder;
	private RemoteViews customViews;
	private CheckedDialogFragment updateDialog;

	private UpdateCheckCallback updateCheckCallback;

	private boolean forceCheckMode;
	private long fileSize;
	private boolean downloadInBack;
	private float fCount = 1F; //转换单位量
	private String format;     //下载进度信息格式
	private int iconResId;
	private String appName;
	private String downloadPath;


	private AppUpdater(Context context) {
		this.context = context;
		this.iconResId = context.getApplicationInfo().icon;
		this.appName = AndroidUtil.getApplicationName(context);
		this.downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
		this.downloadFileName = appName;
	}

	/**
	 * 需要应用上下文参数，注意：传入的Context必须是Acitivity的实例。
	 * @param context
	 * @return
     */
	public static AppUpdater with(Context context) {
		if (!(context instanceof Activity)) {
			throw new RuntimeException("Ensure parameter \"context\" is instance of Activity");
		}
		return new AppUpdater(context);
	}

	/**
	 * 开始检查
	 */
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
	                		progressDialog = setProgressTxtFormat(progressDialog, fileSize);
	                	} else if (downloadInBack) { //后台下载，只更新通知中的进度条
							// 文件大小为0时
							if (fileSize != 0) {
								int size = values[1];
								size = (int) (size * 100.0F / fileSize);
								customViews.setProgressBar(R.id.notify_progress, 100, size, false);
								customViews.setTextViewText(R.id.notify_progress_percent, 100 * size / 100 + "%");
								customViews.setTextViewText(R.id.notify_progress_size, format.replace("%1f", JavaUtil.formatFloat2String(values[1]/fCount, 2)).replace("%2f", JavaUtil.formatFloat2String(fileSize/fCount, 2)));
							} else {
								customViews.setProgressBar(R.id.notify_progress, 100, 0, true);
								customViews.setTextViewText(R.id.notify_progress_percent, "");
								customViews.setTextViewText(R.id.notify_progress_size, format.replace("%1f", JavaUtil.formatFloat2String(values[1]/fCount, 2)));
							}
							notifyMgr.notify(DOWNLOAD_NOTIFY_ID, notifyBuilder.build());
	                	} else {
							// 文件大小为0时
							if (fileSize == 0) {
								progressDialog = setProgressTxtFormat(progressDialog, values[1]);
							}
	                		//设置进度条对话框进度
	                		setProgressValue(progressDialog, values[1]/fCount);
	                	}
	                }

					@Override
					public void onFoundNewVersion(UpdateModel appInfo) {
						if (updateCheckCallback != null) {
							updateCheckCallback.onSuccess(true);
						}
						downloadFileName = downloadFileName + "_v" + appInfo.getVersionName()+ ".apk";

						final String downloadUrl = appInfo.getApkUrl();
						StringBuilder releaseNoteBuild = new StringBuilder();
						List<String> releaseNoteList = appInfo.getReleaseNoteList();
						for (int i=0; i<releaseNoteList.size(); i++) {
							releaseNoteBuild.append(i+1).append(". ").append(releaseNoteList.get(i)).append("\n");
						}
						String updateMsg = releaseNoteBuild.substring(0, releaseNoteBuild.length() - 1);

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
										doDownload(updateDialog, downloadUrl);
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
						if (forceCheckMode && updateCheckCallback!=null) {
							updateCheckCallback.onSuccess(false);
							forceCheckMode = false;
						}
						release();
					}

					@Override
					public void onDownloadFinish() {
						progressDialog.dismissAllowingStateLoss();
						notifyMgr.cancel(DOWNLOAD_NOTIFY_ID);
						// 安装
						AndroidUtil.installApk(context, downloadPath + File.separator + getDownloadFileName());
						release();
					}

					@Override
					public void onDownloadCanceled() {
						progressDialog.dismissAllowingStateLoss();
						notifyMgr.cancel(DOWNLOAD_NOTIFY_ID);
						release();
					}

					@Override
					public void onDownloadFailed(String errorMsg) {
						progressDialog.dismissAllowingStateLoss();
						notifyMgr.cancel(DOWNLOAD_NOTIFY_ID);
						AndroidUtil.toast(context, "下载失败，请重试！");
						release();
					}

					@Override
					public void onCheckError(String errorMsg) {
						if (updateCheckCallback != null) {
							updateCheckCallback.onError(errorMsg);
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

	/**
	 * Android 6.0以上检测文件写权限，然后下载
	 * @param dialog
	 * @param downloadUrl
	 */
	private void doDownload(DialogFragment dialog, String downloadUrl) {
		dialog.dismiss();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int allowed = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			if (allowed != PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(context, "请先获取文件读写权限", Toast.LENGTH_LONG).show();
				return;
			}
		}
		fileDownload(downloadUrl);
	}



	private void fileDownload(String downloadUrl) {
		progressDialog = new ProgressDialogFragment();
		progressDialog.setTitle("新版本下载");
		progressDialog.setMessage("正在下载新版本，请稍后...");
		progressDialog.setPositiveButton("后台下载");
		progressDialog.setNegativeButton("取消下载");
		progressDialog.setCancelable(false);
		progressDialog.setOnButtonClickListener(new ProgressDialogFragment.OnClickBtnListener() {
			@Override
			public void clicked(ProgressDialogFragment dialog, int which) {
				switch (which) {
					case CheckedDialogFragment.BTN_OK:
						dialog.dismiss();
						downloadInNotification();
						break;
					case CheckedDialogFragment.BTN_CANCEL:
						//下载过程中取消下载
						release();
						break;
				}
			}
		});
		progressDialog.setCancelable(false);
		progressDialog.show(((Activity)context).getFragmentManager(), "ProgressDialogFragment");

		//调用服务的文件下载方法
		updateService.startDownLoadTask(downloadUrl, this.downloadPath, this.downloadFileName);
	}

	/**
	 * 释放Service
	 */
	private void release() {
		if (context != null) {
			context.unbindService(serviceConn);
		}
		updateService = null;
		serviceConn = null;
		notifyBuilder = null;
		customViews = null;
		context = null;
	}

    private ProgressDialogFragment setProgressTxtFormat(ProgressDialogFragment pd, long pSize) {
    	float size = pSize;
    	String[] f = {"B", "KB", "MB", "GB"};
    	int formatCount = 0;
    	while(size>=1024) {
    		size /= 1024;
    		formatCount ++;
    	}
    	//统计转换单位量
    	fCount = (float) Math.pow(1024, formatCount);
    	if (fileSize != 0) {
			format = "%1f" + f[formatCount] + " / %2f" + f[formatCount];
			pd.setMax(size);
		} else {
			format = "%1f" + f[formatCount] + " / 未知大小";
		}
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
	 * 后台下载
	 */
	private void downloadInNotification() {
		NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// 高版本需要通知渠道
		if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			// 只在Android O之上需要渠道
			NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"文件下载", NotificationManager.IMPORTANCE_LOW);
			// 如果这里用IMPORTANCE_NOENE就需要在系统的设置里面开启渠道，通知才能正常弹出
			notifyMgr.createNotificationChannel(notificationChannel);
		}

		notifyBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
		notifyBuilder.setSmallIcon(iconResId);
		//使用系统自带下载动态图标
		notifyBuilder.setSmallIcon(iconResId);
		notifyBuilder.setTicker(appName + "已转入后台下载");
		notifyBuilder.setAutoCancel(true);
		notifyBuilder.setOngoing(true);
		notifyBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
		customViews = new RemoteViews(context.getPackageName(), R.layout.layout_notification_download);
		customViews.setImageViewResource(R.id.notify_icon, iconResId);
		customViews.setTextViewText(R.id.notify_title, appName + "正在下载");
		//当前下载进度设置
		//============start=================
		customViews.setTextViewText(R.id.notify_progress_percent, "0%");
		customViews.setTextViewText(R.id.notify_progress_size, "0.0KB/0.0M");
		customViews.setProgressBar(R.id.notify_progress, 100, 0, false);
		//============end=================
		notifyBuilder.setContent(customViews);



		notifyMgr.notify(DOWNLOAD_NOTIFY_ID, notifyBuilder.build());
		downloadInBack = true;
	}


	/**
	 * 设置检查更新信息服务地址，必须设置。
	 * @param hostUpdateCheckUrl
	 * @return
     */
	public AppUpdater setHostUpdateCheckUrl(String hostUpdateCheckUrl) {
		this.hostUpdateCheckUrl = hostUpdateCheckUrl;
		return this;
	}

	/**
	 * 设置下载路径，可选设置项，默认下载到拓展存储卡根目录下的Download内。
	 * @param downloadPath
	 * @return
     */
	public AppUpdater setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
		return this;
	}

	public String getDownloadPath() {
		return downloadPath;
	}

	/**
	 * 设置下载文件名称(不需要指定文件拓展名)，可选设置项，默认名称格式为：[应用名称]_v[应用版本名称].apk。
	 * @param downloadFileName
	 * @return
     */
	public AppUpdater setDownloadFileName(String downloadFileName) {
		this.downloadFileName = downloadFileName;
		return this;
	}

	public String getDownloadFileName() {
		return downloadFileName;
	}

	/**
	 * 设置检查更新结果通知，手动检查更新时可使用，以便应用给出检查结果提示。
	 * @param callback
	 * @return
     */
	public AppUpdater setUpdateCheckCallback(UpdateCheckCallback callback) {
		this.updateCheckCallback = callback;
		return this;
	}

	/**
	 * 强制检查一次更新，不管之前的忽略更新标识。
	 * @param isForceModel
	 * @return
     */
	public AppUpdater setForceMode(boolean isForceModel) {
		forceCheckMode = isForceModel;
		return this;
	}



}



