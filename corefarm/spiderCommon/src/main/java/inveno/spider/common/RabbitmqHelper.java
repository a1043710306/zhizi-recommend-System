package inveno.spider.common;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

import inveno.spider.common.utils.ModelBuilder;
import inveno.spider.common.utils.NotMapModelBuilder;

public class RabbitmqHelper {

	//private static final Logger LOG = Logger.getLogger(RabbitmqHelper.class);

	public synchronized static RabbitmqHelper getInstance() {
		if (null == instance) {
			//instance = new RabbitmqHelper();
		}
		return instance;
	}

	private ConnectionFactory factory = null;

	private Connection connection = null;

	private static volatile RabbitmqHelper instance = null;

	private RabbitmqHelper() {
		/*try {
			if (factory == null) {
				factory = new ConnectionFactory();
//				factory.setHost(Constants.RABBIT_HOST);
				factory.setPort(Constants.RABBIT_PORT);
				factory.setUsername(Constants.RABBIT_USERNAME);
				factory.setPassword(Constants.RABBIT_PASSWORD);
				factory.setAutomaticRecoveryEnabled(true);

//				connection = factory.newConnection();
				connection = factory.newConnection(Address.parseAddresses(Constants.RABBIT_HOST));
			}
		} catch (Exception e) {
			LOG.error("newConnection has exception:" + e);
		}*/
	}
	
	public static void close(Channel channel, Connection connection) {
		/*try {
			if (channel != null) {
				channel.close();
			}
			if (null != connection) {
				connection.close();
			}
		} catch (Exception e) {
			LOG.error("release resources has exception:" + e);
		}*/
	}

	public void close() {
		/*if (null != connection) {
			try {
				connection.close();
			} catch (Exception e) {
				LOG.error("close connection has exception:" + e);
			}
		}*/
	}

	public void close(Channel channel) {
		/*try {
			if (channel != null) {
				channel.close();
			}
		} catch (Exception e) {
			LOG.error("close channel has exception:" + e);
		}*/
	}

	private Channel getChannel() {
		/*Channel channel = null;
		if (null != connection) {
			try {
				channel = connection.createChannel();
			} catch (Exception e) {
				LOG.error("createChannel has exception:" + e);
			}
		}
		return channel;*/
		return null;
	}
	
	/**
	 *   从mq中获取数据，并序列化为已经存在的model的类对象
	 * @param queueName
	 * @param clazz 
	 * @return class
	 */
	public <T extends Serializable> T getMessageNonblocking(String queueName, Class<T> clazz) {
		/*Channel channel = getChannel();
		T result = null;
		String text = "";
		GetResponse response = null;
		try {
			channel.queueDeclare(queueName, true, false, false, null);
			response = channel.basicGet(queueName, false);
			if (response != null) {
				byte[] data = response.getBody();
				text = new String(data, "UTF-8");
				result = ModelBuilder.getInstance().build(text, clazz);
				LOG.debug("Get message[" + queueName + "]:" + result);
				channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
			}
		} catch (Exception e) {
			LOG.error("get mesg [" + text + "] happen error!" + e);
			try {
				channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
			} catch (IOException e1) {
				LOG.error("basicAck has exception:" + e);
			}
		} finally {
			close(channel);
		}

		return result;*/
		return null;
	}

	/**
	 * 从mq中获取数据，并转化为自定义类的对象
	 * @param queueName
	 * @param clazz
	 * @return class
	 */
	public <T extends Serializable> T getMessageOtherNonblocking(String queueName, Class<T> clazz) {
		/*Channel channel = getChannel();
		T result = null;
		String text = "";
		GetResponse response = null;
		try {
			channel.queueDeclare(queueName, true, false, false, null);
			response = channel.basicGet(queueName, false);
			if (response != null) {
				byte[] data = response.getBody();

				text = new String(data, "UTF-8");
				result = NotMapModelBuilder.getInstance().build(text, clazz);
				LOG.debug("Get message[" + queueName + "]:" + result + "-----original text:" + text);
				channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
			}
		} catch (Exception e) {
			LOG.error("get mesg [" + text + "] happen error! e:", e);
			try {
				channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
			} catch (IOException e1) {
				LOG.error("basicAck happen error! e:" + e1);
			}
		} finally {
			close(channel);
		}

		return result;*/
		return null;
	}

	/**
	 * Get messages of specified number,if that queue is empty,then it will
	 * waiting until timeout(default:10 secs).
	 * 从rabbit mq获取固定数量数据，并返回一个已存在model的类的list对像
	 * @param queueName
	 * @param count
	 * @param clazz
	 * @return list
	 */
	public <T extends Serializable> List<T> getMessagesNonblocking(String queueName, int count, Class<T> clazz) {
		/*int i = 0;
		List<T> list = new ArrayList<T>();
		Channel channel = getChannel();
		try {
			channel.queueDeclare(queueName, true, false, false, null);
			// long start = System.currentTimeMillis();
			//从mq中取count条数据存入list
			while (i < count) {

				GetResponse response = channel.basicGet(queueName, false);
				if (response != null) {
					byte[] data = response.getBody();

					String text = new String(data, "UTF-8");
					T object = ModelBuilder.getInstance().build(text, clazz);
					list.add(object);
					LOG.debug("Get message[" + queueName + "]:" + object);
					channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
					i++;
					continue;
				} else {
					break;
				}
			}
		} catch (Exception e) {
			LOG.error("get mesg happen error! e:" + e);
		} finally {
			close(channel);
		}

		return list;*/
		return null;
	}
	
