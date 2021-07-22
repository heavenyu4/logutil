package com.pwrd.log.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.pwrd.log.bean.CmdInfo;
import com.pwrd.log.bean.DeviceBean;
import com.pwrd.log.callback.CurrentDeviceListener;
import com.pwrd.log.callback.DeviceListListener;
import com.pwrd.log.callback.NotifyCallBack;
import com.pwrd.log.callback.RefreshUICallBack;
import com.pwrd.log.thread.AdbStartLogThread;
import com.pwrd.log.thread.CommandThread;
import com.pwrd.log.utils.DeviceDataManager;
import com.pwrd.log.utils.OperLog;
import com.pwrd.log.utils.StringUtils;

public class CurrentDevicePannel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String TIP_ADVANCED = "如果您想使用高级版的话, 请在点击开始后, 重启游戏, 再执行您的游戏操作~";
	private final String TIP_SELECT_DEVICE = "请从设备列表中选择要抓取log的设备~";
	Font fontContent, fontTitle;
//	ArrayList<String> cmdList;
	JButton btnStart;
	JButton btnEnd;
	JLabel deviceLabel;
	JTextArea tipArea;
	JTextField logNameText;
	private StringBuilder sb = new StringBuilder();

	// 当前选中的设备
//	DeviceBean currentDeviceBean = null;
	private String strPath = "";
	private JTextArea logArea;
	/**
	 * 停止或是窗口关闭时, 是否需要删除onesdk的调试文件 我们测试不需要一直删这个文件
	 */
	private boolean delDebugFileSwitch = true;

	public CurrentDevicePannel() {
		initView();
		initData();
	}

	private void initView() {

		int xOffset = 0;
		int width = 500;
		try {
			strPath = URLDecoder.decode(System.getProperty("user.dir"), "utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		setBackground(Color.RED);

		setLayout(new FlowLayout(FlowLayout.LEADING, 0, 20));
//		setAlignmentX(LEFT_ALIGNMENT);

//		setAlignmentY(LEFT_ALIGNMENT);
		// 字体设置
		fontTitle = new Font("微软雅黑", Font.BOLD, 14);
		fontContent = new Font("微软雅黑", Font.PLAIN, 12);

		deviceLabel = new JLabel();
		deviceLabel.setBounds(xOffset, 0, width, 30);
		deviceLabel.setFont(fontTitle);
		deviceLabel.setText("机型:");

		add(deviceLabel);
//		add(Box.createVerticalStrut(30));

		// 提示窗口
		tipArea = new JTextArea();
//		tipArea.setBounds(xOffset, deviceLabel.getY() + deviceLabel.getHeight() + 10, width, 50);
//		tipArea.setBounds(0, 10, width, 50);
		tipArea.setPreferredSize(new Dimension(width, 50));
		tipArea.setText(TIP_SELECT_DEVICE);
		tipArea.setLineWrap(true);
		tipArea.setFont(fontContent);
		add(tipArea);

		// log文件名
		JPanel logNamePal = new JPanel();
//		GridBagLayout gridLayout = new GridBagLayout();

		logNamePal.setLayout(new BoxLayout(logNamePal, BoxLayout.X_AXIS));
//		logNamePal.setLayout(gridLayout);
//		logNamePal.setBounds(xOffset, tipArea.getY() + tipArea.getHeight() + 10, width, 30);
		logNamePal.setPreferredSize(new Dimension(width, 30));

		JLabel logNameLabel = new JLabel("自定义log文件名:");
		logNameLabel.setFont(fontTitle);

		logNamePal.add(logNameLabel);
//		logNameLabel.setSize(60, 50);

//		logNamePal.add(Box.createHorizontalStrut(10));
//		logNamePal.add(Box.createHorizontalGlue());

		logNameText = new JTextField("");
		logNameText.setFont(fontContent);
		logNameText.setToolTipText("请输入log文件名, 您不输入的话, 以当前时间为log文件名");
//		logNameText.setPreferredSize(new Dimension(350,30));
//		logNameText.setBounds(0,0,300,30);
//		gridLayout.setConstraints(logNameText, GridBagConstraints.HORIZONTAL);
		logNamePal.add(logNameText);

		add(logNamePal);

//		add(Box.createVerticalStrut(50));
		GridLayout layout = new GridLayout(1, 3);
		layout.setHgap(30);
		layout.setVgap(20);
		JPanel btnPanel = new JPanel(layout);
//		btnPanel.setBackground(Color.GREEN);
//		btnPanel.setBounds(50, 0, width, 50);
		btnPanel.setPreferredSize(new Dimension(width, 40));
		add(btnPanel);

		btnStart = new JButton("开始");
//		btnStart.setBounds(xOffset, 100, 100, 50);
//		btnStart.setPreferredSize(new Dimension(130, 50));
		btnStart.setFont(fontTitle);
		btnPanel.add(btnStart);

		btnEnd = new JButton("停止");
//		btnEnd.setBounds(xOffset+200, 100, 100, 50);
//		btnEnd.setPreferredSize(new Dimension(130, 50));
		btnEnd.setFont(fontTitle);
		btnPanel.add(btnEnd);

		JButton btnOpenDir = new JButton();
		btnOpenDir.setText("打开日志目录");
//		btnOpenDir.setPreferredSize(new Dimension(130, 50));
		btnOpenDir.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String dirPath = System.getProperty("user.dir") + "//log";
				try {
					java.awt.Desktop.getDesktop().open(new File(dirPath));
				} catch (IOException e3) {
					e3.printStackTrace();
				}

			}
		});
		btnOpenDir.setFont(fontTitle);
		btnPanel.add(btnOpenDir);

