package com.inveno.common.factory;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class PooledThreadFactory extends BasePooledObjectFactory<TTransport> {
	public String host;
	
	public int port;
	
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

	@Override
	public TTransport create() throws Exception {
		TTransport transport = new TFramedTransport(new TSocket(host,
				port));
		transport.open();
		return transport;
	}

	@Override
	public PooledObject<TTransport> wrap(TTransport transport) {
		return new DefaultPooledObject<TTransport>(transport);
	}

	@Override
	public void destroyObject(PooledObject<TTransport> p) throws Exception {
		p.getObject().close();
	}

}