	/**
	 * 从rabbitmq中 获取一个队列中的count
	 * @param queueName
	 * @return
	 */
	public int getCount(String queueName) {
		/*DeclareOk dok = null;
		Channel channel = null;
		int count = 0;
		try {
			channel = getChannel();
			dok = channel.queueDeclare(queueName, true, false, false, null);
			count = dok.getMessageCount();
		} catch (Exception e) {
			LOG.error("get count has exception:" + e);
		} finally {
			close(channel);
		}

		return count;*/
		return 0;
	}

	/**
	 * 从rabbitmq中获取一条信息
	 * @param queueName
	 * @return string
	 */
	public String getMessage(String queueName) {
		/*Channel channel = null;
		String object = null;
		try {
			channel = getChannel();
			channel.queueDeclare(queueName, true, false, false, null);

			channel.basicQos(1);
			QueueingConsumer consumer = new QueueingConsumer(channel);
			boolean autoAck = false;
			channel.basicConsume(queueName, autoAck, consumer);

			Delivery delivery = consumer.nextDelivery();

			byte[] data = delivery.getBody();
			String text = new String(data, "UTF-8");
			object = text;

			LOG.debug("Get message[" + queueName + "]:" + object);
			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		} catch (Exception e) {
			LOG.error("get message has exception:" + e);
		} finally {
			close(channel);
		}

		return object;*/
		return null;
	}
	
	/**
	 * 从rabbitmq中获取一个类对象
	 * @param queueName
	 * @param clazz
	 * @return class
	 */
	public <T extends Serializable> T getMessage(String queueName, Class<T> clazz) {
		/*Channel channel = null;
		T object = null;
		try {
			channel = getChannel();
			channel.queueDeclare(queueName, true, false, false, null);

			channel.basicQos(1);
			QueueingConsumer consumer = new QueueingConsumer(channel);
			boolean autoAck = false;
			channel.basicConsume(queueName, autoAck, consumer);

			Delivery delivery = consumer.nextDelivery();

			byte[] data = delivery.getBody();
			String text = new String(data, "UTF-8");
			object = ModelBuilder.getInstance().build(text, clazz);

			LOG.debug("Get message[" + queueName + "]:" + object);
			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		} catch (Exception e) {
			LOG.error("get message has exception:" + e);
		} finally {
			close(channel);
		}*/

		return null;
	}

	/**
	 * 发送一个list对象到mq中
	 * @param queue
	 * @param list
	 */
	public <T extends Serializable> void sendMessages(String queue, List<T> list) {
	/*	if (null == list || list.size() == 0) {
			return;
		}
		Channel channel = getChannel();
		try {
			if (null != channel) {
				for (T object : list) {
					String tmp = ModelBuilder.getInstance().toJson(object);// JSON.toJSONString(object,SerializerFeature.WriteEnumUsingToString);
					byte[] data = tmp.getBytes("UTF-8");
					channel.queueDeclare(queue, true, false, false, null);
					channel.basicPublish("", queue, null, data);
				}
			}
		} catch (Exception e) {
			LOG.error("send msg has exception:" + e);
		} finally {
			close(channel);
		}*/
	}

	/**
	 * 发送一个class对象到mq中
	 * @param queue
	 * @param object
	 */
	public <T extends Serializable> void sendMessage(String queue, T object) {
		/*Channel channel = getChannel();
		try {
			if (null != channel) {
				// String tmp = JSON.toJSONString(object);
				String tmp = ModelBuilder.getInstance().toJson(object);
				byte[] data = tmp.getBytes("UTF-8");

				channel.queueDeclare(queue, true, false, false, null);
				channel.basicPublish("", queue, null, data);
			}
		} catch (Exception e) {
			LOG.error("send msg has exception:", e);
		} finally {
			close(channel);
		}*/
	}
	
	/**
	 * 发送一个字符串到 mq中
	 * @param queue
	 * @param json
	 */
	public <T extends Serializable> void sendMessage(String queue, String json) {
		/*Channel channel = getChannel();
		try {
			if (null != channel) {
				byte[] data = json.getBytes("UTF-8");

				channel.queueDeclare(queue, true, false, false, null);
				channel.basicPublish("", queue, null, data);
			}
		} catch (Exception e) {
			LOG.error("send msg has exception:" + e);
		} finally {
			close(channel);
		}*/
	}

}
