package com.inveno.core.process.post;

import com.inveno.common.bean.Context;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.IProcess;

public interface IPostProcess extends IProcess {
	
	public void setMonitorLog(MonitorLog monitorLog);

}
