package com.albedo.java.thrift.rpc.client.thrift;

import com.albedo.java.thrift.rpc.client.manage.Server;
import com.albedo.java.thrift.rpc.common.PoolOperationCallBack;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 连接池,thrift-client for spring
 */
public class ThriftClientPoolFactory extends BasePoolableObjectFactory<TServiceClient> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private final String proccessName;
    private final Server server;
    private final TServiceClientFactory<TServiceClient> clientFactory;
    private PoolOperationCallBack callback;


    public ThriftClientPoolFactory(Server server, TServiceClientFactory<TServiceClient> clientFactory,
                                   PoolOperationCallBack callback, String proccessName) throws Exception {
        this.server = server;
        this.clientFactory = clientFactory;
        this.callback = callback;
        this.proccessName = proccessName;
    }

    @Override
    public void destroyObject(TServiceClient client) throws Exception {
        if (callback != null) {
            try {
                callback.destroy(client);
            } catch (Exception e) {
                logger.warn("destroyObject:{}", e);
            }
        }
        logger.info("destroyObject:{}", client);
        TTransport pin = client.getInputProtocol().getTransport();
        pin.close();
        TTransport pout = client.getOutputProtocol().getTransport();
        pout.close();
    }

    @Override
    public void activateObject(TServiceClient client) throws Exception {
    }

    @Override
    public void passivateObject(TServiceClient client) throws Exception {
    }

    @Override
    public boolean validateObject(TServiceClient client) {
        TTransport pin = client.getInputProtocol().getTransport();
        logger.info("validateObject input:{}", pin.isOpen());
        TTransport pout = client.getOutputProtocol().getTransport();
        logger.info("validateObject output:{}", pout.isOpen());
        return pin.isOpen() && pout.isOpen();
    }

    @Override
    public TServiceClient makeObject() throws Exception {
        TSocket tsocket = new TSocket(server.getHost(), server.getPort());
        tsocket.open();
//        TTransport transport = new TFramedTransport(tsocket);
        TJSONProtocol protocol = new TJSONProtocol(tsocket);
        TMultiplexedProtocol uProtocol=new TMultiplexedProtocol(protocol, proccessName);
        TServiceClient client = this.clientFactory.getClient(uProtocol);
        if (callback != null) {
            try {
                callback.make(client);
            } catch (Exception e) {
                logger.warn("makeObject:{}", e);
            }
        }
        return client;
    }

}
