package com.inveno.common.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransport;

public class TSocketFactory extends BasePooledObjectFactory<TSocket> {

    private static Log log = LogFactory.getLog("BasePooledObjectFactory");

    private String host;
    private int port;
    private int timeout;


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
    public TSocket create() throws Exception {
        TSocket tSocket = new TSocket(host, port, timeout);
        tSocket.open();
        log.debug(Thread.currentThread().getName() + " TSocketFactory create connection");
        return tSocket;
    }

    public PooledObject<TSocket> makeObject() throws Exception {  
        log.debug(Thread.currentThread().getName() + " makeObject");
        return wrap(create());  
    }  


    @Override
    public PooledObject<TSocket> wrap(TSocket socket) {
        log.debug(Thread.currentThread().getName() + " TSocketFactory wrap connection");
        return new DefaultPooledObject<>(socket);
    }

    /**
     * 对象钝化(即：从激活状态转入非激活状态，returnObject时触发）
     *
     * @param pooledObject
     * @throws Exception
     */
    @Override
    public void passivateObject(PooledObject<TSocket> pooledObject) throws Exception {
        log.debug(Thread.currentThread().getName() + " TSocketFactory passivateObject[" + pooledObject.getObject() + "]");
    }


    /**
     * 对象激活(borrowObject时触发）
     *
     * @param pooledObject
     * @throws Exception
     */
    @Override
    public void activateObject(PooledObject<TSocket> pooledObject) throws Exception {
        log.debug(Thread.currentThread().getName() + " TSocketFactory activateObject [" + pooledObject.getObject() + "]");
        if (pooledObject.getObject() != null) {
            if (!pooledObject.getObject().isOpen()) {
                try {
                    pooledObject.getObject().open();
                    log.debug(Thread.currentThread().getName() + " TSocketFactory getObject [" + pooledObject.getObject() + "] is reopened");
                } catch (Exception e) {
                    log.debug(Thread.currentThread().getName() + " TSocketFactory getObject [" + pooledObject.getObject() + "] is reopen failed");
                }
            } else {
                log.debug(Thread.currentThread().getName() + " TSocketFactory getObject [" + pooledObject.getObject() + "] is already opened");
            }
            try {
                //try to check remote connection;
                pooledObject.getObject().getSocket().sendUrgentData(0xFF);
                log.debug(Thread.currentThread().getName() + " TSocketFactory getObject [" + pooledObject.getObject() + "] is test ok");
            } catch (Exception e) {
                pooledObject = makeObject();
                log.debug(Thread.currentThread().getName() + " TSocketFactory getObject [" + pooledObject.getObject() + "] is remade");
            }
        } else {
            pooledObject = makeObject();
            log.debug(Thread.currentThread().getName() + " TSocketFactory getObject [" + pooledObject.getObject() + "] is remade");
        }
    }


    /**
     * 对象销毁(clear时会触发）
     * @param pooledObject
     * @throws Exception
     */
    @Override
    public void destroyObject(PooledObject<TSocket> pooledObject) throws Exception {
        log.debug(Thread.currentThread().getName() + " TSocketFactory destroyObject[" + pooledObject.getObject() + "]");
        if (pooledObject.getObject() != null && pooledObject.getObject().isOpen()) {
            pooledObject.getObject().flush();
            pooledObject.getObject().close();
        }
    }


    /**
     * 验证对象有效性
     *
     * @param pooledObject
     * @return
     */
    @Override
    public boolean validateObject(PooledObject<TSocket> pooledObject) {
        boolean bValidate = false;
        if (pooledObject.getObject() != null) {
            if (pooledObject.getObject().isOpen()) {
                return true;
            } else {
                try {
                    pooledObject.getObject().open();
                    bValidate = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        log.debug(Thread.currentThread().getName() + " TSocketFactory validateObject :" + bValidate);
        return bValidate;
    }
}