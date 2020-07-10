package com.inveno.fallback.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.inveno.fallback.IServiceLoader;

public class SpringContextLoader implements IServiceLoader{

	public void loadService() {
		// TODO Auto-generated method stub
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationcontext-*.xml");
	}

	public void destroy() {
		// TODO Auto-generated method stub
	}

}
