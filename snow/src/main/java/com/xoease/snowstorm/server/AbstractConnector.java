package com.xoease.snowstorm.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.io.ArrayByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.FutureCallback;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;

public abstract class AbstractConnector extends ContainerLifeCycle implements Connector, Dumpable
{
    protected final Logger LOG = Log.getLogger(getClass());
    // Order is important on server side, so we use a LinkedHashMap
    private final Map<String, ConnectionFactory> _factories = new LinkedHashMap<>();
    private final SnowAbstractServer _server;
    private final Executor _executor;
    private final Scheduler _scheduler;
    private final ByteBufferPool _byteBufferPool;
    private final Thread[] _acceptors;
    private final Set<EndPoint> _endpoints = Collections.newSetFromMap(new ConcurrentHashMap<EndPoint,Boolean>());
    private final Set<EndPoint> _immutableEndPoints = Collections.unmodifiableSet(_endpoints);
    private volatile CountDownLatch _stopping;
    private long _idleTimeout =0;
    private String _defaultProtocol;
    private ConnectionFactory _defaultConnectionFactory;
    private String _name;
    private int _acceptorPriorityDelta;


    /**
     * @param server The server this connector will be added to. Must not be null.
     * @param executor An executor for this connector or null to use the servers executor
     * @param scheduler A scheduler for this connector or null to either a {@link Scheduler} set as a server bean or if none set, then a new {@link ScheduledExecutorScheduler} instance.
     * @param pool A buffer pool for this connector or null to either a {@link ByteBufferPool} set as a server bean or none set, the new  {@link ArrayByteBufferPool} instance.
     * @param acceptors the number of acceptor threads to use, or -1 for a default value. If 0, then no acceptor threads will be launched and some other mechanism will need to be used to accept new connections.
     * @param factories The Connection Factories to use.
     */
    public AbstractConnector(
            SnowAbstractServer server,
            Executor executor,
            Scheduler scheduler,
            ByteBufferPool pool,
            int acceptors,
            ConnectionFactory... factories)
    {
        _server=server;
        _executor=executor!=null?executor:_server.getThreadPool();
        if (scheduler==null)
            scheduler=_server.getBean(Scheduler.class);
        _scheduler=scheduler!=null?scheduler:new ScheduledExecutorScheduler();
        if (pool==null)
            pool=_server.getBean(ByteBufferPool.class);
        _byteBufferPool = pool!=null?pool:new ArrayByteBufferPool();

        addBean(_server,false);
        addBean(_executor);
        if (executor==null)
            unmanage(_executor); // inherited from server
        addBean(_scheduler);
        addBean(_byteBufferPool);

        for (ConnectionFactory factory:factories)
            addConnectionFactory(factory);

        int cores = Runtime.getRuntime().availableProcessors();
        if (acceptors < 0)
            acceptors=Math.max(1, Math.min(4,cores/8));
        if (acceptors > cores)
            LOG.warn("Acceptors should be <= availableProcessors: " + this);
        _acceptors = new Thread[acceptors];
    }


    @Override
    public SnowAbstractServer getServer()
    {
        return _server;
    }

    @Override
    public Executor getExecutor()
    {
        return _executor;
    }

    @Override
    public ByteBufferPool getByteBufferPool()
    {
        return _byteBufferPool;
    }

    @Override
    @ManagedAttribute("Idle timeout")
    public long getIdleTimeout()
    {
        return _idleTimeout;
    }

    /**
     * <p>Sets the maximum Idle time for a connection, which roughly translates to the {@link Socket#setSoTimeout(int)}
     * call, although with NIO implementations other mechanisms may be used to implement the timeout.</p>
     * <p>The max idle time is applied:</p>
     * <ul>
     * <li>When waiting for a new message to be received on a connection</li>
     * <li>When waiting for a new message to be sent on a connection</li>
     * </ul>
     * <p>This value is interpreted as the maximum time between some progress being made on the connection.
     * So if a single byte is read or written, then the timeout is reset.</p>
     *
     * @param idleTimeout the idle timeout
     */
    public void setIdleTimeout(long idleTimeout)
    {
        _idleTimeout = idleTimeout;
    }

    /**
     * @return Returns the number of acceptor threads.
     */
    @ManagedAttribute("number of acceptor threads")
    public int getAcceptors()
    {
        return _acceptors.length;
    }

    @Override
    protected void doStart() throws Exception
    {
        _defaultConnectionFactory = getConnectionFactory(_defaultProtocol);
        if(_defaultConnectionFactory==null)
            throw new IllegalStateException("No protocol factory for default protocol: "+_defaultProtocol);

        super.doStart();

        _stopping=new CountDownLatch(_acceptors.length);
        for (int i = 0; i < _acceptors.length; i++)
        {
            Acceptor a = new Acceptor(i);
            addBean(a);
            getExecutor().execute(a);
        }

        LOG.info("Started {}", this);
    }


