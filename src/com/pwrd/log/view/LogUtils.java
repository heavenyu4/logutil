package com.pwrd.log.view;

import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.google.gson.Gson;
import com.pwrd.log.bean.ConfigBean;
import com.pwrd.log.bean.DeviceBean;
import com.pwrd.log.callback.RefreshUICallBack;
import com.pwrd.log.thread.CommandThread;
import com.pwrd.log.utils.DeviceDataManager;
import com.pwrd.log.utils.OperLog;
import com.pwrd.log.utils.StringUtils;

/**
 * 抓取log主方法
 * 
 * @author Administrator
 *
 */
public class LogUtils {

//	private StringBuilder sb;
//	private final String TIP_ADVANCED = "如果您想使用高级版的话, 请在点击开始后, 重启游戏, 再执行您的游戏操作~";
//	private final String TIP_SELECT_DEVICE = "请从设备列表中选择要抓取log的设备~";
//	Font fontContent, fontTitle;
////	ArrayList<String> cmdList;
//	JButton btnStart;
//	JLabel deviceLabel;
//	JTextArea tipArea;
//
//	// 当前选中的设备
//	DeviceBean currentDeviceBean = null;
	private String strPath = "";
//	private JTextArea logArea;
	/**
	 * 停止或是窗口关闭时, 是否需要删除onesdk的调试文件 我们测试不需要一直删这个文件
	 */
	private boolean delDebugFileSwitch = true;

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LogUtils window = new LogUtils();
				} catch (Exception e) {
					e.printStackTrace();
					OperLog.getInstance().recordLog(e.getMessage());
				}
			}
		});

	}

	public LogUtils() {
		init();
	}

	private void init() {

		JFrame frm = new JFrame();
		frm.setTitle("抓Log工具");
		int xOffset = 300;
		int width = 500;
		frm.setBounds(xOffset, 100, 900, 620);
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.getContentPane().setLayout(null);
		frm.setResizable(false);
		// 设置windows界面效果
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		frm.setVisible(true);
		frm.setIconImage(new ImageIcon("library/bugfly.jpg").getImage());

		try {
			strPath = URLDecoder.decode(System.getProperty("user.dir"), "utf-8");
			// 去除空格后的路径名
			String subPath = strPath.replace(" ", "");
			if ((subPath.length() < strPath.length()) || strPath.contains("(") || strPath.contains(")")) {
				String pathErr = "path err: [" + strPath + "]";
				OperLog.getInstance().recordLog("init： " + pathErr);
				JOptionPane.showMessageDialog(frm, "log工具所在路径非法! \n请放置到无空格无()的路径下", "警告", JOptionPane.ERROR_MESSAGE);
			}
			OperLog.getInstance().recordLog("init： " + strPath);
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			OperLog.getInstance().recordLog("init： " + e2.getMessage());
		}

		// 设备列表
		DeviceList deviceList = new DeviceList();
		deviceList.setBounds(10, 50, 250, 370);
		frm.add(deviceList);

		CurrentDevicePannel currentDevicePannel = new CurrentDevicePannel();
		currentDevicePannel.setBounds(300, 10, 500, 700);
		frm.add(currentDevicePannel);

		frm.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {

			}

			@Override
			public void windowClosing(WindowEvent e) {
				// 窗口关闭时, 停止抓log
				// 这时候需要把onesdk开关的debug文件删下, 避免影响游戏测试的效果
				List<DeviceBean> allDevices = DeviceDataManager.getInstance().getDeviceList();
				if (allDevices != null && allDevices.size() > 0) {
					for (int i = 0; i < allDevices.size(); i++) {
						DeviceBean bean = allDevices.get(i);
						
						if (bean.cmdInfo != null) {
							String pid = bean.cmdInfo.pid;
							if (!StringUtils.isEmpty(pid)) {
								String cmd = " taskkill /f /pid " + pid;
								new CommandThread(cmd, new RefreshUICallBack() {

									@Override
									public void refreshUI(String string) {
//										DeviceDataManager.getInstance().removeDeviceCmd(currentDeviceBean);
									}
								}).start();
							}
							delDebugFile(bean);
						}
					}

				}
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
		delDebugFileSwitch = checkDebugFileSwitch();
	}


	private boolean checkDebugFileSwitch() {
		boolean delDebugFile = true;
		File file = new File(strPath + "//library//config.json");
		if (file.exists()) {
			try {
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line = null;
				StringBuilder sb = new StringBuilder();
//				while((line=bufferedReader.readLine())!=null) {
				while (!StringUtils.isEmpty(line = bufferedReader.readLine())) {
					sb.append(line);
				}
				OperLog.getInstance().recordLog("checkDebugFileSwitch: " + sb.toString());
				Gson gson = new Gson();
				ConfigBean configBean = gson.fromJson(sb.toString(), ConfigBean.class);
				delDebugFile = configBean.delDebugFile;

			} catch (Exception e) {
				e.printStackTrace();
				OperLog.getInstance().recordLog("checkDebugFileSwitch: " + e.getStackTrace().toString());
			}
		}

		OperLog.getInstance().recordLog("checkDebugFileSwitch:" + delDebugFile);
		return delDebugFile;

	}

	// 删除onesdk的debug开关文件
	public void delDebugFile(DeviceBean bean) {
		if (bean == null) {
			return;
		}
		if (!delDebugFileSwitch) {
			return;
		}
		String cmd = strPath + "//library//adb.exe -s " + bean.serialNo + " shell rm /sdcard/onesdk_develop.properties";
		new CommandThread(cmd, new RefreshUICallBack() {

			@Override
			public void refreshUI(String string) {
			}
		}).start();
	}

}
