package com.inveno.common.filter;

import java.util.List;

/**
 * 
 *  Class Name: InfoFilter.java
 *  Description: 
 *  过滤器，过滤不符合的资讯
 *  
 *  @author liyuanyi  DateTime 2016年1月19日 下午5:03:59 
 *  @company inveno 
 *  @version 1.0
 */
public interface InfoFilter {
	
	List<String> filter(List<String> ids); 

}
