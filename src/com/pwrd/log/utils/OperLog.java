package com.pwrd.log.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 记录操作日志
 * 
 * @author Administrator
 *
 */
public class OperLog {

	private final String logName = "oper.log";
	private String logAbsPath;

	private OperLog() {
		checkFileValid();
	}

	private final static OperLog instance = new OperLog();

	public static final OperLog getInstance() {
		return instance;
	}

	/**
	 * 
	 */
	private void checkFileValid() {
//		if (!StringUtils.isEmpty(logAbsPath)) {
//			
//			return;
//		}
		System.out.println("checkFileValid");
		String strPath = System.getProperty("user.dir");
		logAbsPath = strPath + "//library//" + logName;
		File logFile = new File(logAbsPath);
		if (logFile.exists()) {
			// 大于3M时删除该操作日志文件
			if (logFile.length() > 1024 * 1024 * 3) {
				logFile.delete();
			}
		}
	}

	public void recordLog(String text) {
		System.out.println(text);

		File logFile = new File(logAbsPath);
		BufferedWriter fileBW = null;
		try {
			fileBW = new BufferedWriter(new FileWriter(logFile, true));

			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String format = sdf.format(date);

			fileBW.write(format + "\t" + text + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fileBW != null) {
					fileBW.flush();
					fileBW.close();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}

	}

}
