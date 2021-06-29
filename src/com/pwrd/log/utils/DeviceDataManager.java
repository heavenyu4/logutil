package com.pwrd.log.utils;

import java.util.ArrayList;
import java.util.List;

import com.pwrd.log.bean.CmdInfo;
import com.pwrd.log.bean.DeviceBean;
import com.pwrd.log.callback.CurrentDeviceListener;
import com.pwrd.log.callback.DeviceListListener;

/**
 * 设备信息管理
 * 
 * @author Administrator
 *
 */
public class DeviceDataManager {

	private DeviceDataManager() {
	}

	private static volatile DeviceDataManager instance = null;

	public static DeviceDataManager getInstance() {
		if (instance == null) {
			synchronized (DeviceDataManager.class) {
				if (instance == null) {
					instance = new DeviceDataManager();
				}
			}
		}
		return instance;
	}

	// 设备列表
	private ArrayList<DeviceBean> mDeviceList = new ArrayList<DeviceBean>();
	// 当前选中的设备
	private DeviceBean currentDeviceBean;

	public ArrayList<DeviceBean> getDeviceList() {
		ArrayList<DeviceBean> tmp = new ArrayList<DeviceBean>();
		if (mDeviceList != null && mDeviceList.size() > 0) {
			for (int i = 0; i < mDeviceList.size(); i++) {
				tmp.add(mDeviceList.get(i).clone());
			}
		}
		return tmp;
	}

	/**
	 * 设置新的设备列表, 刷新后重新设置设备列表用
	 * 
	 * @param deviceList
	 */
	public void setDeviceList(ArrayList<DeviceBean> deviceList) {
		OperLog.getInstance().recordLog("setDeviceList: " + deviceList);
		// 更新下命令参数, 传入的deviceList参数只有设备信息, 命令信息是空的
		if (deviceList != null && deviceList.size() > 0) {
			ArrayList<DeviceBean> tmp = new ArrayList<DeviceBean>();
			for (int i = 0; i < deviceList.size(); i++) {
				DeviceBean bean = deviceList.get(i);
				bean.cmdInfo = findDeviceCmd(bean);
				tmp.add(bean.clone());
			}

			this.mDeviceList = tmp;
		} else {
			this.mDeviceList = new ArrayList<DeviceBean>();
		}
		if (deviceListListeners.size() > 0) {
			for (int i = 0; i < deviceListListeners.size(); i++) {
				deviceListListeners.get(i).notifyDeviceList(this.mDeviceList);
			}
		}
	}

	public synchronized DeviceBean getCurrentDeviceBean() {
		return currentDeviceBean;
	}

	public synchronized void setCurrentDeviceBean(DeviceBean currentDeviceBean) {
		OperLog.getInstance()
				.recordLog("setCurrentDeviceBean: " + currentDeviceBean == null ? null : currentDeviceBean.toResult());
		if (currentDeviceBean == null) {
			this.currentDeviceBean = currentDeviceBean;
		} else {
			this.currentDeviceBean = currentDeviceBean.clone();
			updateDeviceList();
		}
		if (currentDeviceListeners.size() > 0) {
			for (int i = 0; i < currentDeviceListeners.size(); i++) {
				currentDeviceListeners.get(i).notifyCurrentDevice(currentDeviceBean);
			}
		}
	}

	private void updateDeviceList() {
//		OperLog.getInstance().recordLog("updateDeviceList: " + mDeviceList);
		if (currentDeviceBean != null && mDeviceList.size() > 0) {
			for (int i = 0; i < mDeviceList.size(); i++) {
				if (mDeviceList.get(i).serialNo.equals(currentDeviceBean.serialNo)) {
					DeviceBean clone = currentDeviceBean.clone();
//					OperLog.getInstance().recordLog("updateDeviceList: clone: " + clone.hashCode());
					mDeviceList.set(i, clone);
					OperLog.getInstance()
							.recordLog("updateDeviceList: find " + i + " current: " + currentDeviceBean.toResult());
//					OperLog.getInstance().recordLog("updateDeviceList: deviceList : " + mDeviceList);
					break;
				}
			}
		}
	}

