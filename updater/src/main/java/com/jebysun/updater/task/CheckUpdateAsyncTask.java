package com.jebysun.updater.task;

import android.os.AsyncTask;

import com.jebysun.updater.service.UpdateService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckUpdateAsyncTask extends AsyncTask<String, Integer, String> {
	private UpdateService service;

	public CheckUpdateAsyncTask(UpdateService service) {
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
		String dataStr = null;
		try {
			dataStr = httpGetRequest(params[0]);
		} catch (Exception e) {
			return "error";
		}
		return dataStr;
	}

	/**
	 * 这里的Intege参数对应AsyncTask中的第二个参数
	 * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作
	 */
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
	}


	/**
	 * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
	 * 在doInBackground方法执行结束之后在运行
	 * 该方法运行在UI线程当中可以对UI空间进行设置
	 */
	@Override
	protected void onPostExecute(String result) {
		this.service.checkUpdateResult(result);
	}


	/**
	 * http get方式请求服务器，参数直接加载地址后面。
	 * @param urlStr
	 * @return json
	 * @throws IOException
	 */
	public String httpGetRequest(String urlStr) {
		String result = "";
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn;
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			conn.connect();
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader bufReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String lineStr = null;
				while ((lineStr = bufReader.readLine()) != null) {
					result += lineStr;
				}
				bufReader.close();
				conn.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
