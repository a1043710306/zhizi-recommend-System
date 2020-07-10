package com.inveno.common.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

/**
 * 
 *  Class Name: SysUtil.java
 *  Description:
 *  系统工具类：
 *  目前包括管理Spring的bean 
 *  @author liyuanyi  DateTime 2016年1月21日 下午3:50:40 
 *  @company inveno 
 *  @version 1.0
 */
public class SysUtil {
	
	
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String beanName) {
		return (T) getSpringContext().getBean(beanName);
	}
	
	/**
	 * 
	 *  Description:
	 *  获取容器
	 *  @author liyuanyi  DateTime 2016年1月16日 下午4:21:08
	 *  @return
	 */
	private static ApplicationContext getSpringContext() {
		return ContextLoader.getCurrentWebApplicationContext();
	}

	
	public static String getIPAddress(boolean useIPv4)
	{
		try
		{
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces)
			{
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs)
				{
					if (addr.isSiteLocalAddress() && !addr.isLoopbackAddress() && addr.getHostAddress().indexOf(":") == -1)
					{
						String sAddr = addr.getHostAddress();
						//boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						boolean isIPv4 = sAddr.indexOf(':') < 0;

						if (useIPv4)
						{
							if (isIPv4)
								return sAddr;
						}
						else
						{
							if (!isIPv4)
							{
								int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
								return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
							}
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
		}
		return "127.0.0.1";
	}
}