	/**
	 * 设置命令行信息
	 * 
	 * @param in
	 */
	public void setCmd(DeviceBean in) {
		if (in == null) {
			OperLog.getInstance().recordLog("setCmd: in null");
			return;
		}
		if (mDeviceList == null) {
			OperLog.getInstance().recordLog("setCmd: deviceList null");
			return;
		}

		for (int i = 0; i < mDeviceList.size(); i++) {
			DeviceBean deviceBean = mDeviceList.get(i);
			if (deviceBean.serialNo.equals(in.serialNo)) {
				deviceBean.cmdInfo = in.cmdInfo;
				break;
			}
		}
	}

	/**
	 * 查找设备抓取log命令的进程号pid
	 * 
	 * @param in
	 * @return
	 */
	public String findDevicePid(DeviceBean in) {
		if (in == null) {
			OperLog.getInstance().recordLog("findDevicePid: in null");
			return null;
		}
		if (mDeviceList == null) {
			OperLog.getInstance().recordLog("findDevicePid: deviceList null");
			return null;
		}
		for (int i = 0; i < mDeviceList.size(); i++) {
			DeviceBean deviceBean = mDeviceList.get(i);
			if (deviceBean.serialNo.equals(in.serialNo)) {
				if (deviceBean.cmdInfo != null) {
					return deviceBean.cmdInfo.pid;
				} else {
					return null;
				}
			}
		}
		return null;

	}

	/**
	 * 移除设备的命令信息, 停止抓log后的操作
	 * 
	 * @param in
	 */
	public void removeDeviceCmd(DeviceBean in) {
		if (in == null) {
			OperLog.getInstance().recordLog("removeDeviceCmd: in null");
			return;
		}
		if (mDeviceList == null) {
			OperLog.getInstance().recordLog("removeDeviceCmd: deviceList null");
			return;
		}
		for (int i = 0; i < mDeviceList.size(); i++) {
			DeviceBean deviceBean = mDeviceList.get(i);
			if (deviceBean.serialNo.equals(in.serialNo)) {
				deviceBean.cmdInfo = null;
				break;
			}
		}
		return;
	}

	/**
	 * 查找设备的命令信息
	 * 
	 * @param in
	 * @return
	 */
	public CmdInfo findDeviceCmd(DeviceBean in) {
		if (in == null) {
			OperLog.getInstance().recordLog("findDeviceCmd: in null");
			return null;
		}
		if (mDeviceList == null) {
			OperLog.getInstance().recordLog("findDeviceCmd: deviceList null");
			return null;
		}
		for (int i = 0; i < mDeviceList.size(); i++) {
			DeviceBean deviceBean = mDeviceList.get(i);
			OperLog.getInstance().recordLog(
					"findDeviceCmd: devicelist: " + deviceBean.hashCode() + " content:" + deviceBean.toResult());
			if (deviceBean.serialNo.equals(in.serialNo)) {
				OperLog.getInstance().recordLog("findDeviceCmd: find cmd " + i + " " + deviceBean.toResult());
				if (deviceBean.cmdInfo == null) {
					return null;
				} else {
					return deviceBean.cmdInfo.clone();
				}
			}
		}
		return null;

	}

	/**
	 * 获取所有正在抓log的设备信息, 退出时杀进程用
	 * 
	 * @return
	 */
	public List<DeviceBean> getLogProcess() {
		ArrayList<DeviceBean> list = new ArrayList<DeviceBean>();
		if (mDeviceList == null) {
			OperLog.getInstance().recordLog("getLogProcess: deviceList null");
			return null;
		}
		for (int i = 0; i < mDeviceList.size(); i++) {
			DeviceBean deviceBean = mDeviceList.get(i);
			if (deviceBean.cmdInfo != null) {
				list.add(deviceBean);
			}
		}
		return list;
	}

	ArrayList<DeviceListListener> deviceListListeners = new ArrayList<DeviceListListener>();

	public void addDeviceListListener(DeviceListListener listener) {
		if (listener != null && !deviceListListeners.contains(listener)) {
			deviceListListeners.add(listener);
		}
	}

	ArrayList<CurrentDeviceListener> currentDeviceListeners = new ArrayList<CurrentDeviceListener>();

	public void addCurrentDeviceListener(CurrentDeviceListener listener) {
		if (listener != null && !currentDeviceListeners.contains(listener)) {
			currentDeviceListeners.add(listener);
		}
	}

}
