package com.inveno;

import com.alibaba.dubbo.container.Main;

/**
 * @author chao.li
 * @date 2016年9月23日
 */
public class CoreLauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty(Main.SHUTDOWN_HOOK_KEY, "true");
		System.setProperty("jute.maxbuffer", "40960000");
		Main.main(null);
	}

}
