package com.pwrd.log.callback;

import java.util.List;

import javax.swing.AbstractListModel;

import com.pwrd.log.bean.DeviceBean;


public class DeviceModel extends AbstractListModel<DeviceBean>{
	
	List<DeviceBean> data;

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public DeviceBean getElementAt(int index) {
		return data.get(index);
	}

	public DeviceModel(List<DeviceBean> data) {
		super();
		this.data = data;
	}

	
	
}
