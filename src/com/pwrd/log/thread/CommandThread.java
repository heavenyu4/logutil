package com.pwrd.log.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.pwrd.log.callback.NotifyCallBack;
import com.pwrd.log.callback.RefreshUICallBack;
import com.pwrd.log.utils.OperLog;
import com.pwrd.log.utils.StringUtils;

public class CommandThread extends Thread {

	private StringBuffer sb;
	private String command;
	private RefreshUICallBack callBack;
	private NotifyCallBack mNotifyCallBack;

	public CommandThread(String commandStr, RefreshUICallBack refreshUICallBack) {
		this(commandStr, refreshUICallBack, null);
	}

	public CommandThread(String commandStr, RefreshUICallBack refreshUICallBack, NotifyCallBack notifyCallBack) {
		sb = new StringBuffer();
		command = "cmd /c " + commandStr;
		callBack = refreshUICallBack;
		mNotifyCallBack = notifyCallBack;
	}

	@Override
	public void run() {
		BufferedReader br = null;
		try {
			OperLog.getInstance().recordLog("exec:\t" + command);
			Process p = Runtime.getRuntime().exec(command);

			InputStream errorStream = p.getErrorStream();

			InputStream is = p.getInputStream() != null ? p.getInputStream() : errorStream;
//			br = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
			br = new BufferedReader(new InputStreamReader(is, "GBK"));
			String line = null;

			//目前mNotifyCallBack回调专门用于收集adb抓log的进程
			if (mNotifyCallBack != null) {
				List<String> searchPids = search();
				String operation = "search adb.exe process result: " + searchPids;
				OperLog.getInstance().recordLog(operation);
				String pid = searchPids.get(searchPids.size() - 1);
				mNotifyCallBack.action(pid);
			}

			while ((line = br.readLine()) != null) {
				//如果该行内容为空的话就放过, 例如4.x手机, 总是有空行
				if (StringUtils.isEmpty(line)) {
					continue;
				}
				sb.append(line + "\n");
//					callBack.refreshUI(line);
//				}
			}

			//错误信息也打印出来
			if (errorStream != null) {
				BufferedReader errbufferedReader = new BufferedReader(new InputStreamReader(errorStream, "GBK"));
				String errLine = null;
				while (!StringUtils.isEmpty(errLine = errbufferedReader.readLine())) {
					OperLog.getInstance().recordLog(errLine);
					sb.append(errLine + "\n");
				}
			}

			if (is != null) {
//				sb.append("The command has been executed!\n");
				callBack.refreshUI(sb.toString());
			}
			OperLog.getInstance().recordLog(sb.toString());

		} catch (Exception e) {
			e.printStackTrace();
			OperLog.getInstance().recordLog(e.getMessage());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
					OperLog.getInstance().recordLog(e.getMessage());
				}
			}
		}
	}

	/**
	 * 查找adb.exe的所有进程号
	 * 
	 * @return
	 */
	public List<String> search() {
		String cmd = "tasklist /fi \"imagename eq adb.exe\" /fo list";

		BufferedReader bufferedReader = null;
		List<String> list = new ArrayList<String>();

		try {
			Process process = Runtime.getRuntime().exec(cmd);
			OperLog.getInstance().recordLog(cmd);

			bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String str;
			while ((str = bufferedReader.readLine()) != null) {

				if (str.startsWith("PID:")) {
					String[] array = str.split(":");
					String pid = array[1].trim();

					list.add(pid);
				}

			}

		} catch (IOException e) {
			OperLog.getInstance().recordLog(e.getMessage());
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				e.getMessage();
			}
		}
		return list;
	}

}
