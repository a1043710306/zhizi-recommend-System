package com.inveno.process;

import com.inveno.common.handler.RedisClusterUtil;


public class TestHandler {
	
	public static void main(String[] args) {
		
		RedisClusterUtil.getInstance().set("testtime", "20160113");
	}

}
