# AppUpdater
  Android应用几乎都少不了新版本检查，每次都写一次也很麻烦，也考虑到这个功能的必要性和通用性，于是封装独立成一个库。使用这个库，一句代码实现Android应用检查更新。

## 一、功能特点
- 使用简单，只需一句代码即可；
- 简单轻量，不依赖第三方库；
- 不需要服务端写接口（只需要在服务端放一个固定格式的json）；
- 兼容最低到android 4.0，可后台下载apk，在通知栏显示下载进度；
- 不依赖引用Activity的context, 使用Application的context，避免内存泄露。

## 二、效果截图
<img src="https://gitee.com/jebysun/website/raw/master/github/screenshot_1.png" width="33%"/><img src="https://gitee.com/jebysun/website/raw/master/github/screenshot_2.png" width="33%"/><img src="https://gitee.com/jebysun/website/raw/master/github/screenshot_3.png" width="33%"/>

## 三、如何使用
#### 1. 首先，添加jitpack仓库支持和gradle依赖：  
修改项目根目录下的build.gradle，增加项目jitpack仓库支持：
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
修改项目模块目录下的build.gradle，添加gradle依赖：
```gradle
dependencies {
	...
	implementation 'com.github.JebySun:AppUpdater:1.1.2'
}
```
#### 2. 然后，在Activity或者Application中加入一句代码即可：
```java
AppUpdater.with(this)
	.setHostUpdateCheckUrl("https://gitee.com/jebysun/website/raw/master/github/app_latest_version.json")
	.check();
```
#### 3. 最后，你只需要在服务端放一个json规范格式的文件，该文件的url地址就是AppUpdater.setHostUpdateCheckUrl()方法的参数，json示例：
```json
{
	"versionCode":10,
	"versionName":"1.0.0",
	"fileSize":"79.15MB",
	"apkUrl":"http://www.lofter.com/rsc/android/lofter.apk",
	"required":false,
	"releaseDate":"2017-02-12 12:45:20",
	"releaseNotes":["新版本特性新版本特性新版本特性，非常推荐下载体验；", "描述版本信息，方便用户选择是否立即下载更新；", "性能优化和BUG修复。"]
}
```
> **JSON字段说明**

|	字段		|	数据类型		|	含义		|	说明		|
|--	|--	|--	|--	|
|	versionCode		|	number	|	最新版本号	    |	大于客户端versionCode则提示新版本	|
|	versionName		|	string	|	最新版本名称		|			|
|	fileSize		|	string	|	apk文件大小	    |			|
|	apkUrl		    |	string	|	apk下载地址		|			|
|	required		|	boolean	|	是否必须更新		|	如果需要强制客户端升级，设为true，弹框则无法取消		|
|	releaseDate		|	string	|	发布日期			|			|
|	releaseNotes	|	array	|	更新描述			|	更新描述的字符串数组		|

做完以上工作之后，运行吧！  

另外，如果需要手动触发检查更新，可以这样写：
```java
/**
 * 手动触发检查更新
 */
private void checkNewVersion() {
	AppUpdater.with(this)
			// 手动强制检查更新
			.setForceMode(true)
			.setHostUpdateCheckUrl("https://gitee.com/jebysun/website/raw/master/github/app_latest_version.json")
			// 检查结果回调
			.setUpdateCheckCallback(new UpdateCheckCallback() {
				@Override
				public void onSuccess(boolean hasNew) {
					if (!hasNew) {
						Toast.makeText(MainActivity.this, "你已经安装最新版本", Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void onFailure(String msg) {
					Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
				}
			})
			.check();
}
```
## PS
- 检查版本前，注意需要先请求本地文件读写权限。
- 如果按照以上说明你没成功，检查请求的json数据格式，参考项目app模块的使用示例。

## License
Copyright 2017 JebySun  

Licensed under the Apache License, Version 2.0 (the "License");  
you may not use this file except in compliance with the License.  
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,  
software distributed under the License is distributed on an "AS IS" BASIS,  
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
See the License for the specific language governing permissions and  
limitations under the License.

