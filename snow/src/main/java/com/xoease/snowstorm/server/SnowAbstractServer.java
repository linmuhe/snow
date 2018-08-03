package com.xoease.snowstorm.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.ClassLoaderDump;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.AttributesMap;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.Uptime;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.Name;
import org.eclipse.jetty.util.component.Graceful;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ShutdownThread;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool.SizedThreadPool;

import com.xoease.snowstorm.config.Snow;
import com.xoease.snowstorm.io.NetworkTrafficServerConnector;
import com.xoease.snowstorm.server.SnowConnector.SnowServerConnecFactoryI;
import com.xoease.snowstorm.server.handler.Handler;
import com.xoease.snowstorm.server.handler.HandlerWrapper;
import com.xoease.snowstorm.util.ShutdownMonitor;


/**
 * @author muhe
 * 启动一个线程池
 * 创建SnowConnector 加入到beans 它启动监听连接服务  
 * 这个类要实现一个工厂  工厂负责创建相应的请求
 */
public abstract class SnowAbstractServer extends  HandlerWrapper implements Attributes, SnowServerConnecFactoryI{
	
	private static final Logger LOG = Log.getLogger(SnowAbstractServer.class);
	/* ------------------------------------------------------------ */
    @ManagedAttribute("version of this server")
    public static String getVersion()
    {
        return Snow.VER;
    }
    /* execute before server.start()
	 * @see com.xoease.snowstorm.server.SnowAbstractServer#createConnector()
	 */
    @Override
 	public AbstractNetworkConnector createConnector() {
 		return new SnowConnector(this);
 	}
    private final AttributesMap _attributes = new AttributesMap();
    private final List<Connector> _connectors = new CopyOnWriteArrayList<>();
    private boolean _dumpAfterStart=false;
    private boolean _dumpBeforeStop=false;
   
    private SessionIdManager _sessionIdManager;
    private boolean _stopAtShutdown;
    private final ThreadPool _threadPool;

   
    /* ------------------------------------------------------------ */
    public SnowAbstractServer()
    {
        this((ThreadPool)null);
    }

    /* ------------------------------------------------------------ */
    /**
     * Convenience constructor
     * <p>
     * Creates server and a {@link ServerConnector} at the passed address.
     * @param addr the inet socket address to create the connector from
     */
    public SnowAbstractServer(InetSocketAddress addr) {
		this((ThreadPool)null);
		AbstractNetworkConnector connector = createConnector();
        connector.setHost(addr.getHostName());
        connector.setPort(addr.getPort());
        setConnectors(new Connector[]{connector});
    }

    /* ------------------------------------------------------------ */
    /** Convenience constructor
     * Creates server and a {@link ServerConnector} at the passed port.
     * @param port The port of a network HTTP connector (or 0 for a randomly allocated port).
     * @see NetworkConnector#getLocalPort()
     */
    public SnowAbstractServer(int port) {
		 this((ThreadPool)null);
		 AbstractNetworkConnector connector=createConnector();
	     connector.setPort(port);

	     setConnectors(new Connector[]{connector});
	}

    /* ------------------------------------------------------------ */
    public SnowAbstractServer(@Name("threadpool") ThreadPool pool)
    {
        _threadPool=pool!=null?pool:new QueuedThreadPool();
        addBean(_threadPool);
        setServer(this);
    }

