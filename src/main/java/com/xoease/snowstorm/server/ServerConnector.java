package com.xoease.snowstorm.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.ManagedSelector;
import org.eclipse.jetty.io.SelectChannelEndPoint;
import org.eclipse.jetty.io.SelectorManager;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.Name;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.Scheduler;

public class ServerConnector extends AbstractNetworkConnector
{
    private final SelectorManager _manager;
    private volatile ServerSocketChannel _acceptChannel;
    private volatile boolean _inheritChannel = false;
    private volatile int _localPort = -1;
    private volatile int _acceptQueueSize = 0;
    private volatile boolean _reuseAddress = true;
    private volatile int _lingerTime = -1;

    /** Generic Server Connection.
     * @param server    
     *          The server this connector will be accept connection for.  
     * @param executor  
     *          An executor used to run tasks for handling requests, acceptors and selectors.
     *          If null then use the servers executor
     * @param scheduler 
     *          A scheduler used to schedule timeouts. If null then use the servers scheduler
     * @param bufferPool
     *          A ByteBuffer pool used to allocate buffers.  If null then create a private pool with default configuration.
     * @param acceptors 
     *          the number of acceptor threads to use, or -1 for a default value. Acceptors accept new TCP/IP connections.  If 0, then 
     *          the selector threads are used to accept connections.
     * @param selectors
     *          the number of selector threads, or &lt;=0 for a default value. Selectors notice and schedule established connection that can make IO progress.
     * @param factories 
     *          Zero or more {@link ConnectionFactory} instances used to create and configure connections.
     */
    public ServerConnector(
        @Name("server") SnowAbstractServer server,
        @Name("executor") Executor executor,
        @Name("scheduler") Scheduler scheduler,
        @Name("bufferPool") ByteBufferPool bufferPool,
        @Name("acceptors") int acceptors,
        @Name("selectors") int selectors,
        @Name("factories") ConnectionFactory... factories)
    {
        super(server,executor,scheduler,bufferPool,acceptors,factories);
        _manager = newSelectorManager(getExecutor(), getScheduler(),
            selectors>0?selectors:Math.max(1,Math.min(4,Runtime.getRuntime().availableProcessors()/2)));
        addBean(_manager, true);
        setAcceptorPriorityDelta(-2);
    }

    protected SelectorManager newSelectorManager(Executor executor, Scheduler scheduler, int selectors)
    {
        return new ServerConnectorManager(executor, scheduler, selectors);
    }

    @Override
    protected void doStart() throws Exception
    {
        super.doStart();

        if (getAcceptors()==0)
        {
            _acceptChannel.configureBlocking(false);
            _manager.acceptor(_acceptChannel);
        }
    }

    @Override
    public boolean isOpen()
    {
        ServerSocketChannel channel = _acceptChannel;
        return channel!=null && channel.isOpen();
    }

   
    /**
     * @return whether this connector uses a channel inherited from the JVM.
     * @see System#inheritedChannel()
     */
    public boolean isInheritChannel()
    {
        return _inheritChannel;
    }

    /**
     * <p>Sets whether this connector uses a channel inherited from the JVM.</p>
     * <p>If true, the connector first tries to inherit from a channel provided by the system.
     * If there is no inherited channel available, or if the inherited channel is not usable,
     * then it will fall back using {@link ServerSocketChannel}.</p>
     * <p>Use it with xinetd/inetd, to launch an instance of Jetty on demand. The port
     * used to access pages on the Jetty instance is the same as the port used to
     * launch Jetty.</p>
     *
     * @param inheritChannel whether this connector uses a channel inherited from the JVM.
     */
    public void setInheritChannel(boolean inheritChannel)
    {
        _inheritChannel = inheritChannel;
    }

    @Override
    public void open() throws IOException
    {
        if (_acceptChannel == null)
        {
            ServerSocketChannel serverChannel = null;
            if (isInheritChannel())
            {
                Channel channel = System.inheritedChannel();
                if (channel instanceof ServerSocketChannel)
                    serverChannel = (ServerSocketChannel)channel;
                else
                    LOG.warn("Unable to use System.inheritedChannel() [{}]. Trying a new ServerSocketChannel at {}:{}", channel, getHost(), getPort());
            }

            if (serverChannel == null)
            {
                serverChannel = ServerSocketChannel.open();

                InetSocketAddress bindAddress = getHost() == null ? new InetSocketAddress(getPort()) : new InetSocketAddress(getHost(), getPort());
                serverChannel.socket().setReuseAddress(getReuseAddress());
                serverChannel.socket().bind(bindAddress, getAcceptQueueSize());

                _localPort = serverChannel.socket().getLocalPort();
                if (_localPort <= 0)
                    throw new IOException("Server channel not bound");

                addBean(serverChannel);
            }

            serverChannel.configureBlocking(true);
            addBean(serverChannel);

            _acceptChannel = serverChannel;
        }
    }

