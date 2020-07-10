package com.inveno.server.contentgroup.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class SystemUtils
{
	public static String getIPAddress(boolean useIPv4)
	{
		try
		{
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces)
			{
				if (!intf.getName().equalsIgnoreCase("eth0"))
					continue;

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

	public static void doGarbageCollection()
	{
		collectGarbage();
		collectGarbage();
	}

	public static void collectGarbage()
	{
		try {
			System.gc();
			// Thread.currentThread();
			Thread.sleep(100);
			System.runFinalization();
			// Thread.currentThread();
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
}