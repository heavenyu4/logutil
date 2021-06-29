package com.pwrd.log.view;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.pwrd.log.bean.DeviceBean;
import com.pwrd.log.callback.DeviceListListener;
import com.pwrd.log.callback.DeviceModel;
import com.pwrd.log.callback.RefreshUICallBack;
import com.pwrd.log.thread.CommandThread;
import com.pwrd.log.utils.DeviceDataManager;
import com.pwrd.log.utils.OperLog;
import com.pwrd.log.utils.StringUtils;

/**
 * 设备 列表
 * 
 * @author Administrator
 *
 */
public class DeviceList extends JPanel {

	// 设备列表
	ArrayList<DeviceBean> mDeviceList = new ArrayList<DeviceBean>();

	// 列表view
	JList jList;
	// 刷新按钮
	JButton btnFresh;

	Font font;

	AtomicInteger deviceTotalCnt;

	public DeviceList() {
		initView();
		initData();
	}

	private void initData() {
		DeviceDataManager.getInstance().addDeviceListListener(new DeviceListListener() {

			@Override
			public void notifyDeviceList(ArrayList<DeviceBean> deviceList) {
				EventQueue.invokeLater(new Runnable() {

					@Override
					public void run() {
						// 更新设备列表
						btnFresh.setEnabled(true);
						// 清除选中状态
						jList.clearSelection();
						ArrayList<DeviceBean> tmp = deviceList == null ? new ArrayList<DeviceBean>() : deviceList;
						mDeviceList.clear();
						mDeviceList.addAll(tmp);
						jList.updateUI();
					}
				});
			}
		});
	}