    protected void interruptAcceptors()
    {
        synchronized (this)
        {
            for (Thread thread : _acceptors)
            {
                if (thread != null)
                    thread.interrupt();
            }
        }
    }

    @Override
    public Future<Void> shutdown()
    {
        return new FutureCallback(true);
    }

    @Override
    protected void doStop() throws Exception
    {
        // Tell the acceptors we are stopping
        interruptAcceptors();

        // If we have a stop timeout
        long stopTimeout = getStopTimeout();
        CountDownLatch stopping=_stopping;
        if (stopTimeout > 0 && stopping!=null)
            stopping.await(stopTimeout,TimeUnit.MILLISECONDS);
        _stopping=null;

        super.doStop();

        for (Acceptor a : getBeans(Acceptor.class))
            removeBean(a);

        LOG.info("Stopped {}", this);
    }

    public void join() throws InterruptedException
    {
        join(0);
    }

    public void join(long timeout) throws InterruptedException
    {
        synchronized (this)
        {
            for (Thread thread : _acceptors)
                if (thread != null)
                    thread.join(timeout);
        }
    }

    protected abstract void accept(int acceptorID) throws IOException, InterruptedException;


    /* ------------------------------------------------------------ */
    /**
     * @return Is the connector accepting new connections
     */
    protected boolean isAccepting()
    {
        return isRunning();
    }

    @Override
    public ConnectionFactory getConnectionFactory(String protocol)
    {
        synchronized (_factories)
        {
            return _factories.get(StringUtil.asciiToLowerCase(protocol));
        }
    }

    @Override
    public <T> T getConnectionFactory(Class<T> factoryType)
    {
        synchronized (_factories)
        {
            for (ConnectionFactory f : _factories.values())
                if (factoryType.isAssignableFrom(f.getClass()))
                    return (T)f;
            return null;
        }
    }

    public void addConnectionFactory(ConnectionFactory factory)
    {
        synchronized (_factories)
        {
            Set<ConnectionFactory> to_remove = new HashSet<>();
            for (String key:factory.getProtocols())
            {
                key=StringUtil.asciiToLowerCase(key);
                ConnectionFactory old=_factories.remove(key);
                if (old!=null)
                {
                    if (old.getProtocol().equals(_defaultProtocol))
                        _defaultProtocol=null;
                    to_remove.add(old);
                }
                _factories.put(key, factory);
            }

            // keep factories still referenced
            for (ConnectionFactory f : _factories.values())
                to_remove.remove(f);

            // remove old factories
            for (ConnectionFactory old: to_remove)
            {
                removeBean(old);
                if (LOG.isDebugEnabled())
                    LOG.debug("{} removed {}", this, old);
            }

            // add new Bean
            addBean(factory);
            if (_defaultProtocol==null)
                _defaultProtocol=factory.getProtocol();
            if (LOG.isDebugEnabled())
                LOG.debug("{} added {}", this, factory);
        }
    }

    public void addFirstConnectionFactory(ConnectionFactory factory)
    {
        synchronized (_factories)
        {
            List<ConnectionFactory> existings = new ArrayList<>(_factories.values());
            _factories.clear();
            addConnectionFactory(factory);
            for (ConnectionFactory existing : existings)
                addConnectionFactory(existing);
            _defaultProtocol = factory.getProtocol();
        }
    }

    public void addIfAbsentConnectionFactory(ConnectionFactory factory)
    {
        synchronized (_factories)
        {
            String key=StringUtil.asciiToLowerCase(factory.getProtocol());
            if (_factories.containsKey(key))
            {
                if (LOG.isDebugEnabled())
                    LOG.debug("{} addIfAbsent ignored {}", this, factory);
            }
            else
            {
                _factories.put(key, factory);
                addBean(factory);
                if (_defaultProtocol==null)
                    _defaultProtocol=factory.getProtocol();
                if (LOG.isDebugEnabled())
                    LOG.debug("{} addIfAbsent added {}", this, factory);
            }
        }
    }

    public ConnectionFactory removeConnectionFactory(String protocol)
    {
        synchronized (_factories)
        {
            ConnectionFactory factory= _factories.remove(StringUtil.asciiToLowerCase(protocol));
            removeBean(factory);
            return factory;
        }
    }

    @Override
    public Collection<ConnectionFactory> getConnectionFactories()
    {
        synchronized (_factories)
        {
            return _factories.values();
        }
    }

    public void setConnectionFactories(Collection<ConnectionFactory> factories)
    {
        synchronized (_factories)
        {
            List<ConnectionFactory> existing = new ArrayList<>(_factories.values());
            for (ConnectionFactory factory: existing)
                removeConnectionFactory(factory.getProtocol());
            for (ConnectionFactory factory: factories)
                if (factory!=null)
                    addConnectionFactory(factory);
        }
    }

    @ManagedAttribute("The priority delta to apply to acceptor threads")
    public int getAcceptorPriorityDelta()
    {
        return _acceptorPriorityDelta;
    }

