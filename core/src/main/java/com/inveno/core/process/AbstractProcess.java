package com.inveno.core.process;

import java.util.List;

public abstract class   AbstractProcess<T,R> {
	
	protected R r ; 
	
	 public R getR() {
		return r;
	}

	public void setR(R r) {
		this.r = r;
	}
	/**
	 * 
	  * initProcess(初选)
 	  * @Title: initProcess
 	  * @param @return    设定文件
	  * @return List<String>    返回类型
	  * @throws
	 */
	public abstract List<T> initProcess(R r);
	
 	
	/**
	 * 
	  * beforeProcess(前置处理)
 	  * @Title: beforeProcess
 	  * @param @return    设定文件
	  * @return List<String>    返回类型
	  * @throws
	 */
	public abstract List<T> beforeProcess(List<T> list,R r);
	
	
	/**
	 * 
	  * modelProcess(算法处理)
 	  * @Title: modelProcess
 	  * @param @return    设定文件
	  * @return List<String>    返回类型
	  * @throws
	 */
	public abstract List<T> modelProcess(List<T> list,R r);
	
	/**
	 * 
	  * afterProcess(后处理)
 	  * @Title: afterProcess
 	  * @param @return    设定文件
	  * @return List<String>    返回类型
	  * @throws
	 */
	public abstract List<T> afterProcess(List<T> list,R r);
	
	
	/**
	 * 
	  * strategyProcess(最后策略处理)
 	  * @Title: strategyProcess
 	  * @param @return    设定文件
	  * @return List<String>    返回类型
	  * @throws
	 */
	public abstract List<T> strategyProcess(List<T> list,R r);
	

	/**
	  * 
 	   * @Title: process
	   * @Description: 总的处理流程
	   * @param     设定文件
	   * @return void    返回类型
	   * @throws
	  */
	public List<T> process() {
		List<T> list = initProcess(r);
		list =  beforeProcess(list,r);
		//list =  modelProcess(list,r);
		//list =  afterProcess(list,r);
		//list =  strategyProcess(list,r);
		return list;
	}
 	

}