	private void initView() {

		// 字体设置
		font = new Font("微软雅黑", Font.BOLD, 14);

		DeviceModel dataModel = new DeviceModel(mDeviceList);
		jList = new JList(dataModel);
		// 单选模式
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				// 选中某一项
				if (jList.getValueIsAdjusting()) {
					mDeviceList = DeviceDataManager.getInstance().getDeviceList();
					DeviceBean selectDeviceBean = mDeviceList.get(jList.getSelectedIndex());
					OperLog.getInstance().recordLog("jList selected: " + selectDeviceBean.toResult());
					DeviceDataManager.getInstance().setCurrentDeviceBean(selectDeviceBean);
//					if (deviceClickListener != null) {
//						deviceClickListener.onClick(deviceList.get(jList.getSelectedIndex()));
//					}
				}
			}
		});
		Font fontList = new Font("微软雅黑", Font.PLAIN, 12);
		jList.setFont(fontList);
		JScrollPane scrollPane = new JScrollPane(jList);
		// 分别设置水平和垂直滚动条自动出现
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(15, 20, 230, 280);
		add(scrollPane);
		setLayout(null);
		/*
		 * public static TitledBorder createTitledBorder(Border border, String title,
		 * int titleJustification, int titlePosition, Font titleFont) { this(null,
		 * title, LEADING, DEFAULT_POSITION, null, null);
		 */
		setBorder(BorderFactory.createTitledBorder(null, "设备列表", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				font));
		setFont(font);

		btnFresh = new JButton();
		btnFresh.setText("刷新");
		btnFresh.setFont(font);
		btnFresh.setBounds(scrollPane.getX(), scrollPane.getY() + scrollPane.getHeight() + 20, 70, 30);
		btnFresh.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				freshDeviceList();
			}
		});
		add(btnFresh);
		killOtherAdb();

	}

	/**
	 * 根据设备序列号 查找设备名
	 * 
	 * @param deviceString KWG7N16803000154 device
	 */
	private void findName(List<DeviceBean> deviceList, String deviceString) {
		if (StringUtils.isEmpty(deviceString)) {
			String operation = "findName: devicedesc empty: [" + deviceString + "]";
			OperLog.getInstance().recordLog(operation);
			return;
		}

		String[] split = deviceString.split("\t");
		if (split != null) {
			String serialNo = split[0];

			String strPath = System.getProperty("user.dir");
			String cmd = strPath + "\\library\\adb.exe -s " + serialNo
					+ "  shell \" getprop | grep -e ro.product.manufacturer -e market -e ro.product.model -e ro.build.version.release -e ro.build.version.sdk\" ";
			new CommandThread(cmd, new RefreshUICallBack() {

				@Override
				public void refreshUI(String name) {
					if (StringUtils.isEmpty(name)) {
						String operation = "findName: " + deviceString + " cmd result empty: [" + name + "]";
						OperLog.getInstance().recordLog(operation);
					} else {
						/*
						 * [ro.build.version.release]: [8.0.0] [ro.build.version.sdk]: [26]
						 * [ro.product.manufacturer]: [HUAWEI] [ro.product.model]: [EVA-AL10]
						 */

						DeviceBean bean = extractDeviceInfo(name);
						bean.serialNo = serialNo;

						deviceList.add(bean);
						OperLog.getInstance().recordLog(deviceList.toString());

						int decrementAndGet = deviceTotalCnt.decrementAndGet();
						OperLog.getInstance().recordLog("findName refreshUI: device cnt: " + decrementAndGet);
						if (decrementAndGet == 0) {
							DeviceDataManager.getInstance().setDeviceList(new ArrayList<DeviceBean>(deviceList));
						}

					}

				}
			}).start();

		}

	}

	protected DeviceBean extractDeviceInfo(String name) {
		/*
		 * [ro.build.version.release]: [8.0.0] [ro.build.version.sdk]: [26]
		 * [ro.product.manufacturer]: [HUAWEI] [ro.product.model]: [EVA-AL10]
		 */
		DeviceBean bean = new DeviceBean();
		name = name.substring(0, name.lastIndexOf("\n"));
		// 去掉[ ] 空格+[ 例如, [ro.build.version.release]: [8.0.0] ->
		// ro.build.version.release:8.0.0
		name = name.replace(" [", "");
		name = name.replace("[", "");
		name = name.replace("]", "");
		String[] split1 = name.split("\n");
		if (split1 != null && split1.length > 0) {
			String marketing_name = "";
			for (int i = 0; i < split1.length; i++) {
				if (!StringUtils.isEmpty(split1[i])) {
//					split1[i]: 表示每一行 例如 ,ro.build.version.release:8.0.0
					// 去除空格, 比如OPPO R17 ->OPPO_R17
//					split2: 表示每一行按照":"分割
					String[] split2 = split1[i].replace(" ", "_").split(":");
					if (split2 != null && split2.length > 0) {
						String value = split2[0].replace(" ", "");
						switch (value) {
						case "ro.build.version.release":
							bean.andVer = split2[1];
							break;
						case "ro.build.version.sdk":
							bean.api = split2[1];
							break;
						case "ro.product.manufacturer":
							bean.manufacturer = split2[1];
							break;
						case "ro.product.model":
							bean.model = split2[1];
							break;
						default:
							break;
						}
						// 有的属性是ro.config.marketing_name 有的属性是 market.name
//						现在测试 试用情况较多的是oppo huawei vivo 
						// [ro.oppo.market.name]: [OPPO R17]
						// [ro.config.marketing_name]: [POT-AL00a] huawei
						// [ro.config.marketing_name]: [HUAWEI P9]
						if (value.contains("marketing_name") || value.contains("market.name")) {
							marketing_name = split2[1];
						}

					}
				}
			}
			// marketing_name 可以展示设备名称 比如 华为p30 pro 如果marketing_name有的话 就显示这个
			if (!StringUtils.isEmpty(marketing_name)) {
				OperLog.getInstance().recordLog("extractDeviceInfo: marketing_name:" + marketing_name);
				if (marketing_name.contains(bean.manufacturer)) {
					// 有的marketing_name 包含manufacturer 例如:[HUAWEI P9]
					bean.manufacturer = marketing_name;
					bean.manufacturer = modifyManufacturerName(bean.manufacturer);
				} else {
					// 有的marketing_name 不包含manufacturer 例如:[ro.config.marketing_name]: [POT-AL00a]
					// 这就需要将manufacturer加上
					String tmp = bean.manufacturer + "_" + marketing_name;
					tmp = modifyManufacturerName(tmp);
					bean.manufacturer = tmp;
				}
			}
			OperLog.getInstance().recordLog("extractDeviceInfo: manufacturer final: " + bean.manufacturer);
		}

		return bean;

	}

	/**
	 * 修改厂商名, 这个后边要做抓log文件的文件名, 不能含有非法字符
	 * 
	 * @param manufacturer
	 * @return
	 */
	private String modifyManufacturerName(String manufacturer) {
		String tmp = manufacturer;
		// 去掉空格
		tmp = manufacturer.replace(" ", "");
		// 去掉非法字符
		String[] notValidStr = { "<", ">", "|", ":", "*", "?", "/", "\\" };
		for (int i = 0; i < notValidStr.length; i++) {
			tmp = tmp.replace(notValidStr[i], "_");
		}
		return tmp;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 刷新设备列表
	 */
	private void freshDeviceList() {
		btnFresh.setEnabled(false);
		OperLog.getInstance().recordLog("freshDeviceList: devicelist before: " + mDeviceList);
		mDeviceList.clear();
		jList.updateUI();
		String strPath = System.getProperty("user.dir");
		String cmd = strPath + "\\library\\adb.exe devices";
		new CommandThread(cmd, new RefreshUICallBack() {

			@Override
			public void refreshUI(String string) {
				String[] split = string.split("\n");
				if (split != null) {
					boolean isDeviceEmpty = true;
					deviceTotalCnt = new AtomicInteger(split.length);
					for (int i = 0; i < split.length; i++) {
						if (split[i].startsWith("*") || split[i].contains("List of")) {
							deviceTotalCnt.decrementAndGet();
							// adb.exe被销毁再连接时会返回这个
							/*
							 * C:\Users\Administrator>adb devices daemon not running; starting now at
							 * tcp:5037 daemon started successfully List of devices attached
							 * KWG7N16803000154 device
							 */
						} else {
							isDeviceEmpty = false;
							findName(mDeviceList, split[i]);
						}
					}
					if (isDeviceEmpty) {
						// 没有设备时 要更新列表
						DeviceDataManager.getInstance().setDeviceList(null);
					}
				} else {
					String operation = "no device connected!";
//					没有设备时 要更新列表
					OperLog.getInstance().recordLog("freshDeviceList: " + operation);
					DeviceDataManager.getInstance().setDeviceList(null);
				}

			}

		}).start();

	}

	/**
	 * 杀掉所有的adb.exe 进程, 这个当adb.exe版本不一致时, 在执行adb devices时, 会发生找不到设备的情况,
	 * 所以这里杀掉所有的adb.exe
	 */
	private void killOtherAdb() {
		String cmd = "TASKKILL /F /IM  adb.exe";
		new CommandThread(cmd, new RefreshUICallBack() {

			@Override
			public void refreshUI(String string) {
				OperLog.getInstance().recordLog("killOtherAdb: " + string);
				freshDeviceList();
			}

		}).start();
	}

}