    /* ------------------------------------------------------------ */
    /** Set the acceptor thread priority delta.
     * <p>This allows the acceptor thread to run at a different priority.
     * Typically this would be used to lower the priority to give preference
     * to handling previously accepted connections rather than accepting
     * new connections</p>
     * @param acceptorPriorityDelta the acceptor priority delta
     */
    public void setAcceptorPriorityDelta(int acceptorPriorityDelta)
    {
        int old=_acceptorPriorityDelta;
        _acceptorPriorityDelta = acceptorPriorityDelta;
        if (old!=acceptorPriorityDelta && isStarted())
        {
            for (Thread thread : _acceptors)
                thread.setPriority(Math.max(Thread.MIN_PRIORITY,Math.min(Thread.MAX_PRIORITY,thread.getPriority()-old+acceptorPriorityDelta)));
        }
    }

    @Override
    @ManagedAttribute("Protocols supported by this connector")
    public List<String> getProtocols()
    {
        synchronized (_factories)
        {
            return new ArrayList<>(_factories.keySet());
        }
    }

    public void clearConnectionFactories()
    {
        synchronized (_factories)
        {
            _factories.clear();
        }
    }

    @ManagedAttribute("This connector's default protocol")
    public String getDefaultProtocol()
    {
        return _defaultProtocol;
    }

    public void setDefaultProtocol(String defaultProtocol)
    {
        _defaultProtocol = StringUtil.asciiToLowerCase(defaultProtocol);
        if (isRunning())
            _defaultConnectionFactory=getConnectionFactory(_defaultProtocol);
    }

    @Override
    public ConnectionFactory getDefaultConnectionFactory()
    {
        if (isStarted())
            return _defaultConnectionFactory;
        return getConnectionFactory(_defaultProtocol);
    }

    private class Acceptor implements Runnable
    {
        private final int _id;
        private String _name;

        private Acceptor(int id)
        {
            _id = id;
        }

        @Override
        public void run()
        {
            final Thread thread = Thread.currentThread();
            String name=thread.getName();
            _name=String.format("%s-acceptor-%d@%x-%s",name,_id,hashCode(),AbstractConnector.this.toString());
            thread.setName(_name);

            int priority=thread.getPriority();
            if (_acceptorPriorityDelta!=0)
                thread.setPriority(Math.max(Thread.MIN_PRIORITY,Math.min(Thread.MAX_PRIORITY,priority+_acceptorPriorityDelta)));

            synchronized (AbstractConnector.this)
            {
                _acceptors[_id] = thread;
            }

            try
            {
                while (isAccepting())
                {
                    try
                    {
                        accept(_id);
                    }
                    catch (Throwable e)
                    {
                        if (isAccepting())
                            LOG.warn(e);
                        else
                            LOG.ignore(e);
                    }
                }
            }
            finally
            {
                thread.setName(name);
                if (_acceptorPriorityDelta!=0)
                    thread.setPriority(priority);

                synchronized (AbstractConnector.this)
                {
                    _acceptors[_id] = null;
                }
                CountDownLatch stopping=_stopping;
                if (stopping!=null)
                    stopping.countDown();
            }
        }

        @Override
        public String toString()
        {
            String name=_name;
            if (name==null)
                return String.format("acceptor-%d@%x", _id, hashCode());
            return name;
        }

    }




//    protected void connectionOpened(Connection connection)
//    {
//        _stats.connectionOpened();
//        connection.onOpen();
//    }
//
//    protected void connectionClosed(Connection connection)
//    {
//        connection.onClose();
//        long duration = System.currentTimeMillis() - connection.getEndPoint().getCreatedTimeStamp();
//        _stats.connectionClosed(duration, connection.getMessagesIn(), connection.getMessagesOut());
//    }
//
//    public void connectionUpgraded(Connection oldConnection, Connection newConnection)
//    {
//        oldConnection.onClose();
//        _stats.connectionUpgraded(oldConnection.getMessagesIn(), oldConnection.getMessagesOut());
//        newConnection.onOpen();
//    }

    @Override
    public Collection<EndPoint> getConnectedEndPoints()
    {
        return _immutableEndPoints;
    }

    protected void onEndPointOpened(EndPoint endp)
    {
        _endpoints.add(endp);
    }

    protected void onEndPointClosed(EndPoint endp)
    {
        _endpoints.remove(endp);
    }

    @Override
    public Scheduler getScheduler()
    {
        return _scheduler;
    }

    @Override
    public String getName()
    {
        return _name;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set a connector name.   A context may be configured with
     * virtual hosts in the form "@contextname" and will only serve
     * requests from the named connector,
     * @param name A connector name.
     */
    public void setName(String name)
    {
        _name=name;
    }

    @Override
    public String toString()
    {
        return String.format("%s@%x{%s,%s}",
                _name==null?getClass().getSimpleName():_name,
                hashCode(),
                getDefaultProtocol(),getProtocols());
    }
}
