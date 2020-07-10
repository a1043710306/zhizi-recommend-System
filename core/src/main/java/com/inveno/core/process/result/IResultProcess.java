package com.inveno.core.process.result;

import com.inveno.common.bean.Context;

public interface IResultProcess {
 	
	public void cache(Context context);
	
	public void noCache(Context context);

}
