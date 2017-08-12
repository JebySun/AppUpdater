package com.jebysun.updater.task;

import android.os.AsyncTask;
import android.util.Log;

import com.jebysun.updater.service.UpdateService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class DownloadAsyncTask extends AsyncTask<String, Integer, String> {

	// 下载缓冲区大小(KB)
	public static final int BUF_SIZE_KB = 32;

	private UpdateService service;
	private String downloadUrl;
	private String downloadPath;
	private String downloadFileName;
	
	public DownloadAsyncTask(UpdateService service) {
		this.service = service;
	}

	/**
	 * 该方法运行在UI线程当中,可以对UI空间进行设置  
	 */
    @Override  
    protected void onPreExecute() {
    	
    }  

    /**  
     * 这里的String参数对应AsyncTask中的第一个参数   
     * 这里的String返回值对应AsyncTask的第三个参数  
     * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI进行设置和修改  
     */ 
	@Override
	protected String doInBackground(String... params) {
        //文件保存路径创建
		downloadUrl = params[0];
		downloadPath = params[1];
		downloadFileName = params[2];
		File downloadPathFile = new File(downloadPath);
		if (!downloadPathFile.exists()) {
			downloadPathFile.mkdirs();
		}
		return downloadFile(downloadUrl, new File(downloadPathFile, downloadFileName));
	}

	/**
	 * 文件下载
	 * @param downloadUrl
	 * @param file
     */
	private String downloadFile(String downloadUrl, File file) {
		File tempFile = null;
		InputStream is = null;
		FileOutputStream fos = null;
		int byteBufSize = BUF_SIZE_KB * 1024;
		try {
			//支持下载连接类型：
//			downloadUrl = "http://files.cnblogs.com/files/jebysun/app-release.apk"; //OK
            downloadUrl = "https://gitee.com/jebysun/PublicResource/raw/master/waca_release.apk"; //NO, 无法获取文件总大小。
//            downloadUrl = "http://www.cr173.com/down.asp?id=3188"; //OK
//            downloadUrl = "http://files.cnblogs.com/files/jebysun/豌豆荚.apk"; //OK
//            downloadUrl = "http://files.cnblogs.com/files/jebysun/%E8%B1%8C%E8%B1%86%E8%8D%9A.apk"; //OK
//            downloadUrl = "http://files.cnblogs.com/files/jebysun/%25E8%25B1%258C%25E8%25B1%2586%25E8%258D%259A.apk"; //OK
//            downloadUrl = "http://imtt.dd.qq.com/16891/D2233EF6C81785F5C12CC61CC4DC0566.apk?fsname=com.yueren.pyyx_2.1.8_20181.apk&csr=1bbd";


			//先解码，是预防URL已经编码，两次解码是预防要下载的文件使用中文URL编码为文件名。
			downloadUrl = URLDecoder.decode(downloadUrl, "utf-8");
			downloadUrl = URLDecoder.decode(downloadUrl, "utf-8");
			//url编码兼容处理
			downloadUrl = URLEncoder.encode(downloadUrl, "utf-8").replaceAll("\\+", "%20");
			downloadUrl = downloadUrl.replaceAll("%3A", ":").replaceAll("%2F", "/").replaceAll("%3F", "?").replaceAll("%3D", "=").replaceAll("%26", "&");
			tempFile = new File(file.getAbsolutePath() + ".temp");
			tempFile.createNewFile();

			URL url = new URL(downloadUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept-Encoding", "identity");
			conn.setRequestProperty("Accept-Charset", "UTF-8");
			conn.setRequestProperty("ContentType", "UTF-8");
			conn.setConnectTimeout(5000);
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				int length = conn.getContentLength();
				//无法获取文件大小时，默认为0。
				length = length == -1 ? 0 : length;
				this.publishProgress(length, 0);
				is = conn.getInputStream();
				fos = new FileOutputStream(tempFile);
				byte[] buffer = new byte[byteBufSize];
				int readLength = 0;
				int finishedCount = 0;
				while ((readLength = is.read(buffer)) != -1) {
					fos.write(buffer, 0, readLength);
					finishedCount = finishedCount + readLength;
					this.publishProgress(length, finishedCount);
				}
				fos.flush();
				fos.close();
				is.close();
			} else {
				return "error";
			}
			tempFile.renameTo(file);
			return "finished";
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (fos != null) fos.close();
				if (is != null) is.close();
			} catch (IOException ie) {
				ie.printStackTrace();
				return "error";
			}
			if (tempFile.exists()) {
				tempFile.delete();
			}
			return "error";
		}
	}


    /**  
     * 这里的Intege参数对应AsyncTask中的第二个参数 
     * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作  
     */  
    @Override  
    protected void onProgressUpdate(Integer... values) {
    	super.onProgressUpdate(values);
    	//更新Progress进度
    	this.service.updateProgress(values);
    }  

    
    /**  
     * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）  
     * 在doInBackground方法执行结束之后在运行
     * 该方法运行在UI线程当中可以对UI空间进行设置  
     */
    @Override  
    protected void onPostExecute(String result) {
    	if (result.equals("error")) {
			onProgressUpdate(-1);
    	} else if (result.equals("finished")) {
        	//清除进度显示
			onProgressUpdate(-100);
    	}
    }


}
