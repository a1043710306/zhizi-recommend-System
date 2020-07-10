package com.inveno.server.contentgroup.queue;

public interface TaskQueueProducer
{
	public static final String QUEUE_NOTIFY_HONEYBEE = "task_remove_cache";
	public static final String QUEUE_NOTIFY_FEEDER   = "task_feeder_info";

	public Object send(String queue, String msg);
}