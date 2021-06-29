package com.pwrd.log.bean;

import com.sun.xml.internal.bind.v2.runtime.RuntimeUtil.ToStringAdapter;

public class DeviceBean {

	/*
	 * [ro.build.version.release]: [8.0.0] [ro.build.version.sdk]: [26]
	 * [ro.product.manufacturer]: [HUAWEI] [ro.product.model]: [EVA-AL10]
	 */

	/**
	 * 产品名 例如:EVA-AL10
	 */
	public String model = "";

	/**
	 * 制造商 例如:华为
	 */
	public String manufacturer = "";

	/**
	 * android版本 例如:8.0.0
	 */
	public String andVer = "";

	/**
	 * android API等级
	 */
	public String api = "";

	/**
	 * 设备序列号
	 */
	public String serialNo = "";

	public CmdInfo cmdInfo = null;

	public DeviceBean() {

	}

	@Override
	public String toString() {
		return "[" + manufacturer + " " + model + " Android" + andVer + " API" + api + " sn=" + serialNo + "]";
//		return "DeviceBean [model=" + model + ", manufacturer=" + manufacturer + ", andVer=" + andVer + ", api=" + api
//				+ ", serialNo=" + serialNo + ", cmdInfo=" + cmdInfo + "]";
	}
	
	public String toResult() {
		return "DeviceBean [model=" + model + ", manufacturer=" + manufacturer + ", andVer=" + andVer + ", api=" + api
				+ ", serialNo=" + serialNo + ", cmdInfo=" + cmdInfo + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((andVer == null) ? 0 : andVer.hashCode());
		result = prime * result + ((api == null) ? 0 : api.hashCode());
		result = prime * result + ((cmdInfo == null) ? 0 : cmdInfo.hashCode());
		result = prime * result + ((manufacturer == null) ? 0 : manufacturer.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((serialNo == null) ? 0 : serialNo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeviceBean other = (DeviceBean) obj;
		if (andVer == null) {
			if (other.andVer != null)
				return false;
		} else if (!andVer.equals(other.andVer))
			return false;
		if (api == null) {
			if (other.api != null)
				return false;
		} else if (!api.equals(other.api))
			return false;
		if (cmdInfo == null) {
			if (other.cmdInfo != null)
				return false;
		} else if (!cmdInfo.equals(other.cmdInfo))
			return false;
		if (manufacturer == null) {
			if (other.manufacturer != null)
				return false;
		} else if (!manufacturer.equals(other.manufacturer))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (serialNo == null) {
			if (other.serialNo != null)
				return false;
		} else if (!serialNo.equals(other.serialNo))
			return false;
		return true;
	}

	@Override
	public DeviceBean clone() {
		DeviceBean deviceBean = new DeviceBean();
		deviceBean.model = this.model;
		deviceBean.manufacturer = this.manufacturer;
		deviceBean.andVer = this.andVer;
		deviceBean.api = this.api;
		deviceBean.serialNo = this.serialNo;
		if (this.cmdInfo == null) {
			deviceBean.cmdInfo = null;
		} else {
			deviceBean.cmdInfo = this.cmdInfo.clone();
		}

		return deviceBean;
	}

}
