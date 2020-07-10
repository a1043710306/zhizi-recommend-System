
package com.inveno.sire.threadpool.fixed;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.inveno.sire.threadpool.AbortPolicyWithReport;
import com.inveno.sire.threadpool.NamedThreadFactory;
import com.inveno.sire.threadpool.ThreadPool;
import org.springframework.beans.factory.InitializingBean;



/**
* Created by Klaus Liu on 2016/9/20.
* 此线程池启动时即创建固定大小的线程数，不做任何伸缩
*/
public class FixedThreadPool implements ThreadPool, InitializingBean {
	
	//private static Logger logger = LoggerFactory.getLogger(FixedThreadPool.class);

	private String name  = "SIRE";

	private int threads = 20;

	private int queues = 200;

	private RejectedExecutionHandler rejectedHandler = new AbortPolicyWithReport(name);

	private ThreadFactory threadFactory = new NamedThreadFactory(name, true);

	private Executor executor;

	public void initialize() {

		executor = new ThreadPoolExecutor(threads, threads, 0,
				TimeUnit.MILLISECONDS,
				queues == 0 ? new SynchronousQueue<Runnable>()
						: (queues < 0 ? new LinkedBlockingQueue<Runnable>()
								: new LinkedBlockingQueue<Runnable>(queues)),
								threadFactory, rejectedHandler);
	}

	public Executor getExecutor() {
		return executor;
	}

	public void execute(Runnable command) {
		executor.execute(command);
	}

	public void afterPropertiesSet() throws Exception {
		initialize();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public int getQueues() {
		return queues;
	}

	public void setQueues(int queues) {
		this.queues = queues;
	}

	public RejectedExecutionHandler getRejectedHandler() {
		return rejectedHandler;
	}

	public void setRejectedHandler(RejectedExecutionHandler rejectedHandler) {
		this.rejectedHandler = rejectedHandler;
	}

	public ThreadFactory getThreadFactory() {
		return threadFactory;
	}

	public void setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}
	
	

}