package com.inveno.common.factory;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class TProtocolFactory extends BasePooledObjectFactory<TProtocol> {
    private String host;
    private int port;
    private int timeout;

    private boolean keepAlive = true;

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

    @Override
    public TProtocol create() throws TTransportException {
        TSocket tSocket = new TSocket(host, port, timeout);
        TTransport tTransport = new TFramedTransport(tSocket);
        tTransport.open();
        return new TBinaryProtocol(tTransport);
    }


    @Override
    public PooledObject<TProtocol> wrap(TProtocol protocol) {
        return new DefaultPooledObject<>(protocol);
    }

    /**
     * 对象钝化(即：从激活状态转入非激活状态，returnObject时触发）
     *
     * @param pooledObject
     * @throws TTransportException
     */
    @Override
    public void passivateObject(PooledObject<TProtocol> pooledObject) throws TTransportException {
        if (!keepAlive) {
            pooledObject.getObject().getTransport().flush();
            pooledObject.getObject().getTransport().close();
        }
    }


    /**
     * 对象激活(borrowObject时触发）
     *
     * @param pooledObject
     * @throws TTransportException
     */
    @Override
    public void activateObject(PooledObject<TProtocol> pooledObject) throws TTransportException {
        if (!pooledObject.getObject().getTransport().isOpen()) {
            pooledObject.getObject().getTransport().open();
        }
    }


    /**
     * 对象销毁(clear时会触发）
     * @param pooledObject
     * @throws TTransportException
     */
    @Override
    public void destroyObject(PooledObject<TProtocol> pooledObject) throws TTransportException {
        if (pooledObject.getObject().getTransport() != null && pooledObject.getObject().getTransport().isOpen()) {
            pooledObject.getObject().getTransport().close();
        }
    }


    /**
     * 验证对象有效性
     *
     * @param p
     * @return
     */
    @Override
    public boolean validateObject(PooledObject<TProtocol> p) {
        if (p.getObject() != null) {
            if (p.getObject().getTransport().isOpen()) {
                return true;
            }
            try {
                p.getObject().getTransport().open();
                return true;
            } catch (TTransportException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}