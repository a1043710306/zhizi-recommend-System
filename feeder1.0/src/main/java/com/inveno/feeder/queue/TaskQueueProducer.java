package com.inveno.feeder.queue;

public interface TaskQueueProducer
{
	public static final String QUEUE_NOTIFY_HONEYBEE = "task_remove_cache";
	public static final String QUEUE_NOTIFY_FEEDER   = "task_feeder_info";
	public static final String ARTICLE_GMP = "article-gmp";


	public Object send(String queue, String msg);
}