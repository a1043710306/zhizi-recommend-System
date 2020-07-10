package com.inveno.core.sever;

import com.inveno.core.process.cms.CMSInterface;
import com.inveno.core.util.SysUtil;

public class TriggerCache {
	public static void updateCache() throws Exception{
		CMSInterface cmsInterface = (CMSInterface)SysUtil.getBean("cMSInterface");
		cmsInterface.initScenarioCagetory();
		cmsInterface.initConfigMixedRuleMap();
	}
}
