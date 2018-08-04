package com.xoease.center;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;

import java.net.InetSocketAddress;

/**
 * @see  spring-boot
 */
@Deprecated
public abstract class Server  {
    public Server(int  port) throws TTransportException {
        this(new InetSocketAddress(port));
    }
    private TServer ser;
    public Server(InetSocketAddress address) throws TTransportException {
        TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(address);
        TThreadedSelectorServer.Args serverParams= new TThreadedSelectorServer.Args(serverTransport);
        serverParams.protocolFactory(this.protocolFactory());
        serverParams.processor(processor());
        ser=new TThreadedSelectorServer(serverParams);
    }
    public abstract TProcessor processor();
    public abstract TProtocolFactory protocolFactory();
    public void start(){
        ser.serve();
    }
}
