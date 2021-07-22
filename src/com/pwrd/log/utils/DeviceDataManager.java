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
	// 上次的设备列表
	private ArrayList<DeviceBean> mOldDeviceList = new ArrayList<DeviceBean>();
	// 当前选中的设备
	private DeviceBean currentDeviceBean;

	public ArrayList<DeviceBean> getDeviceList() {
//		ArrayList<DeviceBean> tmp = new ArrayList<DeviceBean>();
//		if (mDeviceList != null && mDeviceList.size() > 0) {
//			for (int i = 0; i < mDeviceList.size(); i++) {
//				tmp.add(mDeviceList.get(i).clone());
//			}
//		}
//		return tmp;
		return mDeviceList;
	}

	public void reset() {
		mOldDeviceList.clear();
		mOldDeviceList.addAll(mDeviceList);
		mDeviceList.clear();
	}

	/**
	 * 设置新的设备列表, 刷新后重新设置设备列表用
	 * 
	 * @param deviceList
	 */
	public void setDeviceList(ArrayList<DeviceBean> deviceList) {
		OperLog.getInstance().recordLog("setDeviceList: ");
		printDeviceList(deviceList);
//		// 更新下命令参数, 传入的deviceList参数只有设备信息, 命令信息是空的
		if (deviceList != null && deviceList.size() > 0) {
			ArrayList<DeviceBean> tmp = new ArrayList<DeviceBean>();
			for (int i = 0; i < deviceList.size(); i++) {
				DeviceBean bean = deviceList.get(i);
				bean.cmdInfo = findOldDeviceCmd(bean);
				tmp.add(bean.clone());
			}

			this.mDeviceList = tmp;
		} else {
			this.mDeviceList = new ArrayList<DeviceBean>();
		}
//		this.mDeviceList = deviceList;
		OperLog.getInstance().recordLog("old: ");
		printDeviceList(mOldDeviceList);
		OperLog.getInstance().recordLog("new: ");
		printDeviceList(mDeviceList);
//		if (!mOldDeviceList.equals(mDeviceList) && deviceListListeners.size() > 0) {
		if (deviceListListeners.size() > 0) {
			for (int i = 0; i < deviceListListeners.size(); i++) {
				deviceListListeners.get(i).notifyDeviceList(this.mDeviceList);
			}
		}
	}

	/**
	 * 根据设备列表逐个添加设备信息的时候使用
	 * 
	 * @param bean
	 */
	public void addDevice(DeviceBean bean) {
		if (!isInList(mDeviceList, bean)) {
//			updateBeanCmd(bean);
			mDeviceList.add(bean);
			setDeviceList(mDeviceList);
		}
	}

//	/**
//	 * 从oldDeviceList提取cmd命令到bean
//	 * 存储之前的命令存储操作
//	 * @param bean 
//	 */
//	private void updateBeanCmd(DeviceBean bean) {
//		OperLog.getInstance().recordLog("updateBeanCmd: before: " + bean);
//		if (bean == null) {
//			return;
//		}
//		if (mOldDeviceList != null && mOldDeviceList.size() > 0) {
//			for (int i = 0; i < mOldDeviceList.size(); i++) {
//				if (bean.serialNo.equals(mOldDeviceList.get(i).serialNo)) {
//					bean.cmdInfo = mOldDeviceList.get(i).cmdInfo;
//					OperLog.getInstance().recordLog("updateBeanCmd: after: " + bean);
//					return;
//				}
//			}
//		}
//	}

	/**
	 * 查找当前bean是否已在list列表中
	 * 
	 * @param list
	 * @param bean
	 * @return
	 */
	private boolean isInList(ArrayList<DeviceBean> list, DeviceBean bean) {
		if (list == null || bean == null) {
			return false;
		}
		for (int i = 0; i < list.size(); i++) {
			if (bean.serialNo.contentEquals(list.get(i).serialNo)) {
				return true;
			}
		}
		return false;
	}

	public synchronized DeviceBean getCurrentDeviceBean() {
		return currentDeviceBean;
	}

	public synchronized void setCurrentDeviceBean(DeviceBean currentDeviceBean) {
		OperLog.getInstance()
				.recordLog("setCurrentDeviceBean: " + currentDeviceBean == null ? null : currentDeviceBean.toResult());
		this.currentDeviceBean = currentDeviceBean.clone();
		updateDeviceList();
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
//					DeviceBean clone = currentDeviceBean.clone();
//					OperLog.getInstance().recordLog("updateDeviceList: clone: " + clone.hashCode());
					mDeviceList.set(i, currentDeviceBean);
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
	public CmdInfo findOldDeviceCmd(DeviceBean in) {
		if (in == null) {
			OperLog.getInstance().recordLog("findOldDeviceCmd: in null");
			return null;
		}
		if (mOldDeviceList != null && mOldDeviceList.size() > 0) {

			for (int i = 0; i < mOldDeviceList.size(); i++) {
				DeviceBean deviceBean = mOldDeviceList.get(i);
				OperLog.getInstance().recordLog(
						"findOldDeviceCmd: devicelist: " + deviceBean.hashCode() + " content:" + deviceBean.toResult());
				if (deviceBean.serialNo.equals(in.serialNo)) {
					OperLog.getInstance().recordLog("findOldDeviceCmd: find cmd " + i + " " + deviceBean.toResult());
					if (deviceBean.cmdInfo == null) {
						return null;
					} else {
						return deviceBean.cmdInfo.clone();
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 查找设备的命令信息
	 * 
	 * @param in
	 * @return
	 */
	public CmdInfo findCurrDeviceCmd(DeviceBean in) {
		if (in == null) {
			OperLog.getInstance().recordLog("findCurrDeviceCmd: in null");
			return null;
		}
		if (mDeviceList != null && mDeviceList.size() > 0) {

			for (int i = 0; i < mDeviceList.size(); i++) {
				DeviceBean deviceBean = mDeviceList.get(i);
				OperLog.getInstance().recordLog(
						"findCurrDeviceCmd: devicelist: " + deviceBean.hashCode() + " content:" + deviceBean.toResult());
				if (deviceBean.serialNo.equals(in.serialNo)) {
					OperLog.getInstance().recordLog("findCurrDeviceCmd: find cmd " + i + " " + deviceBean.toResult());
					if (deviceBean.cmdInfo == null) {
						return null;
					} else {
						return deviceBean.cmdInfo.clone();
					}
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

	public void printDeviceList(ArrayList<DeviceBean> list) {
		if (list != null && list.size() > 0) {
			OperLog.getInstance().recordLog("======list start=====");
			for (int i = 0; i < list.size(); i++) {
				OperLog.getInstance().recordLog(list.get(i).toResult());
			}
			OperLog.getInstance().recordLog("======list end  =====");
		}
	}
	
}
