package com.pwrd.log.bean;

public class CmdInfo {
	/**
	 * 执行抓log的命令
	 */
	public String cmd;
	
	/**
	 * 抓log的进程
	 */
	public String pid;

	public CmdInfo(String cmd, String pid) {
		super();
		this.cmd = cmd;
		this.pid = pid;
	}

	@Override
	public String toString() {
		return "CmdInfo [cmd=" + cmd + ", pid=" + pid + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cmd == null) ? 0 : cmd.hashCode());
		result = prime * result + ((pid == null) ? 0 : pid.hashCode());
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
		CmdInfo other = (CmdInfo) obj;
		if (cmd == null) {
			if (other.cmd != null)
				return false;
		} else if (!cmd.equals(other.cmd))
			return false;
		if (pid == null) {
			if (other.pid != null)
				return false;
		} else if (!pid.equals(other.pid))
			return false;
		return true;
	}
	
	@Override 
	public CmdInfo clone() {
		CmdInfo cmdInfo = new CmdInfo(this.cmd, this.pid);
		return cmdInfo;
	}
	
	
	
}
