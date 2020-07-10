package com.inveno.common.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * JAVA读取properties属性配置文件公共类
 * 
 */
@SuppressWarnings("unchecked")
public class PropertyUtils {
	/*
	 * 声明要读取的配置文件
	 */
	private static String filePath = "/src/plmvc-config.properties";
	//private static String filePath = "/sso.properties";
	
	/*
	 * 缓存起来
	 */
	@SuppressWarnings("rawtypes")
	private static Map propertiesMap = new HashMap<String, String>(15);

	/*
	 * 单例，不允许创建实例 
	 */
	protected PropertyUtils() {
	}
	
	/**
	 * 默认路径是/src/inveno.properties，
	 * <p>Title: PropertyUtils.java</p>
	 * <p>Company: invneo</p>
	 * @author wangdesheng
	 * @date 2014年8月8日
	 * @version 1.0
	 * @param path
	 */
	public static void setFilePath(String path){
		filePath = path;
	}
	
	public static void setFilePathOfOut(String path){
		filePath = System.getProperty("user.dir") + path;
	}

	/**
	 * 读取内部文件，根据键取出对应的值
	 * <p>Title: PropertyUtils.java</p>
	 * <p>Company: invneo</p>
	 * @author wangdesheng
	 * @date 2014年7月12日
	 * @version 1.0
	 * @param key
	 * @param fileName
	 * @return
	 */
	public static String getProperty(String key, String fileName) {
		String strValue = null;
		StringBuffer sb = new StringBuffer(50);
		sb.append(fileName).append(".").append(key);
		String tempKey = sb.toString();
		strValue = (String) propertiesMap.get(tempKey);
		if (StringUtils.isNotEmpty(strValue)) {
			return strValue;
		}
		InputStream inputStream = null;
		try {
			inputStream = PropertyUtils.class.getResourceAsStream(fileName);
			Properties properties = new Properties();
			properties.load(inputStream);
			strValue = properties.getProperty(key);
			propertiesMap.put(tempKey, strValue);
		} catch (IOException ie) {
			ie.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return strValue;
	}

	/**
	 * 读取外部文件，根据键取出对应的值
	 * <p>Title: PropertyUtils.java</p>
	 * <p>Company: invneo</p>
	 * @author wangdesheng
	 * @date 2014年8月8日
	 * @version 1.0
	 * @param key
	 * @param fileName
	 * @return
	 */
	public static String getPropertyOfOut(String key, String fileName) {
		String strValue = null;
		StringBuffer sb = new StringBuffer(50);
		sb.append(fileName).append(".").append(key);
		String tempKey = sb.toString();
		strValue = (String) propertiesMap.get(tempKey);
		if (StringUtils.isNotEmpty(strValue)) {
			return strValue;
		}
		InputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(fileName));
			Properties properties = new Properties();
			properties.load(inputStream);
			strValue = properties.getProperty(key);
			propertiesMap.put(tempKey, strValue);
		} catch (IOException ie) {
			ie.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return strValue;
	}
	
	/**
	 * 读取内部文件根据key值得到value
	 * <p>Title: PropertyUtils.java</p>
	 * <p>Company: invneo</p>
	 * @author wangdesheng
	 * @date 2014年7月12日
	 * @version 1.0
	 * @param key
	 * @return
	 */
	public static String getProperty(String key) {
		return getProperty(key, filePath);
	}
	
	/**
	 * 读取外部文件根据key值得到value
	 * <p>Title: PropertyUtils.java</p>
	 * <p>Company: invneo</p>
	 * @author wangdesheng
	 * @date 2014年8月8日
	 * @version 1.0
	 * @param key
	 * @return
	 */
	public static String getPropertyOfOut(String key) {
		return getPropertyOfOut(key, filePath);
	}

	public static String getFilePath() {
		return filePath;
	}
	
	
}
