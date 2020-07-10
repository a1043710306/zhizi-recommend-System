package com.inveno.common.factory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.springframework.cglib.proxy.InvocationHandler;

public class BinaryPooledThreadFactory extends BasePooledObjectFactory<TProtocol> {
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
	public TProtocol create() throws Exception {
		TTransport transport = new TSocket(host,
				port);
		TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
		transport.open();
		return protocol;
	}

	 

	@Override
	public void destroyObject(PooledObject<TProtocol> p) throws Exception {
		p.getObject().getTransport().close();
	}

	@Override
	public PooledObject<TProtocol> wrap(TProtocol arg0) {
 		return new DefaultPooledObject<TProtocol>(arg0);
	}

}
