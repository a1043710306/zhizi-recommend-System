package com.inveno.common.factory;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.inveno.thrift.UfsService;

public class UfsClientPooledThreadFactory extends BasePooledObjectFactory<TServiceClient>{

	private String host;

	private int port;

	private int timeout;

	@Override
	public TServiceClient create() throws Exception {
		TTransport transport = new TSocket(host, port, timeout);
		TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
		transport.open();
		return new UfsService.Client(protocol);
	}

	@Override
	public void destroyObject(PooledObject<TServiceClient> p) throws Exception {
		TServiceClient client = p.getObject();
		TTransport transportInput  = client.getInputProtocol().getTransport();
		if (transportInput != null && transportInput.isOpen()) {
			transportInput.close();
		}
		TTransport transportOutput = client.getOutputProtocol().getTransport();
		if (transportOutput != null && transportOutput.isOpen()) {
			transportOutput.close();
		}
	}

	@Override
	public boolean validateObject(PooledObject<TServiceClient> p) {
		TServiceClient client = p.getObject();
		TTransport transportInput  = client.getInputProtocol().getTransport();
		TTransport transportOutput = client.getOutputProtocol().getTransport();
		return (transportInput.isOpen() && transportOutput.isOpen());
	}

	@Override
	public PooledObject<TServiceClient> wrap(TServiceClient arg0) {
		return new DefaultPooledObject<TServiceClient>(arg0);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