//		add(Box.createVerticalStrut(30));
		// 操作记录
		JPanel groupBox = new JPanel();
		logArea = new JTextArea();
//		logArea.setBounds(5, 5, 290, 290);
		logArea.setLineWrap(true);
		logArea.setRows(13);
		logArea.setColumns(42);
		logArea.setFont(fontContent);
		JScrollPane scrollPane = new JScrollPane(logArea);

		// 分别设置水平和垂直滚动条自动出现
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		groupBox.add(scrollPane);
		groupBox.setBorder(BorderFactory.createTitledBorder(null, "操作：", TitledBorder.LEADING,
				TitledBorder.DEFAULT_POSITION, fontTitle));
		groupBox.setBounds(xOffset, btnPanel.getY() + btnPanel.getHeight() + 10, width, 300);

		add(groupBox);
	}

	private void initData() {
		btnStart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				DeviceBean currentDeviceBean = DeviceDataManager.getInstance().getCurrentDeviceBean();
				if (currentDeviceBean == null) {
					JOptionPane.showMessageDialog(getParent(), "请选择设备!", "提示", JOptionPane.WARNING_MESSAGE);
					return;
				}

				String lognameInput = logNameText.getText().replace(" ", "");
				String logName = "";
				if (currentDeviceBean != null) {
					if (currentDeviceBean.manufacturer.contains(currentDeviceBean.model)) {
						logName += currentDeviceBean.manufacturer.replace(" ", "") + "_";

					} else {
						logName += currentDeviceBean.manufacturer.replace(" ", "") + "_"
								+ currentDeviceBean.model.replace(" ", "") + "_";
					}
				}
				if (StringUtils.isEmpty(lognameInput)) {
					Date date = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
					String format = sdf.format(date);
					logName += format + ".log";
				} else {
					if (!isFileNameValid(lognameInput)) {
						JOptionPane.showMessageDialog(getParent(), "log文件名不合法, 不能包含" + "< > / \\ | : * ? aux", "警告",
								JOptionPane.ERROR_MESSAGE);
						return;
					} else {
						logName += lognameInput + ".log";
					}
				}

				btnStart.setEnabled(false);

				// 拷贝onesdk的debug开关文件
				copyDebugFile(currentDeviceBean);

				String cmd = strPath + "\\library\\adb.exe -s " + currentDeviceBean.serialNo + " logcat -v threadtime >"
						+ strPath + "\\log\\" + logName;
				updateLogArea("【" + currentDeviceBean.model + "】" + " 开始抓取log: " + logName + " .......");
				try {
					new AdbStartLogThread(cmd, new RefreshUICallBack() {

						@Override
						public void refreshUI(String string) {
							OperLog.getInstance().recordLog("initData: btnStart: " + string);

						}
					}, new NotifyCallBack() {

						@Override
						public void action(String pid) {
							// TODO Auto-generated method stub
							String operation = "pid: " + pid;
							OperLog.getInstance().recordLog("initData: btnStart: " + operation);

							if (currentDeviceBean != null) {
								currentDeviceBean.cmdInfo = new CmdInfo(cmd, pid);
								DeviceDataManager.getInstance().setCurrentDeviceBean(currentDeviceBean);
							}

						}
					}).start();
				} catch (Exception e1) {
					OperLog.getInstance().recordLog("initData: btnStart: " + e1.getMessage());
				}
			}

		});

		btnEnd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				btnStart.setEnabled(true);
				DeviceBean currentDeviceBean = DeviceDataManager.getInstance().getCurrentDeviceBean();

				if (currentDeviceBean != null) {

					if (currentDeviceBean.cmdInfo == null) {
						OperLog.getInstance().recordLog("btnEnd actionPerformed: cmdinfo null: " + currentDeviceBean);
						return;
					}
					delDebugFile(currentDeviceBean);
					String pid = currentDeviceBean.cmdInfo.pid;
					if (!StringUtils.isEmpty(pid)) {
						String cmd = " taskkill /f /pid " + pid;
						new CommandThread(cmd, new RefreshUICallBack() {

							@Override
							public void refreshUI(String string) {
								currentDeviceBean.cmdInfo = null;
								DeviceDataManager.getInstance().setCurrentDeviceBean(currentDeviceBean);
							}
						}).start();

						updateLogArea("【" + currentDeviceBean.model + "】 抓取log停止");
					}

				}

			}
		});

		DeviceDataManager.getInstance().addCurrentDeviceListener(new CurrentDeviceListener() {

			@Override
			public void notifyCurrentDevice(DeviceBean deviceBean) {
				if (deviceBean == null) {
					deviceLabel.setText("机型: ");
					tipArea.setText(TIP_SELECT_DEVICE);
					btnStart.setEnabled(true);
				} else {
					deviceLabel.setText("机型: " + deviceBean);
					logNameText.setText("");
					if (deviceBean.cmdInfo == null) {
						btnStart.setEnabled(true);
					} else {
						// 之前在抓着log
						btnStart.setEnabled(false);
					}
				}
			}
		});

		DeviceDataManager.getInstance().addDeviceListListener(new DeviceListListener() {

			@Override
			public void notifyDeviceList(ArrayList<DeviceBean> deviceList) {
				deviceLabel.setText("机型: ");
				tipArea.setText(TIP_SELECT_DEVICE);
				btnStart.setEnabled(true);
			}
		});

	}

	private boolean isFileNameValid(String fileName) {
		String[] notValidStr = { "<", ">", "|", ":", "*", "?", "/", "\\" };
		for (int i = 0; i < notValidStr.length; i++) {
			if (fileName.contains(notValidStr[i])) {
				return false;
			}
		}

		if (fileName.equals("aux")) {
			return false;
		}
		return true;
	}

	// 拷贝onesdk的debug开关文件
	private void copyDebugFile(DeviceBean bean) {
		String cmd = strPath + "//library//adb.exe -s " + bean.serialNo + " push " + strPath
				+ "//library//onesdk_develop.properties /sdcard/";
		new CommandThread(cmd, new RefreshUICallBack() {

			@Override
			public void refreshUI(String string) {
			}
		}).start();
	}

	// 删除onesdk的debug开关文件
	private void delDebugFile(DeviceBean bean) {
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

	private void updateLogArea(String text) {
		sb.append(text).append("\n");
		logArea.setText(sb.toString());
	}
}
