/**
 * 
 */
package com.inveno.sire.threadpool;

import java.util.concurrent.Executor;


public interface ThreadPool {
	
	 Executor getExecutor();
	
	 void execute(Runnable command);

}
