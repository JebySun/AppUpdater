# AppUpdater
  由于各大应用市场相继拒绝上线集成友盟等第三方自动更新功能的应用，友盟已放弃维护和支持自动更新功能模块。但这个功能对很多App还是很重要的，考虑到这个功能的必要性和通用性，于是这个项目就这么诞生了。使用这个项目，你可以一句代码实现Android应用自身检查更新。

## 功能特点
* 使用简单，只需一句代码即可；
* 不依赖第三方库；
* 界面美观；

## 如何使用
  目前需要把项目下载到本地导入updater模块，并在你的项目主模块添加依赖updater模块。然后，在合适的位置（通常是Activity的onCreate方法内）加入以下一句代码即可：
```java
// 自动检查更新
AppUpdater.with(this)
		// 设置通知栏图标
		.setIconResId(R.mipmap.ic_launcher)
		// 设置检查新版本URL，得到json格式数据
		.setHostUpdateCheckUrl("http://files.cnblogs.com/files/jebysun/app_version_default.js")
		// 设置下载路径
		.setDownloadFilePath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download")
		// 设置下载文件名
		.setDownloadFileName("WACA_update")
		.check();
```

如果需要手动检查更新，应该这样写：
```java
Button btnCheckUpdate = (Button) this.findViewById(R.id.btn_check_update);
btnCheckUpdate.setOnClickListener(new View.OnClickListener() {
	@Override
	public void onClick(View view) {
		checkNewVersion();
	}
});

/**
 * 手动检查更新
 */
private void checkNewVersion() {
	// 提示用户正在检查更新
	mProgressDialog = new ProgressDialog(this);
	mProgressDialog.show();
	AppUpdater.with(this)
			// 手动强制检查更新
			.setForceMode(true)
			.setIconResId(R.mipmap.ic_launcher)
			.setHostUpdateCheckUrl("http://files.cnblogs.com/files/jebysun/app_version_default.js")
			.setDownloadFilePath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download")
			.setDownloadFileName("WACA_update")
			// 检查结果回调
			.setOnUpdateCheckResultListener(new OnUpdateCheckResultListener() {
				@Override
				public void onSuccess(boolean hasNew) {
					// 关闭提示
					mProgressDialog.dismiss();
					if (!hasNew) {
						Toast.makeText(MainActivity.this, "你已经安装最新版本", Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void onError(String msg) {
					// 关闭提示
					mProgressDialog.dismiss();
					Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
				}
			})
			.check();
}
```

哦，对了，别忘记在服务器放一个json格式的数据文件，该文件内容如下：
```javascript
{
	"versionCode":10,
	"versionName":"1.0.0",
	"fileSize":"6.2M",
	"apkUrl":"http://58.216.107.44/imtt.dd.qq.com/16891/36C5694F6FE468D788FFFC65166547BE.apk?mkey=58a403869c7c4c41&f=858&c=0&fsname=com.qiyi.video_8.1_80830.apk&csr=4d5s&p=.apk",
	"required":false,
	"releaseDate":"2017-02-12 12:45:20",
	"releaseNotes":["1.新版本特性新版本特性新版本特性。", "2.描述版本信息，方便用户选择是否便用户选择是否下载。", "3.性能优化和BUG修复。"]
}
```

如果按照以上说明你没成功，请参考项目中app模块的使用示例吧。

## 反馈联系
* jebysun(a)126.com