    @Override
    public Future<Void> shutdown()
    {
        // shutdown all the connections
        return super.shutdown();
    }

    @Override
    public void close()
    {
        ServerSocketChannel serverChannel = _acceptChannel;
        _acceptChannel = null;

        if (serverChannel != null)
        {
            removeBean(serverChannel);

            // If the interrupt did not close it, we should close it
            if (serverChannel.isOpen())
            {
                try
                {
                    serverChannel.close();
                }
                catch (IOException e)
                {
                    LOG.warn(e);
                }
            }
        }
        // super.close();
        _localPort = -2;
    }

    @Override
    public void accept(int acceptorID) throws IOException
    {
        ServerSocketChannel serverChannel = _acceptChannel;
        if (serverChannel != null && serverChannel.isOpen())
        {
            SocketChannel channel = serverChannel.accept();
            accepted(channel);
        }
    }
    
    private void accepted(SocketChannel channel) throws IOException
    {
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        configure(socket);
        _manager.accept(channel);
    }

    protected void configure(Socket socket)
    {
        try
        {
            socket.setTcpNoDelay(true);
            if (_lingerTime >= 0)
                socket.setSoLinger(true, _lingerTime / 1000);
            else
                socket.setSoLinger(false, 0);
        }
        catch (SocketException e)
        {
            LOG.ignore(e);
        }
    }

    public SelectorManager getSelectorManager()
    {
        return _manager;
    }

    @Override
    public Object getTransport()
    {
        return _acceptChannel;
    }

    @Override
    @ManagedAttribute("local port")
    public int getLocalPort()
    {
        return _localPort;
    }

    protected SelectChannelEndPoint newEndPoint(SocketChannel channel, ManagedSelector selectSet, SelectionKey key) throws IOException
    {
        return new SelectChannelEndPoint(channel, selectSet, key, getScheduler(), getIdleTimeout());
    }

    /**
     * @return the linger time
     * @see Socket#getSoLinger()
     */
    @ManagedAttribute("TCP/IP solinger time or -1 to disable")
    public int getSoLingerTime()
    {
        return _lingerTime;
    }

    /**
     * @param lingerTime the linger time. Use -1 to disable.
     * @see Socket#setSoLinger(boolean, int)
     */
    public void setSoLingerTime(int lingerTime)
    {
        _lingerTime = lingerTime;
    }

    /**
     * @return the accept queue size
     */
    @ManagedAttribute("Accept Queue size")
    public int getAcceptQueueSize()
    {
        return _acceptQueueSize;
    }

    /**
     * @param acceptQueueSize the accept queue size (also known as accept backlog)
     */
    public void setAcceptQueueSize(int acceptQueueSize)
    {
        _acceptQueueSize = acceptQueueSize;
    }

    /**
     * @return whether the server socket reuses addresses
     * @see ServerSocket#getReuseAddress()
     */
    public boolean getReuseAddress()
    {
        return _reuseAddress;
    }

    /**
     * @param reuseAddress whether the server socket reuses addresses
     * @see ServerSocket#setReuseAddress(boolean)
     */
    public void setReuseAddress(boolean reuseAddress)
    {
        _reuseAddress = reuseAddress;
    }

    protected class ServerConnectorManager extends SelectorManager
    {
        public ServerConnectorManager(Executor executor, Scheduler scheduler, int selectors)
        {
            super(executor, scheduler, selectors);
        }

        @Override
        protected void accepted(SocketChannel channel) throws IOException
        {
            ServerConnector.this.accepted(channel);
        }
@Override
public long getStopTimeout() {
	// TODO Auto-generated method stub
	return 1;
}

        @Override
        protected SelectChannelEndPoint newEndPoint(SocketChannel channel, ManagedSelector selectSet, SelectionKey selectionKey) throws IOException
        {
            return ServerConnector.this.newEndPoint(channel, selectSet, selectionKey);
        }

        @Override
        public Connection newConnection(SocketChannel channel, EndPoint endpoint, Object attachment) throws IOException
        {
            return getDefaultConnectionFactory().newConnection(ServerConnector.this, endpoint);
        }

        @Override
        protected void endPointOpened(EndPoint endpoint)
        {
            super.endPointOpened(endpoint);
            onEndPointOpened(endpoint);
        }

        @Override
        protected void endPointClosed(EndPoint endpoint)
        {
            onEndPointClosed(endpoint);
            super.endPointClosed(endpoint);
        }
    }
}
