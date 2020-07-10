package com.inveno.process;

import com.inveno.common.handler.RedisClusterUtil;

public class TestRedisCache {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//RedisClusterUtil.getInstance().set("zhizi:toplist:total:tm", (System.currentTimeMillis()) + "" );
		RedisClusterUtil.getInstance().rpush("zhizi:toplist:total", "88", "99", "10");
	}

}