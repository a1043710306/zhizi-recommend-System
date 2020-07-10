package com.inveno.core.util;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.qihoo.qconf.Qconf;
import net.qihoo.qconf.QconfException;

public class QConfUtil {

	public static Log logger = LogFactory.getLog(QConfUtil.class);

	public static String getStringQconfCfg(String path, String uid) {
		return getQconfCfg(path, uid);
	}

	public static double getDoubleQconfCfg(String path, String uid) {
		return Double.parseDouble(getQconfCfg(path, uid));

	}

	public static int getIntergerQconfCfg(String path, String uid) {
		return Integer.parseInt(getQconfCfg(path, uid));
	}

	public static boolean getBooleanQconfCfg(String path, String uid) {
		return Boolean.parseBoolean(getQconfCfg(path, uid));

	}

	public static ArrayList<String> getBatchKeys(String path) {
		if (!System.getProperties().getProperty("os.name").startsWith("Windows"))
		{
			try
			{
				return Qconf.getBatchKeys(path);
			}
			catch (Exception e)
			{
				logger.error("=== get qconf error,has no "+ path +" config", e);
			}
		}
		return null;
	}

	public static Map<String, String> getBatchConf(String path) {
		return getBatchConf(path, true);
	}
	public static Map<String, String> getBatchConf(String strBasePath, boolean bRecursive)
	{
		Map<String, String> mConfiguration = new HashMap<String, String>();
		if (!System.getProperties().getProperty("os.name").startsWith("Windows"))
		{
			try
			{
				if (bRecursive)
				{
					ArrayList<String> alKeys = Qconf.getBatchKeys(strBasePath);
					if (CollectionUtils.isNotEmpty(alKeys))
					{
						for (String strKeyPrefix : alKeys)
						{
							String strTargetPath = strBasePath + "/" + strKeyPrefix;
							ArrayList<String> alTargetKeys = Qconf.getBatchKeys(strTargetPath);
							if (CollectionUtils.isNotEmpty(alTargetKeys))
							{
								Map<String, String> mKeyConfiguration = getBatchConf(strTargetPath, bRecursive);
								for (Map.Entry<String, String> entry : mKeyConfiguration.entrySet())
								{
									String strKey = strKeyPrefix + "/" + entry.getKey();
									mConfiguration.put(strKey, entry.getValue());
								}
							}
							else
							{
								mConfiguration.put(strKeyPrefix, Qconf.getConf(strTargetPath));
							}
						}
					}
				}
				else
				{
					mConfiguration = Qconf.getBatchConf(strBasePath);
				}
			}
			catch (Exception e)
			{
				logger.error("=== get qconf error,has no "+ strBasePath +" config", e);
			}
		}
		return mConfiguration;
	}
	private static String getQconfCfg(String path, String uid) {
		if (!System.getProperties().getProperty("os.name").startsWith("Windows")) {
			try {
				return Qconf.getConf(path);
			} catch (QconfException e) {
				logger.error("=== get qconf error,has no "+ path +" config ,and uid is " + uid + "path ", e);
			}
		}
		return null;
	}
}