    /* ------------------------------------------------------------ */
    public void addConnector(Connector connector)
    {
        if (connector.getServer() != this)
            throw new IllegalArgumentException("Connector " + connector +
                    " cannot be shared among server " + connector.getServer() + " and server " + this);
        if (_connectors.add(connector))
            addBean(connector);
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.eclipse.util.AttributesMap#clearAttributes()
     */
    @Override
    public void clearAttributes()
    {
        Enumeration<String> names = _attributes.getAttributeNames();
        while (names.hasMoreElements())
            removeBean(_attributes.getAttribute(names.nextElement()));
        _attributes.clearAttributes();
    }



    /* ------------------------------------------------------------ */
    @Override
    protected void doStart() throws Exception
    {
        //If the Server should be stopped when the jvm exits, register
        //with the shutdown handler thread.
        if (getStopAtShutdown())
            ShutdownThread.register(this);

        //Register the Server with the handler thread for receiving
        //remote stop commands
       ShutdownMonitor.register(this);
        //Start a thread waiting to receive "stop" commands.
       ShutdownMonitor shutmonitor=null ;
       if(( shutmonitor = getShutdownMonitor())!=null){
    	   shutmonitor.start();
       }
         LOG.info("SnowServ-v" + getVersion());
      //  HttpGenerator.setJettyVersion(HttpConfiguration.SERVER_VERSION);
        MultiException mex=new MultiException();

        // check size of thread pool
        SizedThreadPool pool = getBean(SizedThreadPool.class);
        int max=pool==null?-1:pool.getMaxThreads();
        int selectors=0;
        int acceptors=0;
        if (mex.size()==0)
        {
            for (Connector connector : _connectors)
            {
                if (connector instanceof AbstractConnector)
                    acceptors+=((AbstractConnector)connector).getAcceptors();

                if (connector instanceof ServerConnector)
                    selectors+=((ServerConnector)connector).getSelectorManager().getSelectorCount();
            }
        }

        int needed=1+selectors+acceptors;
        if (max>0 && needed>max)
            throw new IllegalStateException(String.format("Insufficient threads: max=%d < needed(acceptors=%d + selectors=%d + request=1)",max,acceptors,selectors));

        try
        {
            super.doStart();
        }
        catch(Throwable e)
        {
            mex.add(e);
        }

        // start connectors last
        for (Connector connector : _connectors)
        {
            try
            {
                connector.start();
            }
            catch(Throwable e)
            {
                mex.add(e);
            }
        }

        if (isDumpAfterStart())
            dumpStdErr();

        mex.ifExceptionThrow();

        LOG.info(String.format("Started @%dms",Uptime.getUptime()));

    }


    /* ------------------------------------------------------------ */
    @Override
    protected void doStop() throws Exception
    {
        if (isDumpBeforeStop())
            dumpStdErr();

        if (LOG.isDebugEnabled())
            LOG.debug("doStop {}",this);
        
        MultiException mex=new MultiException();

        // list if graceful futures
        List<Future<Void>> futures = new ArrayList<>();

        // First close the network connectors to stop accepting new connections
        for (Connector connector : _connectors)
            futures.add(connector.shutdown());

        // Then tell the contexts that we are shutting down
        Handler[] gracefuls = getChildHandlersByClass(Graceful.class);
        for (Handler graceful : gracefuls)
            futures.add(((Graceful)graceful).shutdown());

        // Shall we gracefully wait for zero connections?
        long stopTimeout = getStopTimeout();
        if (stopTimeout>0)
        {
            long stop_by=System.currentTimeMillis()+stopTimeout;
            if (LOG.isDebugEnabled())
                LOG.debug("Graceful shutdown {} by ",this,new Date(stop_by));

            // Wait for shutdowns
            for (Future<Void> future: futures)
            {
                try
                {
                    if (!future.isDone())
                        future.get(Math.max(1L,stop_by-System.currentTimeMillis()),TimeUnit.MILLISECONDS);
                }
                catch (Exception e)
                {
                    mex.add(e);
                }
            }
        }

        // Cancel any shutdowns not done
        for (Future<Void> future: futures)
            if (!future.isDone())
                future.cancel(true);

        // Now stop the connectors (this will close existing connections)
        for (Connector connector : _connectors)
        {
            try
            {
                connector.stop();
            }
            catch (Throwable e)
            {
                mex.add(e);
            }
        }

        // And finally stop everything else
        try
        {
            super.doStop();
        }
        catch (Throwable e)
        {
            mex.add(e);
        }

        if (getStopAtShutdown())
            ShutdownThread.deregister(this);

        //Unregister the Server with the handler thread for receiving
        //remote stop commands as we are stopped already
        ShutdownMonitor.deregister(this);

        mex.ifExceptionThrow();
    }

    /* ------------------------------------------------------------ */
    @Override
    public void dump(Appendable out,String indent) throws IOException
    {
        dumpBeans(out,indent,Collections.singleton(new ClassLoaderDump(this.getClass().getClassLoader())));
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.eclipse.util.AttributesMap#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String name)
    {
        return _attributes.getAttribute(name);
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.eclipse.util.AttributesMap#getAttributeNames()
     */
    @Override
    public Enumeration<String> getAttributeNames()
    {
        return AttributesMap.getAttributeNamesCopy(_attributes);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the connectors.
     */
    @ManagedAttribute(value="connectors for this server", readonly=true)
    public Connector[] getConnectors()
    {
        List<Connector> connectors = new ArrayList<>(_connectors);
        return connectors.toArray(new Connector[connectors.size()]);
    }

    

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the sessionIdManager.
     */
    public SessionIdManager getSessionIdManager()
    {
        return _sessionIdManager;
    }

    /* ------------------------------------------------------------ */
    public boolean getStopAtShutdown()
    {
        return _stopAtShutdown;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the threadPool.
     */
    @ManagedAttribute("the server thread pool")
    public ThreadPool getThreadPool()
    {
        return _threadPool;
    }

    /**
     * @return true if {@link #dumpStdErr()} is called after starting
     */
    @ManagedAttribute("dump state to stderr after start")
    public boolean isDumpAfterStart()
    {
        return _dumpAfterStart;
    }

    /**
     * @return true if {@link #dumpStdErr()} is called before stopping
     */
    @ManagedAttribute("dump state to stderr before stop")
    public boolean isDumpBeforeStop()
    {
        return _dumpBeforeStop;
    }

   

    /* ------------------------------------------------------------ */
    public void join() throws InterruptedException
    {
        getThreadPool().join();
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.eclipse.util.AttributesMap#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute(String name)
    {
        Object bean=_attributes.getAttribute(name);
        if (bean!=null)
            removeBean(bean);
        _attributes.removeAttribute(name);
    }

    /* ------------------------------------------------------------ */
    /**
     * Convenience method which calls {@link #getConnectors()} and {@link #setConnectors(Connector[])} to
     * remove a connector.
     * @param connector The connector to remove.
     */
    public void removeConnector(Connector connector)
    {
        if (_connectors.remove(connector))
            removeBean(connector);
    }

   

    /* ------------------------------------------------------------ */
    /*
     * @see org.eclipse.util.AttributesMap#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String name, Object attribute)
    {
        addBean(attribute);
        _attributes.setAttribute(name, attribute);
    }

    /* ------------------------------------------------------------ */
    /** Set the connectors for this server.
     * Each connector has this server set as it's ThreadPool and its Handler.
     * @param connectors The connectors to set.
     */
    public void setConnectors(Connector[] connectors)
    {
        if (connectors != null)
        {
            for (Connector connector : connectors)
            {
                if (connector.getServer() != this)
                    throw new IllegalArgumentException("Connector " + connector +
                            " cannot be shared among server " + connector.getServer() + " and server " + this);
            }
        }

        Connector[] oldConnectors = getConnectors();
        updateBeans(oldConnectors, connectors);
        _connectors.removeAll(Arrays.asList(oldConnectors));
        if (connectors != null)
            _connectors.addAll(Arrays.asList(connectors));
    }

    /**
     * @param dumpAfterStart true if {@link #dumpStdErr()} is called after starting
     */
    public void setDumpAfterStart(boolean dumpAfterStart)
    {
        _dumpAfterStart = dumpAfterStart;
    }

    /**
     * @param dumpBeforeStop true if {@link #dumpStdErr()} is called before stopping
     */
    public void setDumpBeforeStop(boolean dumpBeforeStop)
    {
        _dumpBeforeStop = dumpBeforeStop;
    }

  
    /* ------------------------------------------------------------ */
    /**
     * @param sessionIdManager The sessionIdManager to set.
     */
    public void setSessionIdManager(SessionIdManager sessionIdManager)
    {
        updateBean(_sessionIdManager,sessionIdManager);
        _sessionIdManager=sessionIdManager;
    }

    /* ------------------------------------------------------------ */
    /** Set stop server at shutdown behaviour.
     * @param stop If true, this server instance will be explicitly stopped when the
     * JVM is shutdown. Otherwise the JVM is stopped with the server running.
     * @see Runtime#addShutdownHook(Thread)
     * @see ShutdownThread
     */
    public void setStopAtShutdown(boolean stop)
    {
        //if we now want to stop
        if (stop)
        {
            //and we weren't stopping before
            if (!_stopAtShutdown)
            {
                //only register to stop if we're already started (otherwise we'll do it in doStart())
                if (isStarted())
                    ShutdownThread.register(this);
            }
        }
        else
            ShutdownThread.deregister(this);

        _stopAtShutdown=stop;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set a graceful stop time.
     * The {@link StatisticsHandler} must be configured so that open connections can
     * be tracked for a graceful shutdown.
     * @see org.eclipse.jetty.util.component.ContainerLifeCycle#setStopTimeout(long)
     */
    @Override
    public void setStopTimeout(long stopTimeout)
    {
        super.setStopTimeout(stopTimeout);
    }

   
    @Override
    protected void start(LifeCycle l) throws Exception
    {
        // start connectors last
        if (!(l instanceof Connector))
            super.start(l);
    }

    /* ------------------------------------------------------------ */
    @Override
    public String toString()
    {
        return this.getClass().getName()+"@"+Integer.toHexString(hashCode());
    }
    /**
     * 返回空 表示不启用
     * @return
     */
	public ShutdownMonitor getShutdownMonitor() {
		ShutdownMonitor monitor = ShutdownMonitor.getInstance();
		return monitor;
	}


}
