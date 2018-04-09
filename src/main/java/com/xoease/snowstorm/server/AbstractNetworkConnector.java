package com.xoease.snowstorm.server;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.thread.Scheduler;

public abstract class AbstractNetworkConnector extends AbstractConnector implements NetworkConnector
{

    private volatile String _host;
    private volatile int _port = 0;

    public AbstractNetworkConnector(SnowAbstractServer server, Executor executor, Scheduler scheduler, ByteBufferPool pool, int acceptors, ConnectionFactory... factories)
    {
        super(server,executor,scheduler,pool,acceptors,factories);
    }

    public void setHost(String host)
    {
        _host = host;
    }

    @Override
    @ManagedAttribute("The network interface this connector binds to as an IP address or a hostname.  If null or 0.0.0.0, then bind to all interfaces.")
    public String getHost()
    {
        return _host;
    }

    public void setPort(int port)
    {
        _port = port;
    }

    @Override
    @ManagedAttribute("Port this connector listens on. If set the 0 a random port is assigned which may be obtained with getLocalPort()")
    public int getPort()
    {
        return _port;
    }

    @Override
    public int getLocalPort()
    {
        return -1;
    }

    @Override
    protected void doStart() throws Exception
    {
        open();
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception
    {
        close();
        super.doStop();
    }

    @Override
    public void open() throws IOException
    {
    }

    @Override
    public void close()
    {
        // Interrupting is often sufficient to close the channel
        interruptAcceptors();
    }
    

    @Override
    public Future<Void> shutdown()
    {
        close();
        return super.shutdown();
    }

    @Override
    protected boolean isAccepting()
    {
        return super.isAccepting() && isOpen();
    }

    @Override
    public String toString()
    {
        return String.format("%s{%s:%d}",
                super.toString(),
                getHost() == null ? "0.0.0.0" : getHost(),
                getLocalPort() <= 0 ? getPort() : getLocalPort());
    }
}
