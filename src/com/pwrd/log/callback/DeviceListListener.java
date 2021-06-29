package com.pwrd.log.callback;

import java.util.ArrayList;

import com.pwrd.log.bean.DeviceBean;

/**
 * 设备列表监听
 * @author Administrator
 *
 */
public interface DeviceListListener {

	void notifyDeviceList(ArrayList<DeviceBean> deviceList);
}
