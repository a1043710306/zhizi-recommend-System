package com.inveno.core.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class SysUtil implements ApplicationContextAware {

	private static ApplicationContext context;

	@SuppressWarnings("unchecked")
	public static <T> T getBean(String beanName) {
		return (T) context.getBean(beanName);
		// return (T) getSpringContext().getBean(beanName);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	/**
	 * 
	 * Description: 获取容器
	 * 
	 * @author liyuanyi DateTime 2016年1月16日 下午4:21:08
	 * @return
	 */
	/*
	 * private static ApplicationContext getSpringContext() { return
	 * ContextLoader.getCurrentWebApplicationContext(); }
	 */

}
