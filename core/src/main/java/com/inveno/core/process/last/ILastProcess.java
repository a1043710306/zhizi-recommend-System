package com.inveno.core.process.last;

import com.inveno.common.bean.Context;
import com.inveno.core.monitor.MonitorLog;
import com.inveno.core.process.IProcess;

public interface ILastProcess extends IProcess {
	
	public void setMonitorLog(MonitorLog monitorLog);

}
